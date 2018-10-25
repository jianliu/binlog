package io.github.jianliu;

import com.github.shyiko.mysql.binlog.event.TableMapEventData;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取tableId -> database,table关系
 * Created by cdliujian1 on 2018/10/25.
 */
public class TableMap {

    private final static ConcurrentHashMap<Long,TableMapEventData> tableIdDbMap = new ConcurrentHashMap<Long, TableMapEventData>();

    public static void register(Long tableId, TableMapEventData tableMapEventData){
        if(tableId != null && tableMapEventData != null) {
            tableIdDbMap.putIfAbsent(tableId, tableMapEventData);
        }
    }

    public static TableMapEventData getTableData(Long tableId){
        if(tableId == null){
            return null;
        }
       return tableIdDbMap.get(tableId);
    }
}
