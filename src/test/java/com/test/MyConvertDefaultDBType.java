package com.test;

import com.lqsmart.mysql.entity.ConvertDefaultDBType;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/21.
 */

public class MyConvertDefaultDBType implements ConvertDefaultDBType {
    @Override
    public Object formatToDbData(Object o) {
        return ((TestEnum2)o).getI();
    }

    @Override
    public Object formatFromDb(Class cls, String value) {
        return Integer.valueOf(value) == 1?TestEnum2.More:TestEnum2.More;
    }
}
