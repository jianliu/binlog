package io.github.jianliu;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import io.github.jianliu.listener.GlobalListener;
import io.github.jianliu.listener.TableListener;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * jdbc:mysql://192.168.148.95:3306/jshop_middle?connectTimeout=5000&socketTimeout=20000&autoReconnect=true&useUnicode=true&characterEncoding=UTF8
 * Created by cdliujian1 on 2018/10/25.
 */
public class Main {

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
//       String host = "192.168.148.95";
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
    }

}
