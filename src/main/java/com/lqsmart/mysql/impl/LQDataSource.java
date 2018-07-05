package com.lqsmart.mysql.impl;

import com.lqsmart.core.LQStart;
import com.lqsmart.entity.LQConntion;
import com.lqsmart.entity.StartInitCache;
import com.lqsmart.mysql.DbCallBack;
import com.lqsmart.mysql.SqlDataSource;
import com.lqsmart.mysql.compiler.ColumInit;
import com.lqsmart.mysql.db.DbExecutor;
import com.lqsmart.mysql.entity.*;
import com.lqsmart.util.LqLogUtil;
import com.lqsmart.util.SqlLock;

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
    private final static Map<String,String[]> cmd_jdbcColumsArraysCache = new HashMap<>();
    private DataSource dds;
    private LQDbType lqDbType;
    private boolean isConnectioned;
    protected LQDataSource(Properties properties){
        init(properties);
    }

    @Override
    public void reLoad() {

    }

    public LQDbType getLqDbType() {
        return lqDbType;
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

            if(!this.chekLqdbType(properties)){
                LqLogUtil.error("未找到对应的数据库类型,不能完成实体类与数据的自动操作，需要自行编写sql，当前自动操作只支持"+Arrays.toString(LQDbType.values()));
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

    private boolean chekLqdbType(Properties properties){
        for(Object obj:properties.keySet()){
            if (!(obj instanceof String)) {
                continue;
            }
            String key = ((String) obj);
            String str = key.toLowerCase();
            if(str.contains("url")){
                String value = properties.getProperty(key).toLowerCase();
                str = value.substring(0,value.lastIndexOf(':'));
            }else  if(str.contains("driverclass")){
                String value = properties.getProperty(key).toLowerCase();
                str = value;
            }else {
                continue;
            }

            for(LQDbType dbType:LQDbType.values()){
                if(str.contains(dbType.getAutoMark())){
                    this.lqDbType = dbType;
                    return true;
                }
            }
        }
        return false;
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

    public boolean Execute(String cmd, Object[] p) {
        Connection cn = null;
        PreparedStatement ps = null;
        try {
            cn = getConnection();
            ps = cn.prepareStatement(cmd);
            SetParameter(ps, p);
            return ps.execute();
        } catch (Exception e) {
            LqLogUtil.error(cmd+(p==null?"": Arrays.toString(p)),e);
        } finally {
            this.close(ps, cn);
        }
        return false;
    }

    public boolean ExecuteUpdate(String cmd, Object[] p) {
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

    public boolean ExecuteUpdate(String cmd) {
        Connection cn = null;
        PreparedStatement ps = null;
        try {
            cn = getConnection();
            ps = cn.prepareStatement(cmd);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LqLogUtil.error(cmd,e);
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
     * @param commitLimitCount 最大提交数量（根据mysql.cnf中 max_allowed_packet调整）如果小于等于0则为默认5000
     * @param
     * @return
     */
    public  boolean insertBatch(String tableName,List<Map<String,String>> datas,String[] columNames,int commitLimitCount) {
        return ExecuteUpdates(lqDbType.getDbExecutor().insertBatchSql(tableName,datas,columNames,commitLimitCount));
    }

    public LinkedList<Map<String, Object>> ExecuteQuerysReturnMap(String cmd){
        return ExecuteQuerysReturnMap(cmd);
    }

    public LinkedList<Map<String, Object>> ExecuteQuerysReturnMap(String cmd, Object[] p) {
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            cn = getConnection();

            ps = cn.prepareStatement(cmd);
            SetParameter(ps, p);

            rs =  ps.executeQuery();
            ResultSetMetaData rsMeta = rs.getMetaData();

            String[] columsArray = null;
            LinkedList<Map<String, Object>> rows = new LinkedList<>();
            while (rs.next()){

                if(columsArray == null){
                    columsArray  = initColumsArray(cmd,rsMeta);
                }
                HashMap<String, Object> row = new HashMap<>(columsArray.length);
                for (int i = 0, size = columsArray.length; i < size; ++i) {
                    Object value = rs.getObject(i + 1);
                    row.put(columsArray[i], value);
                }

                rows.add(row);
            }
            return rows;
        } catch (Exception e) {
            LqLogUtil.error(this.getClass(),e);
        } finally {
            this.close(ps, cn, rs);
        }
        return null;
    }

    private String cmdKey(String cmd){

        char c=cmd.charAt(0);
        if(c == ' '){
            int i = 1;
            final int length = cmd.length();
            while (i<length){
                c = cmd.charAt(i++);
                if(c != ' '){
                    break;
                }
            }
            if(c == ' '){
                throw new RuntimeException("cmd is empty!");
            }
        }

        if(c == 's' ||  c == 'S'){//sql
            int idex = cmd.indexOf(" where ");
            if(idex < 0){
                cmd = cmd.toLowerCase();
                idex = cmd.indexOf(" where ");
            }
            if(idex<0){
                return cmd;
            }

            return  cmd.substring(0,idex);
        }

        return cmd.substring(0,cmd.indexOf('('));//存储过程
    }


    private String[] initColumsArray(String cmd,ResultSetMetaData rsMeta) throws SQLException {
        cmd = cmdKey(cmd);
        String[] array = cmd_jdbcColumsArraysCache.get(cmd);
        if(array != null){
            return array;
        }

        synchronized (SqlLock.lock(cmd)){
            array = cmd_jdbcColumsArraysCache.get(cmd);
            if(array != null){
                return array;
            }

            array = new String[rsMeta.getColumnCount()];
            cmd_jdbcColumsArraysCache.put(cmd,array);
            for (int i = 0, size = array.length; i < size; ++i) {
                array[i] = rsMeta.getColumnLabel(i + 1);
            }
            return array;
        }
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

    public long ExecuteInsert(String sql) {
        return ExecuteInsert(sql,null);
    }

    public long ExecuteInsert(String sql, Object[] p) {
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
                return rs.getLong(1);
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
            DbExecutor.SqlData sqlData;
            DBTable table = LQStart.instance.getDBTable(instance.getClass());
            if(table == null){
                LqLogUtil.error(" class:"+instance.getClass().getName()+" not config db");
                return false;
            }

            Object id = table.getColumGetMap().get(table.getIdColumName()).formatToDbData(instance);
            if(id != null && Long.valueOf(id.toString())>0){

                sqlData = lqDbType.getDbExecutor().updateSql(instance,table);
                cn = getConnection();
                ps = cn.prepareStatement(sqlData.getSql());
                SetParameter(ps, sqlData.getParatmers());
                return ps.executeUpdate()>0;
            }else {
                sqlData = lqDbType.getDbExecutor().insertSql(instance,table);
            }
            cn = getConnection();
            ps = cn.prepareStatement(sqlData.getSql(), Statement.RETURN_GENERATED_KEYS);
            SetParameter(ps, sqlData.getParatmers());
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

    /**
     * 只返回一个值
     * @param cmd
     * @param p
     * @return
     */
    public Object ExecuteQueryOnlyOneValue(String cmd, Object[] p) {
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

    public Object ExecuteQueryOnlyOneValue(String cmd){
        return ExecuteQueryOnlyOneValue(cmd,null);
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
        cmd = cmdKey(cmd);
        JdbcColumsArray jdbcColumsArray = cmd_jdbcColumsArrayCache.get(cmd);
        if(jdbcColumsArray != null){
            return jdbcColumsArray;
        }

        synchronized (SqlLock.lock(cmd)) {
            jdbcColumsArray = cmd_jdbcColumsArrayCache.get(cmd);
            if (jdbcColumsArray != null) {
                return jdbcColumsArray;
            }

            jdbcColumsArray = initColumsArray(rs.getMetaData(),dbTable);
            cmd_jdbcColumsArrayCache.put(cmd, jdbcColumsArray);
            return jdbcColumsArray;
        }
    }

    public <T> List<T> ExecuteQueryList(Class<T> cls){
        DBTable dbTable = LQStart.instance.getDBTable(cls);
        if(dbTable == null){
            throw new RuntimeException(cls.getSimpleName()+" not config dbentity");
        }
        return ExecuteQueryList(dbTable,cls,lqDbType.getDbExecutor().getQuerySqlForAll(dbTable),null);
    }

    public <T> List<T> ExecuteQueryList(Class<T> cls,String cmd, Object[] p) {
        DBTable dbTable = LQStart.instance.getDBTable(cls);
        if(dbTable == null){
            return ExecuteQueryListForBase(cmd,p);
        }
        return ExecuteQueryList(dbTable,cls,cmd,p);
    }

    public <T> List<T> ExecuteQueryList(Class<T> cls,String cmd) {
        DBTable dbTable = LQStart.instance.getDBTable(cls);
        if(dbTable == null){
            // throw new RuntimeException(cls.getSimpleName()+" not config dbentity");
            return ExecuteQueryListForBase(cmd,null);
        }
        return ExecuteQueryList(dbTable,cls,cmd,null);
    }

    private  <T> List<T> ExecuteQueryListForBase(String cmd, Object[] p){
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            cn = getConnection();

            ps = cn.prepareStatement(cmd);

            SetParameter(ps, p);
            rs =  ps.executeQuery();

            List<T> list = new LinkedList<>();
            while (rs.next()){
                list.add((T) rs.getObject(1));
            }
            return list;
        } catch (Exception e) {
            LqLogUtil.error(this.getClass(),e);
        } finally {
            this.close(ps, cn, rs);
        }
        return null;
    }

    private  <T> List<T> ExecuteQueryList(DBTable dbTable,Class<T> cls,String cmd, Object[] p) {
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
     * 分页需要做缓存，对已查询过总数量不需要每次都查询
     * @param cls
     * @param page
     * @param <T>
     * @return
     */
    public <T,Page extends LQPage> Page ExecuteQueryForPage(Class<T> cls,Page  page){
        DBTable dbTable = LQStart.instance.getDBTable(cls);
        if(dbTable == null){
            throw new RuntimeException(cls.getSimpleName()+" not config dbentity");
        }

        DbExecutor dbExecutor = lqDbType.getDbExecutor();
        Long resultCount = (Long) ExecuteQueryOnlyOneValue(dbExecutor.getResultCountForQuerySql(dbTable,page),null);
        if(resultCount == null || resultCount == 0 || page.getStart()>resultCount){
            return page;
        }

        List<T> result =  ExecuteQueryList(dbTable,cls,dbExecutor.getQuerySqlForPage(dbTable,page),null);
        page.setResults(result);
        page.setAllCount(resultCount.intValue());
        return page;
    }

    public <T> T ExecuteQueryById(Class<T> cls,Object id){
        DBTable dbTable = LQStart.instance.getDBTable(cls);
        if(dbTable == null){
            throw new RuntimeException(cls.getSimpleName()+" not config dbentity");
        }
        return ExecuteQueryOne(dbTable,cls,lqDbType.getDbExecutor().getQuerySqlForId(dbTable,id),null);
    }

    public <T> void DelEntity(Class<T> cls,Object id){
        DBTable dbTable = LQStart.instance.getDBTable(cls);
        if(dbTable == null){
            throw new RuntimeException(cls.getSimpleName()+" not config dbentity");
        }
        Execute(lqDbType.getDbExecutor().getDelSqlForId(dbTable,id),null);
    }


    /**
     * 只返回一个对象
     * @param cls
     * @param cmd
     * @param p
     * @param <T>
     * @return
     */
    public <T> T ExecuteQueryOne(Class<T> cls,String cmd, Object[] p) {
        DBTable dbTable = LQStart.instance.getDBTable(cls);
        if(dbTable == null){
            throw new RuntimeException(cls.getSimpleName()+" not config dbentity");
        }

        return ExecuteQueryOne(dbTable,cls,cmd,p);
    }

    public <T> T ExecuteQueryOne(Class<T> cls,String cmd){
        return ExecuteQueryOne(cls,cmd,null);
    }

    private  <T> T ExecuteQueryOne(DBTable dbTable,Class<T> cls,String cmd, Object[] p) {
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

            JdbcColumsArray jdbcColumsArray = getJdbcColumsArray(rs,cls,cmd,dbTable);
            if(jdbcColumsArray instanceof MoreJdbcColumsArray){
                QueryResultData<T> resultData = new QueryResultData<>();
                do{
                    jdbcColumsArray.doExute(dbTable,rs,cls,resultData);
                }while (rs.next());
                return resultData.getResult().getFirst();
            }

            return jdbcColumsArray.doExuteOnlyOne(dbTable,rs,cls);
        } catch (Exception e) {
            LqLogUtil.error(this.getClass(),e);
        } finally {
            this.close(ps, cn, rs);
        }
        return null;
    }

    public <T> T ExecuteQuery(String sql, DbCallBack<T> callBack) {
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

}
