package org.example.thesisbuddy.task;

import org.example.thesisbuddy.dao.ApplicationDao;
import org.example.thesisbuddy.dao.TeamDao;
import org.example.thesisbuddy.entity.TeamInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReserveReleaseTask {
    
    private static final Logger logger = LoggerFactory.getLogger(ReserveReleaseTask.class);
    
    @Autowired
    private ApplicationDao applicationDao;
    
    @Autowired
    private TeamDao teamDao;
    
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
    
    /**
     * 每60秒检查并清理过期队伍（创建超过30分钟且状态为forming）
     */
    @Scheduled(fixedDelay = 60000)
    public void processExpiredTeams() {
        try {
            List<TeamInfo> expiredTeams = teamDao.selectExpiredTeams();
            if (expiredTeams.isEmpty()) {
                return;
            }
            for (TeamInfo team : expiredTeams) {
                teamDao.deleteAllMembers(team.getTeamId());
                teamDao.deleteTeam(team.getTeamId());
                logger.info("清理过期队伍: teamId={}, captainId={}, createTime={}",
                    team.getTeamId(), team.getCaptainId(), team.getCreateTime());
            }
        } catch (Exception e) {
            logger.error("清理过期队伍失败", e);
        }
    }
}
