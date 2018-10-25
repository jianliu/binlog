package io.github.jianliu.listener;

import com.github.shyiko.mysql.binlog.event.*;
import io.github.jianliu.Logger;
import io.github.jianliu.NamedTableEventListener;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * table更新write,delete操作Listener
 * Created by cdliujian1 on 2018/10/25.
 */
public class TableListener implements NamedTableEventListener {

    private String database;

    private String table;

    private MySQLConnection master;

    private List<String> columnNames;

    public TableListener(final String database, final String table) throws SQLException, ClassNotFoundException {
        this.database = database;
        this.table = table;
        ResourceBundle bundle = ResourceBundle.getBundle("jdbc");
        String prefix = "jdbc.mysql.replication.";
        master = new MySQLConnection(bundle.getString(prefix + "master.hostname"),
                Integer.parseInt(bundle.getString(prefix + "master.port")), this.database,
                bundle.getString(prefix + "master.username"), bundle.getString(prefix + "master.password"));

        columnNames = new ArrayList<String>();
        master.execute(new Callback<Statement>() {

            public void execute(Statement statement) throws SQLException {
                ResultSet rs = statement.executeQuery("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_name = '" + table + "' AND table_schema = '" + database + "';");
                while (rs.next()) {
                    String field = rs.getString(1);
                    columnNames.add(field);
                }
            }
        });
    }

    public String getTable() {
        return table;
    }

    public String getDatabase() {
        return database;
    }

    public void onEvent(Event event) {
        EventHeader eventHeader = event.getHeader();
        EventType eventType = eventHeader.getEventType();
        if (eventType == EventType.EXT_UPDATE_ROWS) {
            UpdateRowsEventData updateRowsEventData = event.getData();

            List<Map.Entry<Serializable[], Serializable[]>> rows = updateRowsEventData.getRows();
            for (Map.Entry<Serializable[], Serializable[]> row : rows) {
                Serializable[] key = row.getKey();
                Long id = (Long) key[0];
                Logger.info("update field:" + columnNames.get(0) + " value:" + id);
            }

        } else if (eventType == EventType.EXT_DELETE_ROWS) {
            DeleteRowsEventData deleteRowsEventData = event.getData();
            List<Serializable[]> rows = deleteRowsEventData.getRows();
            Long id = (Long) rows.get(0)[0];
            Logger.info("delete field:" + columnNames.get(0) + " value:" + id);
        } else if (eventType == EventType.EXT_WRITE_ROWS) {
            WriteRowsEventData writeRowsEventData = event.getData();
            List<Serializable[]> rows = writeRowsEventData.getRows();
            Long id = (Long) rows.get(0)[0];
            Logger.info("insert field:" + columnNames.get(0) + " value:" + id);
        }

//        Logger.info(event);
    }

    /**
     * Representation of a MySQL connection.
     */
    public static final class MySQLConnection implements Closeable {

        private final String hostname;
        private final int port;
        private final String username;
        private final String password;
        private Connection connection;

        public MySQLConnection(String hostname, int port, String database, String username, String password)
                throws ClassNotFoundException, SQLException {
            this.hostname = hostname;
            this.port = port;
            this.username = username;
            this.password = password;
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database,
                    username, password);
            execute(new Callback<Statement>() {

                public void execute(Statement statement) throws SQLException {
                    statement.execute("SET time_zone = '+00:00'");
                }
            });
        }

        public String hostname() {
            return hostname;
        }

        public int port() {
            return port;
        }

        public String username() {
            return username;
        }

        public String password() {
            return password;
        }

        public void execute(Callback<Statement> callback) throws SQLException {
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            try {
                callback.execute(statement);
                connection.commit();
            } finally {
                statement.close();
            }
        }

        public void execute(final String... statements) throws SQLException {
            execute(new Callback<Statement>() {
                public void execute(Statement statement) throws SQLException {
                    for (String command : statements) {
                        statement.execute(command);
                    }
                }
            });
        }

        public void query(String sql, Callback<ResultSet> callback) throws SQLException {
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            try {
                ResultSet rs = statement.executeQuery(sql);
                try {
                    callback.execute(rs);
                    connection.commit();
                } finally {
                    rs.close();
                }
            } finally {
                statement.close();
            }
        }

        public void close() throws IOException {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Callback used in the {@link MySQLConnection#execute(Callback)} method.
     *
     * @param <T> the type of argument
     */
    public interface Callback<T> {

        void execute(T obj) throws SQLException;
    }
}
