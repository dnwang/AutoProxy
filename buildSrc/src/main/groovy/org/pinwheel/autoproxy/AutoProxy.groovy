package org.pinwheel.autoproxy

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassWriter
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

final class AutoProxy extends Transform implements Plugin<Project> {
    private static final String TAG = "autoProxy"

    @Override
    void apply(Project target) {
        def android = target.extensions.getByType(AppExtension)
        android.registerTransform(this)
    }

    @Override
    String getName() {
        return TAG
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation invocation) throws TransformException, InterruptedException, IOException {
        def outputProvider = invocation.outputProvider
        outputProvider.deleteAll()

        Collection<Mapping> mappings = new HashSet<Mapping>()

        findProxyMapping(invocation, mappings)

        invocation.inputs.forEach { input ->
            input.directoryInputs.each { dir ->
                def newDir = outputProvider.getContentLocation(
                        dir.name,
                        dir.contentTypes,
                        dir.scopes,
                        Format.DIRECTORY)

                dir.file.eachFileRecurse { file ->
                    if (file.isFile()) {
                        def mapping
                        if (Tools.filterClassFile(file.name)
                                && null != (mapping = matchMappings(mappings, Tools.getClazz(dir.file, file)))) {
                            def reader = new ClassReader(file.bytes)
                            def writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
                            def visitor = new MethodRefactorVisitor(writer, mapping)
                            reader.accept(visitor, ClassReader.EXPAND_FRAMES)
                            // write new code to dest
                            def newFile = new File(newDir, getRelativePath(file, dir.file))
                            newFile.parentFile.mkdirs()
                            newFile.bytes = writer.toByteArray()
                        } else {
                            // copy to dest
                            def newFile = new File(newDir, getRelativePath(file, dir.file))
                            newFile.parentFile.mkdirs()
                            FileUtils.copyFile(file, newFile)
                        }
                    }
                }
            }

            input.jarInputs.each { jar ->
                def newJar = outputProvider.getContentLocation(
                        jar.name,
                        jar.contentTypes,
                        jar.scopes,
                        Format.JAR)
                newJar.parentFile.mkdirs()

                if (jar.file.name.endsWith(".jar")) {
                    def jarOut = new JarOutputStream(new FileOutputStream(newJar))
                    def jarFile = new JarFile(jar.file)
                    jarFile.entries().each { entry ->
                        jarOut.putNextEntry(new ZipEntry(entry.name))
                        def source = jarFile.getInputStream(entry)
                        def mapping
                        if (!entry.isDirectory()
                                && Tools.filterClassFile(entry.name)
                                && null != (mapping = matchMappings(mappings, Tools.getClazz(entry.name)))) {
                            def reader = new ClassReader(source.bytes)
                            def writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
                            def visitor = new MethodRefactorVisitor(writer, mapping)
                            reader.accept(visitor, ClassReader.EXPAND_FRAMES)
                            jarOut.write(writer.toByteArray())
                        } else {
                            def buf = new byte[1024]
                            def len
                            while ((len = source.read(buf)) > 0) {
                                jarOut.write(buf, 0, len)
                            }
                        }
                        jarOut.closeEntry()
                    }
                    jarOut.close()
                    jarFile.close()
                } else {
                    FileUtils.copyFile(jar.file, newJar)
                }
            }
        }

        // log result
        println("[mappings]: " + mappings.size())
        for (Mapping m : mappings) {
            println(m)
        }
    }

    private static void findProxyMapping(TransformInvocation invocation, Collection<Mapping> mappings) {
        def visitClass = { bytes ->
            def reader = new ClassReader(bytes)
            def writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
            def visitor = new MappingFinderVisitor(writer, mappings)
            reader.accept(visitor, ClassReader.EXPAND_FRAMES)
        }

        invocation.inputs.forEach { input ->
            input.directoryInputs.each { dir ->
                dir.file.eachFileRecurse { file ->
                    if (Tools.filterClassFile(file.name)) {
                        visitClass(file.bytes)
                    }
                }
            }
            input.jarInputs.each { jar ->
                if (jar.file.name.endsWith(".jar")) {
                    def jarFile = new JarFile(jar.file)
                    jarFile.entries().each { entry ->
                        if (Tools.filterClassFile(entry.name)) {
                            visitClass(jarFile.getInputStream(entry).bytes)
                        }
                    }
                    jarFile.close()
                }
            }
        }
    }

    private static Mapping matchMappings(Collection<Mapping> mappings, String clazz) {
        if (MappingFinderVisitor.PROXY_CLASS == clazz
                || MappingFinderVisitor.PROXY_R_CLASS == clazz) {
            return null
        }
        for (Mapping mapping : mappings) {
            if (mapping.matchClass(clazz)) {
                return mapping
            }
        }
        return null
    }

    private static String getRelativePath(File file, File path) {
        return file.absolutePath.substring(path.absolutePath.length())
    }

}