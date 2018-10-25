# binlog
binlog示例，依赖mysql-binlog-connector-java

https://github.com/shyiko/mysql-binlog-connector-java

##概述
mysql-binlog-connector-java通过fork成为一个slave和master进行binlog通信，默认serverId为65535
可重新设置serverId，不可和master或正常的slave的server_id相同

调用client.connect()后，client会有一个线程来保证连接失败后，重新连接，重新消费

binlog的协议中，有mysql server版本

    ResourceBundle bundle = ResourceBundle.getBundle("jdbc");
        String prefix = "jdbc.mysql.replication.";

        BinaryLogClient client = new BinaryLogClient(bundle.getString(prefix + "master.hostname"),
                Integer.parseInt(bundle.getString(prefix + "master.port")),
                bundle.getString(prefix + "master.username"), bundle.getString(prefix + "master.password"));

        client.registerLifecycleListener(new BinaryLogClient.LifecycleListener() {
            public void onConnect(BinaryLogClient client) {
                Logger.info("connect success");
            }

            public void onCommunicationFailure(BinaryLogClient client, Exception ex) {
                ex.printStackTrace();
                Logger.info("onCommunicationFailure");
            }

            public void onEventDeserializationFailure(BinaryLogClient client, Exception ex) {
                ex.printStackTrace();
                Logger.info("onEventDeserializationFailure");
            }

            public void onDisconnect(BinaryLogClient client) {
                Logger.info("onDisconnect");
            }
        });

        GlobalListener globalListener = new GlobalListener();

        //注册table listener
        TableListener tableListener = new TableListener("local_test", "table_b");
        globalListener.register(tableListener);

        client.registerEventListener(globalListener);
        client.setServerId(1001);
        client.connect();


      SET GLOBAL server_id=2;

       SHOW VARIABLES LIKE 'log_%';
       SELECT VERSION();

       SHOW VARIABLES LIKE 'server_id';

       SHOW BINARY LOGS;

       SELECT * FROM table_a;




     查询锁
     SELECT
             p2.`HOST` 被阻塞方host,
     p2.`USER` 被阻塞方用户,
     r.trx_id 被阻塞方事务id,
             r.trx_mysql_thread_id 被阻塞方线程号,
             TIMESTAMPDIFF(
                 SECOND,
                 r.trx_wait_started,
                 CURRENT_TIMESTAMP
             ) 等待时间,
             r.trx_query 被阻塞的查询,
             l.lock_table 阻塞方锁住的表,
             m.`lock_mode` 被阻塞方的锁模式,
             m.`lock_type`  "被阻塞方的锁类型(表锁还是行锁)",
             m.`lock_index` 被阻塞方锁住的索引,
             m.`lock_space` 被阻塞方锁对象的space_id,
             m.lock_page 被阻塞方事务锁定页的数量,
             m.lock_rec 被阻塞方事务锁定行的数量,
             m.lock_data  被阻塞方事务锁定记录的主键值,
             p.`HOST` 阻塞方主机,
             p.`USER` 阻塞方用户,
             b.trx_id 阻塞方事务id,
             b.trx_mysql_thread_id 阻塞方线程号,
             b.trx_query 阻塞方查询,
             l.`lock_mode` 阻塞方的锁模式,
             l.`lock_type` "阻塞方的锁类型(表锁还是行锁)",
             l.`lock_index` 阻塞方锁住的索引,
             l.`lock_space` 阻塞方锁对象的space_id,
             l.lock_page 阻塞方事务锁定页的数量,
             l.lock_rec 阻塞方事务锁定行的数量,
             l.lock_data 阻塞方事务锁定记录的主键值,
           IF (p.COMMAND = 'Sleep', CONCAT(p.TIME,' 秒'), 0) 阻塞方事务空闲的时间
         FROM
             information_schema.INNODB_LOCK_WAITS w
         INNER JOIN information_schema.INNODB_TRX b ON b.trx_id = w.blocking_trx_id
         INNER JOIN information_schema.INNODB_TRX r ON r.trx_id = w.requesting_trx_id
         INNER JOIN information_schema.INNODB_LOCKS l ON w.blocking_lock_id = l.lock_id  AND l.`lock_trx_id`=b.`trx_id`
           INNER JOIN information_schema.INNODB_LOCKS m ON m.`lock_id`=w.`requested_lock_id` AND m.`lock_trx_id`=r.`trx_id`
         INNER JOIN information_schema. PROCESSLIST p ON p.ID = b.trx_mysql_thread_id
      INNER JOIN information_schema. PROCESSLIST p2 ON p2.ID = r.trx_mysql_thread_id
         ORDER BY
             等待时间 DESC