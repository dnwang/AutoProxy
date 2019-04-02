package org.pinwheel.autoproxy

import jdk.internal.org.objectweb.asm.Opcodes

/**
 * Copyright (C), 2019 <br>
 * <br>
 * All rights reserved <br>
 * <br>
 *
 * @author dnwang
 * @version 2019/3/27,14:55
 */
final class Tools {

    static int getICONST(int i) {
        if (0 == i) {
            return Opcodes.ICONST_0
        } else if (1 == i) {
            return Opcodes.ICONST_1
        } else if (2 == i) {
            return Opcodes.ICONST_2
        } else if (3 == i) {
            return Opcodes.ICONST_3
        } else if (4 == i) {
            return Opcodes.ICONST_4
        } else if (5 == i) {
            return Opcodes.ICONST_5
        } else {
            throw new RuntimeException("[getICONST]: i=[0,5]")
        }
    }

    static String getReturnClass(String desc) {
        if (null == desc || desc.length() < 3) {
            return null // ()
        }
        return desc.substring(desc.lastIndexOf(")") + 1)
    }

    static List<String> getParams(String desc) {
        if (null == desc || desc.length() < 4) {
            return Collections.EMPTY_LIST // ()V
        }
        List<String> params = new ArrayList<>()
        // 91[ 76L 59; 73I 83S 66B 67C 74J 90Z 68D 70F  41)
        int length = desc.length()
        int posL = -1
        int cursor
        int posArray = -1
        for (int i = 0; i < length; i++) {
            cursor = desc.charAt(i) as int
            if (posArray < 0 && 91 == cursor) {
                posArray = i
            } else if (76 == cursor) {
                posL = i
            } else if (posL >= 0 && 59 == cursor) {
                params.add(desc.substring(posArray >= 0 ? posArray : posL, i + 1))
                posArray = -1
                posL = -1
            } else if (posL < 0 && (73 == cursor || 83 == cursor || 67 == cursor
                    || 74 == cursor || 90 == cursor || 68 == cursor
                    || 70 == cursor || 66 == cursor)) {
                if (posArray >= 0) {
                    params.add(desc.substring(posArray, i + 1))
                } else {
                    params.add((cursor as char).toString())
                }
                posArray = -1
            } else if (41 == cursor) {
                break
            }
        }
        return params
    }

    static String getValue(Object value, String defValue) {
        if (null == value) {
            return defValue
        } else {
            String v = String.valueOf(value).trim()
            return isEmpty(v) ? defValue : v
        }
    }

    static String getClazz(File path, File file) {
        def clazz = file.absolutePath
        def suffix = clazz.lastIndexOf(".")
        return clazz.substring(path.absolutePath.length() + 1, suffix > 0 ? suffix : clazz.length())
                .replaceAll("\\\\", "/")
    }

    static String getClazz(String file) {
        def suffix = file.lastIndexOf(".")
        if (suffix > 0) {
            return file.substring(0, suffix)
        } else {
            return file
        }
    }

    static boolean filterClassFile(String name) {
        return name.endsWith(".class")
    }

    static boolean isEmpty(Object obj) {
        if (obj instanceof String) {
            String tmp = (String) obj
            return "null" == tmp || "" == obj.trim()
        } else if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty()
        } else {
            return true
        }
    }

}