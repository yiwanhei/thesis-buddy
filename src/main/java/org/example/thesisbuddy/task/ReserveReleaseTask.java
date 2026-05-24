package org.example.thesisbuddy.task;

import org.example.thesisbuddy.dao.ApplicationDao;
import org.example.thesisbuddy.dao.TeamDao;
import org.example.thesisbuddy.dao.TopicDao;
import org.example.thesisbuddy.entity.TeamInfo;
import org.example.thesisbuddy.entity.ThesisApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReserveReleaseTask {
    
    private static final Logger logger = LoggerFactory.getLogger(ReserveReleaseTask.class);
    
    @Autowired
    private TeamDao teamDao;
    
    @Autowired
    private TopicDao topicDao;
    
    @Autowired
    private ApplicationDao applicationDao;
    
    private final ConcurrentHashMap<Integer, Boolean> taskMap = new ConcurrentHashMap<>();
    
    public void addReservation(Integer teamId, Integer topicId, Integer expectedMembers) {
        taskMap.put(teamId, Boolean.TRUE);
        logger.info("添加名额保留任务：队伍{}, 选题{}, 人数{}", teamId, topicId, expectedMembers);
    }
    
    public void cancelReservation(Integer teamId) {
        taskMap.remove(teamId);
        logger.info("取消名额保留任务：队伍{}", teamId);
    }
    
    @Scheduled(cron = "0 0 0 * * ?")
    public void dailyResetReservedSlots() {
        try {
            logger.info("开始执行每日00:00名额重置任务");
            int resetCount = applicationDao.batchResetReserved();
            logger.info("每日名额重置完成，共重置{}条申请记录", resetCount);
        } catch (Exception e) {
            logger.error("每日名额重置任务失败", e);
        }
    }
    
    @Scheduled(fixedDelay = 60000)
    public void processExpiredReservations() {
        try {
            List<TeamInfo> expiredTeams = teamDao.selectExpiredTeams();
            if (expiredTeams == null || expiredTeams.isEmpty()) {
                return;
            }
            
            for (TeamInfo team : expiredTeams) {
                if (team.getApplyTopic() == null) {
                    continue;
                }
                
                Integer topicId = getTopicIdByTeam(team);
                if (topicId == null) {
                    continue;
                }
                
                applicationDao.releaseReservedSlots(topicId, "队伍30分钟超时自动释放");
                taskMap.remove(team.getTeamId());
                
                logger.info("释放超时名额：队伍{}，选题{}，状态更新为forming", 
                    team.getTeamId(), topicId);
            }
        } catch (Exception e) {
            logger.error("处理过期名额释放任务失败", e);
        }
    }
    
    public void releaseSlotsOnRejection(Integer topicId, String remark) {
        try {
            int releasedCount = applicationDao.releaseReservedSlots(topicId, remark);
            logger.info("驳回后释放名额：选题{}，释放{}条记录", topicId, releasedCount);
        } catch (Exception e) {
            logger.error("驳回后释放名额失败，选题ID: {}", topicId, e);
        }
    }
    
    private Integer getTopicIdByTeam(TeamInfo team) {
        try {
            List<ThesisApplication> applications = applicationDao.selectReservedByTopic(null);
            for (ThesisApplication app : applications) {
                if (app.getTeamId() != null && app.getTeamId().equals(team.getTeamId())) {
                    return app.getTopicId();
                }
            }
        } catch (Exception e) {
            logger.error("查询队伍对应选题失败", e);
        }
        return null;
    }
}
