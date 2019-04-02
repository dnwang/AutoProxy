package org.pinwheel.autoproxy

/**
 * Copyright (C), 2019 <br>
 * <br>
 * All rights reserved <br>
 * <br>
 *
 * @author dnwang
 * @version 2019/3/27,14:55
 */
interface Constants {

    static final String PROXY_CLASS = "org/pinwheel/autoProxy/Proxy"
    static final String PROXY_R_CLASS = PROXY_CLASS + "\$R"
    static final String ANNOTATION_CLASS = "L" + PROXY_CLASS + ";"
    static final String PROXY_METHOD_PARAMS = "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)L" + PROXY_R_CLASS + ";"

    static final String KEY_INCLUDE = "include"
    static final String KEY_EXCLUDE = "exclude"
    static final String KEY_METHOD = "method"
    static final String KEY_PARAMS = "params"

}