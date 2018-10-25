package io.github.jianliu.listener;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import io.github.jianliu.Logger;
import io.github.jianliu.NamedTableEventListener;
import io.github.jianliu.TableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * 记录tableId的listener，他应该先被添加
 * Created by cdliujian1 on 2018/10/25.
 */
public class GlobalListener implements BinaryLogClient.EventListener {

    private Map<String, NamedTableEventListener> namedTableEventListenerMap = new HashMap<String, NamedTableEventListener>();


    /**
     * 注册listener
     * @param eventListener
     */
    public void register(NamedTableEventListener eventListener) {
        if (eventListener == null) {
            return;
        }
        namedTableEventListenerMap.put(getKey(eventListener.getDatabase(), eventListener.getTable()), eventListener);
    }



    public void onEvent(Event event) {
        EventHeader eventHeader = event.getHeader();
        EventType eventType = eventHeader.getEventType();

        long tableId = -1;
        if (eventType == EventType.TABLE_MAP) {
            TableMapEventData tableMapEventData = event.getData();
            TableMap.register(tableMapEventData.getTableId(), tableMapEventData);
        } else if (eventType == EventType.EXT_UPDATE_ROWS) {
            UpdateRowsEventData updateRowsEventData = event.getData();
            tableId = updateRowsEventData.getTableId();

        } else if (eventType == EventType.EXT_DELETE_ROWS) {
            DeleteRowsEventData eventData = event.getData();
            tableId = eventData.getTableId();
        } else if (eventType == EventType.EXT_WRITE_ROWS) {
            WriteRowsEventData eventData = event.getData();
            tableId = eventData.getTableId();
        }

        if (tableId != -1) {
            TableMapEventData tableMapEventData = TableMap.getTableData(tableId);
            Logger.info("update database:" + tableMapEventData.getDatabase() + " table:" + tableMapEventData.getTable());
            NamedTableEventListener listener = namedTableEventListenerMap.get(getKey(tableMapEventData.getDatabase(), tableMapEventData.getTable()));
            if (listener != null) {
                listener.onEvent(event);
            }
        }
    }

    private String getKey(String database, String table) {
        return database + "->" + table;
    }
}
