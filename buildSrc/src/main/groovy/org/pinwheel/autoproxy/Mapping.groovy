package org.pinwheel.autoproxy

import java.util.regex.Pattern

/**
 * Copyright (C), 2019 <br>
 * <br>
 * All rights reserved <br>
 * <br>
 *
 * @author dnwang
 * @version 2019/3/27,14:55
 */
final class Mapping {
    String include = "*" as String
    String exclude = null
    String method = "*" as String
    String params = "*" as String

    String proxyClass
    String proxyMethod

    private Pattern pClassInclude
    private Pattern pClassExclude
    private Pattern pMethodInclude
    private Pattern pMethodExclude
    private Pattern pParams

    private int count

    boolean matchClass(String clazz) {
        // skip lambda
        if (clazz.contains("\$lambda\$")) {
            return false
        }
        if (null == pClassInclude) {
            String regex = this.include.replaceAll("[*]", ".*")
            pClassInclude = Pattern.compile(regex)
        }
        boolean is = pClassInclude.matcher(clazz).matches()
        if (null != exclude) {
            if (null == pClassExclude) {
                String regex = this.exclude.replaceAll("[*]", ".*")
                pClassExclude = Pattern.compile(regex)
            }
            is &= !pClassExclude.matcher(clazz).matches()
        }
        return is
    }

    boolean matchMethod(String method) {
        if (null == pMethodInclude) {
            String regex = this.method.replaceAll("[*]", ".*")
            pMethodInclude = Pattern.compile(regex)
        }
        if (null == pMethodExclude) {
            // skip constructor
            pMethodExclude = Pattern.compile("<.*>")
        }
        return !pMethodExclude.matcher(method).matches() && pMethodInclude.matcher(method).matches()
    }

    boolean matchParams(String params) {
        if (null == pParams) {
            String regex = this.params.replaceAll("\\[", "\\\\[").replaceAll("[*]", ".*")
            regex = "\\(" + regex + "\\).*"
            pParams = Pattern.compile(regex)
        }
        return pParams.matcher(params).matches()
    }

    void countPlus() {
        count++
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        Mapping mapping = (Mapping) o
        if (include != mapping.include) return false
        if (method != mapping.method) return false
        if (params != mapping.params) return false
        return true
    }

    int hashCode() {
        int result
        result = (include != null ? include.hashCode() : 0)
        result = 31 * result + (method != null ? method.hashCode() : 0)
        result = 31 * result + (params != null ? params.hashCode() : 0)
        return result
    }

    @Override
    String toString() {
        return "{" +
                "include=" + include + ", exclude=" + exclude + ", method=" + method + ", params=" + params +
                ", proxy=" + proxyClass.replaceAll("/", ".") + "#" + proxyMethod +
                ", count=" + count +
                "}"
    }
}