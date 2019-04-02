package org.pinwheel.autoproxy

import jdk.internal.org.objectweb.asm.*

final class MethodRefactorVisitor extends ClassVisitor implements Opcodes, Constants {
    private String clazz
    private int classAccess
    private String className
    private Mapping mapping

    private boolean isRefactored = false

    boolean isRefactored() {
        return isRefactored
    }

    MethodRefactorVisitor(ClassVisitor cv, Mapping mapping) {
        super(Opcodes.ASM5, cv)
        this.mapping = mapping
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.clazz = name
        this.classAccess = access
        this.className = name.replaceAll("/", ".")
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        final String returnClass = Tools.getReturnClass(desc)
        if (!Tools.isEmpty(returnClass) && matchMappings(name, desc)) {
            isRefactored |= true
            mapping.countPlus()
            // refactor start
            println "[proxy]: " + className + "#" + name + desc
            final List<String> params = Tools.getParams(desc)
            return new MethodVisitor(ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {
                @Override
                void visitCode() {
                    final boolean isStaticMethod = (access & ACC_STATIC) == ACC_STATIC
                    final int pSize = params.size()
                    int cursor = isStaticMethod ? 0 : 1// static method should be skip this

                    if (0 != pSize) {
                        if (pSize > 5) {
                            mv.visitIntInsn(BIPUSH, pSize)
                        } else {
                            mv.visitInsn(Tools.getICONST(pSize))
                        }
                        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object")
                        for (int i = 0; i < pSize; i++) {
                            String p = params.get(i)
                            mv.visitInsn(DUP)
                            if (i > 5) {
                                mv.visitIntInsn(BIPUSH, i)
                            } else {
                                mv.visitInsn(Tools.getICONST(i))
                            }
                            if ("B" == p) {
                                mv.visitVarInsn(ILOAD, cursor)
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false)
                            } else if ("I" == p) {
                                mv.visitVarInsn(ILOAD, cursor)
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
                            } else if ("S" == p) {
                                mv.visitVarInsn(ILOAD, cursor)
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false)
                            } else if ("C" == p) {
                                mv.visitVarInsn(ILOAD, cursor)
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false)
                            } else if ("J" == p) {
                                mv.visitVarInsn(LLOAD, cursor)
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false)
                                cursor++ // 2 step
                            } else if ("Z" == p) {
                                mv.visitVarInsn(ILOAD, cursor)
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false)
                            } else if ("D" == p) {
                                mv.visitVarInsn(DLOAD, cursor)
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false)
                                cursor++ // 2 step
                            } else if ("F" == p) {
                                mv.visitVarInsn(FLOAD, cursor)
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false)
                            } else {
                                mv.visitVarInsn(ALOAD, cursor)
                            }
                            mv.visitInsn(AASTORE)
                            cursor++ // move cursor
                        }
                    }
                    if (0 != pSize) {
                        mv.visitVarInsn(ASTORE, cursor)
                    }
                    if (isStaticMethod) {
                        mv.visitLdcInsn(Type.getType("L" + clazz + ";")) // .class
                    } else {
                        mv.visitVarInsn(ALOAD, 0) // 0:this
                    }
                    mv.visitLdcInsn(name)           // method name
                    if (0 != pSize) {
                        mv.visitVarInsn(ALOAD, cursor)  // object[]
                        cursor++
                    } else {
                        mv.visitInsn(ACONST_NULL) // null
                    }
                    // call proxy method
                    mv.visitMethodInsn(INVOKESTATIC, mapping.proxyClass, mapping.proxyMethod, PROXY_METHOD_PARAMS, false)
                    // visit return
                    mv.visitVarInsn(ASTORE, cursor)
                    mv.visitVarInsn(ALOAD, cursor)
                    mv.visitFieldInsn(GETFIELD, PROXY_R_CLASS, "skip", "Z")
                    // if
                    Label l0 = new Label()
                    mv.visitJumpInsn(IFEQ, l0) // if (!skip)
                    if ("V" == returnClass) { // return void
                        mv.visitInsn(RETURN)
                    } else { // return with value
                        mv.visitVarInsn(ALOAD, cursor)
                        mv.visitFieldInsn(GETFIELD, PROXY_R_CLASS, "obj", "Ljava/lang/Object;")
                        if ("B" == returnClass) {
                            String type = "java/lang/Byte"
                            mv.visitTypeInsn(CHECKCAST, type)
                            mv.visitMethodInsn(INVOKEVIRTUAL, type, "byteValue", "()B", false)
                            mv.visitInsn(IRETURN)
                        } else if ("I" == returnClass) {
                            String type = "java/lang/Integer"
                            mv.visitTypeInsn(CHECKCAST, type)
                            mv.visitMethodInsn(INVOKEVIRTUAL, type, "intValue", "()I", false)
                            mv.visitInsn(IRETURN)
                        } else if ("S" == returnClass) {
                            String type = "java/lang/Short"
                            mv.visitTypeInsn(CHECKCAST, type)
                            mv.visitMethodInsn(INVOKEVIRTUAL, type, "shortValue", "()S", false)
                            mv.visitInsn(IRETURN)
                        } else if ("C" == returnClass) {
                            String type = "java/lang/Character"
                            mv.visitTypeInsn(CHECKCAST, type)
                            mv.visitMethodInsn(INVOKEVIRTUAL, type, "charValue", "()C", false)
                            mv.visitInsn(IRETURN)
                        } else if ("J" == returnClass) {
                            String type = "java/lang/Long"
                            mv.visitTypeInsn(CHECKCAST, type)
                            mv.visitMethodInsn(INVOKEVIRTUAL, type, "longValue", "()J", false)
                            mv.visitInsn(LRETURN)
                        } else if ("Z" == returnClass) {
                            String type = "java/lang/Boolean"
                            mv.visitTypeInsn(CHECKCAST, type)
                            mv.visitMethodInsn(INVOKEVIRTUAL, type, "booleanValue", "()Z", false)
                            mv.visitInsn(IRETURN)
                        } else if ("D" == returnClass) {
                            String type = "java/lang/Double"
                            mv.visitTypeInsn(CHECKCAST, type)
                            mv.visitMethodInsn(INVOKEVIRTUAL, type, "doubleValue", "()D", false)
                            mv.visitInsn(DRETURN)
                        } else if ("F" == returnClass) {
                            String type = "java/lang/Float"
                            mv.visitTypeInsn(CHECKCAST, type)
                            mv.visitMethodInsn(INVOKEVIRTUAL, type, "floatValue", "()F", false)
                            mv.visitInsn(FRETURN)
                        } else {
                            mv.visitTypeInsn(CHECKCAST, returnClass)
                            mv.visitInsn(ARETURN)
                        }
                    }
                    mv.visitLabel(l0)
                    mv.visitFrame(F_APPEND, 2, ["[Ljava/lang/Object;", PROXY_R_CLASS] as Object[], 0, null)
                    // original code
                    super.visitCode()
                }
            }
        } else {
            super.visitMethod(access, name, desc, signature, exceptions)
        }
    }

    private boolean matchMappings(String method, String desc) {
        // skip proxy method self
        if (clazz == mapping.proxyClass
                && method == mapping.proxyMethod
                && PROXY_METHOD_PARAMS == desc) {
            return false
        }
        return mapping.matchMethod(method) && mapping.matchParams(desc)
    }

}