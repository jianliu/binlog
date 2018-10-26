package io.github.jianliu;

import com.github.shyiko.mysql.binlog.event.TableMapEventData;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取tableId -> database,table关系
 * Created by cdliujian1 on 2018/10/25.
 */
public class TableMap {

    private final static ConcurrentHashMap<Long,TableMapEventData> tableIdDbMap = new ConcurrentHashMap<Long, TableMapEventData>();

    /**
     * tableId和tableMap的关系需及时更新，因为table_id是在table_cache中临时分配的，而不是永久固定的
     * @param tableId
     * @param tableMapEventData
     */
    public static void register(Long tableId, TableMapEventData tableMapEventData){
        if(tableId != null && tableMapEventData != null) {
            tableIdDbMap.put(tableId, tableMapEventData);
        }
    }

    public static TableMapEventData getTableData(Long tableId){
        if(tableId == null){
            return null;
        }
       return tableIdDbMap.get(tableId);
    }
}
