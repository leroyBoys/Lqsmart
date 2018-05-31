# Lqsmart

db:
 注解：LQDBTable 设置表名（默认当前classname）

       LQField：设置字段（默认与fieldName一致）
       DBRelations:关联数据关系（DBRelation 集合）
            DBRelation：关联数据关系详情:colum：当前查询除的colum名字，targetColum：表示colum在关联对象内的对应colum值

       注：LQField是枚举类型的话，如果是普通枚举，直接执行Enum.value(class,name)方法，效率不高；
           也可以直接让枚举类实现DBEnum接口 从而灵活控制enum再数据中对应的值，可以用int，也可以用string（高效）
           目前只支持实体类的查询，只提供实体类单表更新，添加不提供关联表的更新，添加，需要的时候单独写sql

           如果java数据类型与数据库类型不一致时候，可以通过设置ConvertDBType 进行强制转换
                EnumNumber：枚举转换成id存储
                BoolNumber：boolean 转换成数字
                DateNumber:日期转换成时间戳
            也可以自定义格式，只需要设置convertDBTypeClass:为自定义的转换类，实现接口ConvertDefaultDBType
                    formatToDbData(Object o):将对象转换为存入数据库的格式，o为该属性在对象中的值
                    formatFromDb(Class cls, String value):从数据库（redis）中读取的数据转换为对象的值，cls为该field的class，value:对应数据库中的值，返回该对象的值
                    例如：某字段date(String.Date)类对应数据库中bigint
                          formatToDbData(Object o):o即String.Date,此处只需要写成o.getTime()(伪代码);即可
                          formatFromDb(Class cls, String value):重写为new Date(Long.valueOf(value))
redis
    注解:redisCache
        keyFieldName:作为唯一值的method Name（默认getId）
        expire:有效期时间（秒）,0：无限期(默认)
        expireAt:到期时间（时间戳）,0：无限期(默认)(如果两个都大于0则取最小的时间为有效期)
        Type:map:即对应redis的hastmap结构（默认值，效率略低）
            Serialize：对应redis的set结构存储序列化二进制数据(人为识别困难)

     LQField：设置字段（默认与fieldName一致）
              redisSave:存入redis时候是否存入（只对Type=map类型有效）

配置表
    支持一主多备 以及集群配置（根据node获得相关链接）
        私有参数>公共参数
    注datasource.db.type=引用数据源类完全限定名如：com.alibaba.druid.pool.DruidDataSource
    第一位：固定值datasource，表示数据源
    第二位：dbType 目前支持db,redis
    第三位:node 节点名称或者序号，一个唯一标示，可省略（标示所有通用）
    第四位:master、slave、global类型;
           master：主节点
           slave或者slave_01,slave_one:slave节点
           global：该节点公共配置
                  global.slowOpen:true/false是否开启慢查询（默认关闭），开启的话可以使用getSlowSlave（）操作速度比较慢的查询
                  global.sentinels:ip:port集合（用于故障监控和切换主从使用，如果没有则故障不能保证快速切换正确服务器和恢复错误服务器），如：192.168.11.129:26379,192.168.11.129:36379,192.168.11.129:46379
    第五位:属性值，根据采用的数据库连接池自行定义
    例如：
    master配置:datasource.db.name=tome datasource.db.master.name=tome datasource.db.01.master.name=tome
    slave配置: datasource.db.name=tome datasource.db.slave.name=tome   datasource.db.01.slave_01.name=tome


第三方jar包
jedis.2.9.0
mysql-connector-java
数据库连接池由type决定（参考配置信息说明）
如果需要序列化二进制
protostuff-core（1.1.3）
protostuff-runtime（1.1.3）

使用方式：
第一步扫描：LQStart.san("com","org");
第二步初始化db： LQStart.initConnectionManager(Properties properties);
第三部使用 LQStart.getJdbcManager().getMaster/slave().Exceute()