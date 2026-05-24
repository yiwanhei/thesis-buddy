package org.example.thesisbuddy.task;

import org.example.thesisbuddy.dao.ApplicationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReserveReleaseTask {
    
    private static final Logger logger = LoggerFactory.getLogger(ReserveReleaseTask.class);
    
    @Autowired
    private ApplicationDao applicationDao;
    
    /**
     * 每60秒检查并释放超时预占记录（reserve_until已过期的status=1记录）
     */
    @Scheduled(fixedDelay = 60000)
    public void processExpiredReservations() {
        try {
            int releasedCount = applicationDao.batchReleaseExpiredReservations();
            if (releasedCount > 0) {
                logger.info("释放超时预占记录{}条", releasedCount);
            }
        } catch (Exception e) {
            logger.error("处理过期预占释放任务失败", e);
        }
    }
    
    /**
     * 教师驳回后释放该题目的所有预占记录
     */
    public void releaseSlotsOnRejection(Integer topicId, String remark) {
        try {
            applicationDao.releaseReservedSlots(topicId, remark);
            logger.info("驳回后释放名额：选题{}", topicId);
        } catch (Exception e) {
            logger.error("驳回后释放名额失败，选题ID: {}", topicId, e);
        }
    }
}
