package jp.co.seiko_sol.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;



public class LogUtilsDB   {
    protected  static final Logger logger = LogManager.getLogger("BatchStatus.log");


    /**
     * JDBCAppender経由でバッチステータステーブルに記載する
     * @param batchId バッチID
     * @param processStatus　処理ステータス
     * @param processStatusDetails
     */
    public static void info(String batchId,String processStatus,String processStatusDetails) {
    	
    	ThreadContext.put("batch_id",batchId);       
    	ThreadContext.put("mission_key","ALL");     	
    	ThreadContext.put("process_status",processStatus);
    	ThreadContext.put("created_by","110000"); //TOMS固定ユーザ 
        logger.info(processStatusDetails);
        ThreadContext.clearMap();
    }
}
