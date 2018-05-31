package com.lqsmart.mysql.compiler.util;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/21.
 */
public class JavaFile {
    private String name;
    private String code;

    public JavaFile(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
