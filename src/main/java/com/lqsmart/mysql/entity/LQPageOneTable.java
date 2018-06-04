package com.lqsmart.mysql.entity;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/4.
 */
public class LQPageOneTable<T> extends LQPage<T> {
    /** select 语句*/
    private String selectSql;
    /**  from语句（除select 语句外的部分） */
    private String fromSql;
    private String uniqueKeyColum;
    private String tableName;

    public LQPageOneTable(){
    }

    /**
     *
     * @param pageCurrent
     * @param pageSize
     * @param uniqueKeyColum 唯一索引 的columName（最好使用id）
     * @param sql
     */
    public LQPageOneTable(int pageCurrent, int pageSize, String uniqueKeyColum,String sql) {
        super(pageCurrent, pageSize);
        this.uniqueKeyColum = uniqueKeyColum;
        this.parsetSql(sql,uniqueKeyColum);
    }

    /**
     *
     * @param sql
     * @param uniqueKeyColum 唯一索引 的columName（最好使用id） 如果不设置则不做优化
     */
    public void setSql(String sql, String uniqueKeyColum){
        this.parsetSql(sql,uniqueKeyColum);
    }

    public void setUniqueKeyColum(String uniqueKeyColum) {
        this.uniqueKeyColum = uniqueKeyColum;
    }

    public String getUniqueKeyColum() {
        return uniqueKeyColum;
    }

    public String getTableName() {
        return tableName;
    }

    private void parsetSql(String sql,String uniqueKeyColum){
        this.uniqueKeyColum = uniqueKeyColum;
        if(this.selectSql != null){
            return;
        }
        this.selectSql = sql;

        int idex = sql.indexOf("from");
        if(idex < 0){
            idex = sql.toLowerCase().indexOf("from");
        }

        if(idex < 0){
           throw new RuntimeException(" cant find from from sql:"+sql);
        }
        this.selectSql = sql.substring(0,idex);
        this.fromSql = sql.substring(idex,sql.length());
        String str = fromSql.substring(fromSql.indexOf(' ',3)).trim();
        idex = str.indexOf(' ');
        if(idex < 0){
            this.tableName = str;
        }else {
            this.tableName = str.substring(0,idex);
        }
    }

    public LQPageOneTable(int pageCurrent, int pageSize) {
        super(pageCurrent, pageSize);
    }

    public String getSelectSql() {
        return selectSql;
    }

    public void setSelectSql(String selectSql) {
        this.selectSql = selectSql;
    }

    public String getFromSql() {
        return fromSql;
    }

    public void setFromSql(String fromSql) {
        this.fromSql = fromSql;
    }
}
