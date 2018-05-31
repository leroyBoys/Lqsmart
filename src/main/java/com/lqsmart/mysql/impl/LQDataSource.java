package com.lqsmart.mysql.impl;

import com.lqsmart.core.LQStart;
import com.lqsmart.entity.LQConntion;
import com.lqsmart.entity.StartInitCache;
import com.lqsmart.mysql.DbCallBack;
import com.lqsmart.mysql.SqlDataSource;
import com.lqsmart.mysql.compiler.ColumInit;
import com.lqsmart.mysql.compiler.FieldGetProxy;
import com.lqsmart.mysql.entity.*;
import com.lqsmart.util.LqLogUtil;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

/**
 * Created by leroy:656515489@qq.com
 * 2017/4/13.
 */
public class LQDataSource implements SqlDataSource,LQConntion {
    private String url;
    private final static Map<String,JdbcColumsArray> cmd_jdbcColumsArrayCache = new HashMap<>();
    private DataSource dds;
    private boolean isConnectioned;
    protected LQDataSource(Properties properties){
        init(properties);
    }

    @Override
    public void reLoad() {

    }

    private void init(Properties properties){
        if(!checkNeedConnect(properties)){
            return;
        }

        StringBuilder sb = new StringBuilder(100);
        try {
            String classDatascource = properties.getProperty("type","com.alibaba.druid.pool.DruidDataSource");
            StartInitCache.MethodCache methodCache = LQStart.getMethodCache().getMethodCache(classDatascource);
            dds = (DataSource) methodCache.cls.newInstance();

            sb.append(classDatascource);
            for (Enumeration<?> e = properties.keys(); e.hasMoreElements() ;) {
                Object ko = e.nextElement();
                if (!(ko instanceof String)) {
                    continue;
                }

                String k = (String) ko;
                String v = properties.get(k).toString();
                Method method = methodCache.methodMap.get(k);
                if(method == null){
                 //   LqLogUtil.warn(k+" not find  match");
                    continue;
                }

                try {
                    Class<?>[] classes = method.getParameterTypes();
                    method.invoke(dds,SqlTypeToJava.get(classes[0]).formtDataFromDb(v));

                    sb.append(",").append(k).append("=").append(v);
                }catch (Exception ex){
                    LqLogUtil.error(k+"  "+v+"  error");
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!testConnection()){
            return;
        }
        LqLogUtil.info("db succ connection:"+sb.toString());
        isConnectioned = true;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dds.getConnection();
    }

    @Override
    public String getKey() {
        return this.url;
    }

    @Override
    public boolean connctioned() {
        return isConnectioned;
    }

    @Override
    public void setConnetioned(boolean isConnectioned) {
        this.isConnectioned = isConnectioned;
    }


    private boolean checkNeedConnect(Properties properties){
        int count = 3;
        for(Object obj:properties.keySet()){
            if (!(obj instanceof String)) {
                continue;
            }
            String str = ((String) obj).toLowerCase();
            if(str.matches("(.*)(name|password|url)(.*)")){
                count--;
                if(str.endsWith("url")){
                    this.url = (String)obj;
                }
            }
        }
        return count<=0;
    }

    private boolean testConnection(){
        Connection cn = null;
        try {
            cn = dds.getConnection();
            return true;
        } catch (Exception e) {
        } finally {
            this.close(null, cn, null);
        }
        return false;
    }

    public boolean ExecuteUpdate(String cmd, Object... p) {
        Connection cn = null;
        PreparedStatement ps = null;
        try {
            cn = getConnection();
            ps = cn.prepareStatement(cmd);
            SetParameter(ps, p);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LqLogUtil.error(cmd+(p==null?"": Arrays.toString(p)),e);
        } finally {
            this.close(ps, cn);
        }
        return false;
    }

    /**
     *
     * @param tableName
     * @param datas
     * @param columNames 要插入数据的列名集合(与columValues 顺序对应)
     * @param columValues 要插入数据的列名对应值（或者函数或者固定值）集合(与columValues 顺序对应)
     * @param commitLimitCount 最大提交数量（根据mysql.cnf中 max_allowed_packet调整）如果小于等于0则为默认5000
     * @param
     * @return
     */
    public  boolean InsertBatch(String tableName,List<Map<String,String>> datas,String[] columNames,String[] columValues,int commitLimitCount) {
        if(commitLimitCount <= 0){
            commitLimitCount = this.getDefaultLimitCount();
        }

        StringBuilder sb = new StringBuilder("INSERT INTO  ");
        sb.append(tableName).append(" (");
        for(int i = 0;i<columNames.length;i++){
            if(i != 0){
                sb.append(",");
            }
            sb.append("`").append(columNames[i]).append("`");
        }
        sb.append(") values ");
        String sql = sb.toString();

        String str;
        List<String> sqls = new LinkedList<>();
        int i = 0;
        for(Map<String,String> map:datas){
            if(i++ != 0){
                sb.append(",");
            }

            sb.append("(");
            for(int j = 0;j<columNames.length;j++){
                if(j != 0){
                    sb.append(",");
                }

                str = columValues[j];
                if(str.endsWith("()")){
                    sb.append(str);
                    continue;
                }

                sb.append("'");
                str = map.get(str);
                if(str == null){
                    str = columValues[j];
                }
                sb.append(str);
                sb.append("'");
            }
            sb.append(")");

            if(i > commitLimitCount){
                sqls.add(sb.toString());
                sb=new StringBuilder(sql);
                i=0;
            }
        }

        if(i > 0){
            sqls.add(sb.toString());
        }

        return ExecuteUpdates(sqls);
    }

    public boolean ExecuteUpdates(List<String> cmds) {
        Connection cn = null;
        Statement ps = null;
        try {
            cn = getConnection();
            cn.setAutoCommit(false);
            ps = cn.createStatement();
            for (String cmd : cmds) {
                ps.addBatch(cmd);
            }
            ps.executeBatch();
            cn.commit();
            return true;
        } catch (Exception e) {
            LqLogUtil.error(LQDataSource.class,e);
        } finally {
            this.close(ps, cn);
        }
        return false;
    }

    public long ExecuteInsert(String sql, Object... p) {
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            cn = getConnection();
            ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            SetParameter(ps, p);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return (Long) rs.getObject(1);
            }
        } catch (Exception e) {
            LqLogUtil.error(this.getClass(),e);
        } finally {
            close(ps, cn, rs);
        }
        return -1;
    }

