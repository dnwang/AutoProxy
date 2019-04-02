package org.pinwheel.autoproxy

import jdk.internal.org.objectweb.asm.AnnotationVisitor
import jdk.internal.org.objectweb.asm.ClassVisitor
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes

final class MappingFinderVisitor extends ClassVisitor implements Opcodes, Constants {
    private int classAccess
    private String clazz
    private Collection<Mapping> mappings

    MappingFinderVisitor(ClassVisitor cv, Collection<Mapping> mappings) {
        super(Opcodes.ASM5, cv)
        this.mappings = mappings
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.classAccess = access
        this.clazz = name
    }

    @Override
    MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
        new MethodVisitor(ASM5, super.visitMethod(access, methodName, desc, signature, exceptions)) {
            @Override
            AnnotationVisitor visitAnnotation(String annotation, boolean _) {
                if (ANNOTATION_CLASS == annotation && verifyMethodTouchable(access, methodName, desc)) {
                    Mapping mapping = new Mapping()
                    new AnnotationVisitor(ASM5, super.visitAnnotation(annotation, _)) {
                        @Override
                        void visit(String key, Object value) {
                            super.visit(key, value)
                            if (KEY_INCLUDE == key) {
                                mapping.include = Tools.getValue(value, "*")
                            } else if (KEY_EXCLUDE == key) {
                                mapping.exclude = Tools.getValue(value, null)
                            } else if (KEY_METHOD == key) {
                                mapping.method = Tools.getValue(value, "*")
                            } else if (KEY_PARAMS == key) {
                                mapping.params = Tools.getValue(value, "*")
                            }
                        }

                        @Override
                        void visitEnd() {
                            super.visitEnd()
                            mapping.proxyClass = clazz
                            mapping.proxyMethod = methodName
                            mappings.add(mapping)
                        }
                    }
                } else {
                    super.visitAnnotation(annotation, _)
                }
            }
        }
    }

    private boolean verifyMethodTouchable(int access, String name, String desc) {
        if ((classAccess & ACC_PUBLIC) == 0) { // public, final public
            throw new RuntimeException("[AutoProxy]: class must be 'public'.  " + clazz)
        }
        if ((access & (ACC_PUBLIC | ACC_STATIC)) != (ACC_PUBLIC | ACC_STATIC)) { // public
            throw new RuntimeException("[AutoProxy]: proxy method must be 'public static'.  " + name)
        }
        if (PROXY_METHOD_PARAMS != desc) {
            throw new RuntimeException("[AutoProxy]: proxy method params must be '(Object, String, Object[]) Proxy.R'.  " + name)
        }
        return true
    }

}