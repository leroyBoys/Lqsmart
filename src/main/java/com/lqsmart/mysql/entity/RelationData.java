package com.lqsmart.mysql.entity;

import com.lqsmart.mysql.compiler.ColumInit;
import com.lqsmart.mysql.compiler.FieldGetProxy;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/4.
 */
public class RelationData {
    private String fieldName;
    private Class fieldClass;
    private FieldGetProxy fieldGetProxy;
    private ColumInit columInit;
    private SqlTypeToJava sqlTypeToJava;//如果是基本数据类型则直接使用转换器
    private String columName;
    private NewInstance newInstance;
    private DBRelations reltaion;
    private int count;
    private Map<String,ColumInit> colums_target_map;

    public RelationData(DBRelations dbRelations, int count, Field field, FieldGetProxy fieldGetProxy, ColumInit columInit) {
        this.fieldName = field.getName();
        this.count = count;
        this.columInit = columInit;
        this.fieldGetProxy = fieldGetProxy;
        colums_target_map = new HashMap<>(count);
        reltaion = dbRelations;
        if(dbRelations.relation()== DBRelations.Reltaion.OneToMany){
            Type type = field.getGenericType();
            ParameterizedType types =(ParameterizedType)type;
            fieldClass = (Class) (types).getActualTypeArguments()[0];
            Class CollectionCls = (Class) types.getRawType();
            if(CollectionCls.isInterface()){
                if(CollectionCls.getSimpleName().toLowerCase().equals("set")){
                    newInstance = new NewSetInstance(CollectionCls);
                }else{
                    newInstance = new NewListInstance(CollectionCls);
                }
            }else {
                newInstance = new NewInstance(CollectionCls);
            }
        }else {
            fieldClass = field.getType();
            newInstance = new NewInstance(fieldClass);
        }
    }

    public DBRelations getReltaion() {
        return reltaion;
    }

    public SqlTypeToJava getSqlTypeToJava() {
        return sqlTypeToJava;
    }

    public String getColumName() {
        return columName;
    }

    public void setColumName(String columName) {
        this.columName = columName;
    }

    public void setSqlTypeToJava(SqlTypeToJava sqlTypeToJava) {
        this.sqlTypeToJava = sqlTypeToJava;
    }

    public boolean isOneToMany(){
        return reltaion.relation() == DBRelations.Reltaion.OneToMany;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Class getFieldClass() {
        return fieldClass;
    }

    public int getCount() {
        return count;
    }

    public void put(String colum, ColumInit targeColumInit) {
        colums_target_map.put(colum,targeColumInit);
    }

    public Map<String, ColumInit> getColums_target_map() {
        return colums_target_map;
    }

    public FieldGetProxy getFieldGetProxy() {
        return fieldGetProxy;
    }

    public NewInstance getNewInstance() {
        return newInstance;
    }

    public ColumInit getColumInit() {
        return columInit;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(!(obj instanceof RelationData)) return false;
        if(((RelationData)obj).fieldName.equals(fieldName)) return true;
        return false;
    }

    @Override
    public int hashCode() {
        return (fieldName!=null?fieldName.hashCode():1)*31;
    }

    public static class NewInstance{
        protected Class cls;
        public NewInstance(Class cls){
            this.cls = cls;
        }

        public Object create() throws IllegalAccessException, InstantiationException {
            return cls.newInstance();
        }

        public void add(Object list,Object v){}
    }

    public static class NewSetInstance extends NewInstance{
        public NewSetInstance(Class cls){
            super(cls);
        }

        @Override
        public Object create() throws IllegalAccessException, InstantiationException {
            return new HashSet<>();
        }

        @Override
        public void add(Object list, Object v) {
            ((Set)list).add(v);
        }
    }

    public static class NewListInstance extends NewInstance{
        public NewListInstance(Class cls){
            super(cls);
        }
        @Override
        public Object create() throws IllegalAccessException, InstantiationException {
            return new LinkedList<>();
        }

        @Override
        public void add(Object list, Object v) {
            ((List)list).add(v);
        }
    }
}
