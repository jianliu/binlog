package io.github.jianliu;

import com.github.shyiko.mysql.binlog.BinaryLogClient;

/**
 * 知道table的listener
 * Created by cdliujian1 on 2018/10/25.
 */
public interface NamedTableEventListener extends BinaryLogClient.EventListener {

    /**
     * table名称
     *
     * @return
     */
    String getTable();

    /**
     * 数据库名称
     *
     * @return
     */
    String getDatabase();

}