    public boolean ExecuteEntity(Object instance) {
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql;
            DBTable table = LQStart.instance.getDBTable(instance.getClass());
            if(table == null){
                LqLogUtil.error(" class:"+instance.getClass().getName()+" not config db");
                return false;
            }

            Object id = table.getColumGetMap().get(table.getIdColumName()).formatToDbData(instance);
            if(id != null && Long.valueOf(id.toString())>0){

                sql = updateSql(instance,table);
                cn = getConnection();
                ps = cn.prepareStatement(sql);
                return ps.executeUpdate()>0;
            }else {
                sql = insertSql(instance,table);
            }
            cn = getConnection();
            ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                table.getColumInit(table.getIdColumName()).set(instance,rs,1);
                return true;
            }else {
                return false;
            }
        } catch (Exception e) {
            LqLogUtil.error(this.getClass(),e);
        } finally {
            close(ps, cn, rs);
        }
        return false;
    }

    private String insertSql(Object instance,DBTable table) {
        StringBuilder sql = new StringBuilder();
        StringBuilder names = new StringBuilder();
        StringBuilder values = new StringBuilder();
        sql.append("insert into ").append(table.getName());
        names.append("  ( ");
        values.append("  ( ");
        int i = 0;
        Object object;
        for(Map.Entry<String,FieldGetProxy.FieldGet> entry:table.getColumGetMap().entrySet()){
            if(entry.getKey() == table.getIdColumName()){
                continue;
            }

            if(i > 0){
                names.append(" , ");
                values.append(" , ");
            }
            names.append("`").append(entry.getKey()).append("`");
            object =  entry.getValue().formatToDbData(instance);
            if(object == null){
                values.append("null");
            }else{
                values.append("'").append(object).append("'");
            }
            i++;
        }
        names.append("  ) ");
        values.append("  ) ");
        sql.append(names).append("  values ").append(values);
        return sql.toString();
    }

    private String updateSql(Object instance,DBTable table) {
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(table.getName());
        sql.append("  set ");
        int i = 0;
        Object object;
        for(Map.Entry<String,FieldGetProxy.FieldGet> entry:table.getColumGetMap().entrySet()){
            if(entry.getKey() == table.getIdColumName()){
                continue;
            }
            if(i > 0){
                sql.append(" , ");
            }
            object =  entry.getValue().formatToDbData(instance);
            sql.append(entry.getKey()).append("=");
            if(object == null){
                sql.append("null");
            }else{
                sql.append("'").append(object).append("'");
            }
            i++;
        }
        sql.append("  where id = ").append(table.getColumGetMap().get(table.getIdColumName()).formatToDbData(instance));
        return sql.toString();
    }

    /**
     * 只返回一个值
     * @param cmd
     * @param p
     * @return
     */
    public Object ExecuteQueryOnlyOneValue(String cmd, Object... p) {
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            cn = getConnection();

            ps = cn.prepareStatement(cmd);
            SetParameter(ps, p);

            rs =  ps.executeQuery();
            while (rs.next()){
                return rs.getObject(1);
            }
            return null;
        } catch (Exception e) {
            LqLogUtil.error(this.getClass(),e);
        } finally {
            this.close(ps, cn, rs);
        }
        return null;
    }

    private <T> JdbcColumsArray initColumsArray(ResultSetMetaData rsMeta, DBTable dbTable) throws SQLException {
        String[] array = new String[rsMeta.getColumnCount()];

        final Map<String,Map<String,ColumInit>> relationFieldNames = new HashMap<>(2);
        String str;
        RelationData relationData;
        for (int i = 0, size = array.length; i < size; ++i) {
            str = rsMeta.getColumnLabel(i + 1);
            array[i] = str;
            if(dbTable.getColumInit(str) != null){
                continue;
            }
            relationData = dbTable.getRelationMap(str);
            if(relationData == null){
                array[i] = null;
                continue;
            }

            if(!relationFieldNames.containsKey(relationData.getFieldName())){
                relationFieldNames.put(relationData.getFieldName(),new HashMap<String,ColumInit>());
            }
        }

        if(relationFieldNames.isEmpty()){
            return new JdbcColumsArray(array);
        }

        Set<String> cols = new HashSet<>(array.length);
        for(int i = 0;i<array.length;i++){
            cols.add(array[i]);
        }

        for(Map.Entry<String,Map<String,ColumInit>> entry:relationFieldNames.entrySet()){
            relationData = dbTable.getRelationByFieldName(entry.getKey());
            for(Map.Entry<String,ColumInit> entry1:relationData.getColums_target_map().entrySet()){
                if(cols.contains(entry1.getKey())){
                    entry.getValue().put(entry1.getKey(),entry1.getValue());
                }
            }
        }

        return new MoreJdbcColumsArray(array,relationFieldNames);
    }

    private <T> JdbcColumsArray getJdbcColumsArray(ResultSet rs, Class<T> cls, String cmd, DBTable dbTable) throws SQLException {
        JdbcColumsArray jdbcColumsArray = cmd_jdbcColumsArrayCache.get(cmd);
        if(jdbcColumsArray != null){
            return jdbcColumsArray;
        }

        synchronized (cls) {
            jdbcColumsArray = cmd_jdbcColumsArrayCache.get(cmd);
            if (jdbcColumsArray != null) {
                return jdbcColumsArray;
            }

            jdbcColumsArray = initColumsArray(rs.getMetaData(),dbTable);
            cmd_jdbcColumsArrayCache.put(cmd, jdbcColumsArray);
            return jdbcColumsArray;
        }
    }

    public <T> List<T> ExecuteQueryList(Class<T> cls,String cmd, Object... p) {
        //LqLogUtil.log("cm2d:" + cmd);
        DBTable dbTable = LQStart.instance.getDBTable(cls);
        if(dbTable == null){
            throw new RuntimeException(cls.getSimpleName()+" not config dbentity");
        }

        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            cn = getConnection();

            ps = cn.prepareStatement(cmd);

            SetParameter(ps, p);
            rs =  ps.executeQuery();
            JdbcColumsArray jdbcColumsArray = getJdbcColumsArray(rs,cls,cmd,dbTable);

            QueryResultData<T> resultData = new QueryResultData<>();
            while (rs.next()){
                jdbcColumsArray.doExute(dbTable,rs,cls,resultData);
            }
            return resultData.getResult();
        } catch (Exception e) {
            LqLogUtil.error(this.getClass(),e);
        } finally {
            this.close(ps, cn, rs);
        }
        return null;
    }

    /**
     * 只返回一个对象
     * @param cls
     * @param cmd
     * @param p
     * @param <T>
     * @return
     */
    public <T> T ExecuteQueryOne(Class<T> cls,String cmd, Object... p) {
        LqLogUtil.log("ExecuteQueryOne cmd:" + cmd);

        DBTable dbTable = LQStart.instance.getDBTable(cls);
        if(dbTable == null){
            throw new RuntimeException(cls.getSimpleName()+" not config dbentity");
        }

        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            cn = getConnection();

            ps = cn.prepareStatement(cmd);
            SetParameter(ps, p);

            rs =  ps.executeQuery();
            if (!rs.next()){
                return null;
            }
            return getJdbcColumsArray(rs,cls,cmd,dbTable).doExuteOnlyOne(dbTable,rs,cls);
        } catch (Exception e) {
            LqLogUtil.error(this.getClass(),e);
        } finally {
            this.close(ps, cn, rs);
        }
        return null;
    }

    public <T> T ExecuteQuery(String sql, DbCallBack<T> callBack) {
        System.out.println("cmd:" + sql);
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            cn = getConnection();

            ps = cn.prepareStatement(sql);

            rs =  ps.executeQuery();
            return callBack.doInPreparedStatement(rs);
        } catch (Exception e) {
            LqLogUtil.error(this.getClass(),e);
        } finally {
            this.close(ps, cn, rs);
        }
        return null;
    }

    void SetParameter(PreparedStatement stmt, Object[] parameters)
            throws SQLException {
        if (parameters == null) {
            return;
        }
        for (int i = 0, size = parameters.length; i < size; ++i) {
            Object param = parameters[i];
            stmt.setObject(i + 1, param);
        }
    }

    void close(Statement ps, Connection cn) {
        try {
            if (ps != null) {
                ps.close();
            }

        } catch (Exception e) {
            LqLogUtil.error(this.getClass(),e);
        }
        try {
            if (cn != null) {
                cn.close();
            }
        } catch (Exception e) {
            LqLogUtil.error(this.getClass(),e);
        }
        ps = null;
        cn = null;
    }

    private void close(PreparedStatement ps, Connection cn, ResultSet rs) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            LqLogUtil.error(this.getClass(),e);
        }
        try {
            if (cn != null) {
                cn.close();
            }
        } catch (Exception e) {
            LqLogUtil.error(this.getClass(),e);
        }

        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
        }
        ps = null;
        cn = null;
        rs = null;
    }

    public int getDefaultLimitCount() {
        return 5000;
    }

}
