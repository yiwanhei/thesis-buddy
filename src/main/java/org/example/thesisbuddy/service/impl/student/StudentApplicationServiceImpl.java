package org.example.thesisbuddy.service.impl.student;

import org.example.thesisbuddy.dao.*;
import org.example.thesisbuddy.dto.student.TopicApplyDTO;
import org.example.thesisbuddy.entity.*;
import org.example.thesisbuddy.dto.teacher.AuditDTO;
import org.example.thesisbuddy.service.student.StudentApplicationService;
import org.example.thesisbuddy.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StudentApplicationServiceImpl implements StudentApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(StudentApplicationServiceImpl.class);

    @Autowired
    private ApplicationDao applicationDao;
    
    @Autowired
    private TopicDao topicDao;
    
    @Autowired
    private StudentDao studentDao;
    
    @Autowired
    private TeamDao teamDao;
    
    @Autowired
    private ConfigDao configDao;
    
    private boolean isTopicSelectionEnabled() {
        String value = configDao.selectByKey("topic_selection_enabled");
        return "1".equals(value);
    }

    @Override
    @Transactional
    public Result submitApplication(TopicApplyDTO applyDTO) {
        // 验证学生和选题
        if (studentDao.selectById(applyDTO.getStudentId()) == null) {
            return Result.error("学生不存在");
        }
        if (topicDao.selectById(applyDTO.getTopicId()) == null) {
            return Result.error("选题不存在");
        }
        
        // 选题功能未开启时不允许申请
        if (!isTopicSelectionEnabled()) {
            return Result.error("选题功能未开启");
        }
        
        // 单人申请
        if (!"team".equals(applyDTO.getApplyType()) || applyDTO.getTeamId() == null) {
            // 检查是否已有有效申请
            ThesisApplication existing = applicationDao.selectByStudentIdAndStatus(applyDTO.getStudentId(), 1);
            if (existing != null) {
                return Result.error("你已有预占记录");
            }
            existing = applicationDao.selectByStudentIdAndStatus(applyDTO.getStudentId(), 2);
            if (existing != null) {
                return Result.error("你已提交审核，无法重复申请");
            }
            existing = applicationDao.selectByStudentIdAndStatus(applyDTO.getStudentId(), 3);
            if (existing != null) {
                return Result.error("你已通过选题");
            }
            
            // 检查剩余名额
            int remaining = topicDao.selectRemainingCount(applyDTO.getTopicId());
            if (remaining <= 0) {
                return Result.error("该选题人数已满");
            }
            
            // 创建预占记录（status=1, 30分钟）
            ThesisApplication app = new ThesisApplication();
            app.setStudentId(applyDTO.getStudentId());
            app.setTopicId(applyDTO.getTopicId());
            app.setTeamId(applyDTO.getTeamId());
            app.setApplyType(applyDTO.getApplyType());
            app.setApplicationStatus(1);
            app.setIsLocked(0);
            app.setApplyTime(LocalDateTime.now());
            app.setReserveUntil(LocalDateTime.now().plusMinutes(30));
            app.setRemark(applyDTO.getApplyReason());
            int rows = applicationDao.insert(app);
            return rows > 0 ? Result.success("申请成功，名额已保留30分钟") : Result.error("提交失败");
        }
        
        // ====== 团队申请（队长操作） ======
        TeamInfo team = teamDao.selectById(applyDTO.getTeamId());
        if (team == null) {
            return Result.error("队伍不存在");
        }
        if (!team.getCaptainId().equals(applyDTO.getStudentId())) {
            return Result.error("只有队长可以申请选题");
        }
        
        // 获取实际成员
        List<Map<String, Object>> members = teamDao.selectMembersByTeamId(applyDTO.getTeamId());
        if (members == null || members.isEmpty()) {
            return Result.error("队伍没有成员");
        }
        int actualCount = members.size();
        int expected = team.getExpectedMembers() != null ? team.getExpectedMembers() : actualCount;
        
        // 检查选题剩余名额
        TopicLibrary topic = topicDao.selectById(applyDTO.getTopicId());
        int occupied = applicationDao.countOccupiedByTopic(applyDTO.getTopicId());
        int remaining = topic.getMaxCapacity() - occupied;
        
        // 阶段一：检查剩余 ≥ max(实际, 预计)
        // 阶段二（降级）：检查剩余 ≥ 实际
        boolean useExpectedBlock;
        if (remaining >= Math.max(actualCount, expected)) {
            useExpectedBlock = true;
        } else if (remaining >= actualCount) {
            useExpectedBlock = false;
            logger.warn("选题{}名额不足预计人数，降级为仅实际成员预占：剩余={}, 实际={}, 预计={}",
                applyDTO.getTopicId(), remaining, actualCount, expected);
        } else {
            return Result.error("该选题剩余名额不足，需" + actualCount + "个，仅剩" + remaining + "个");
        }
        
        // 为队伍设置选题信息（如果未设置）
        if (team.getTopicId() == null) {
            teamDao.updateTeamInfo(applyDTO.getTeamId(), team.getTeamName(), team.getMaxMembers());
            // 需要更新apply_topic和topic_id
        }
        
        LocalDateTime deadline = LocalDateTime.now().plusMinutes(30);
        List<Integer> actualStudentIds = new ArrayList<>();
        for (Map<String, Object> m : members) {
            Integer sid = (Integer) m.get("student_id");
            if (sid != null) {
                actualStudentIds.add(sid);
            }
        }
        
        if (useExpectedBlock) {
            // 阶段一通过：为所有 actual 成员刷新 30 分钟，多余名额（占位记录）不刷新
            applicationDao.updateReserveUntilByTeamAndStudents(
                applyDTO.getTeamId(), actualStudentIds, deadline);
        } else {
            // 阶段二通过：释放所有多余占位记录，只为实际成员创建预占并设30分钟
            // 先释放不用名额（expected - actual）
            applicationDao.releaseExcessByTeam(applyDTO.getTeamId(), actualStudentIds, "名额不足，释放多余预计名额");
            // 为实际成员创建或刷新预占
            for (Integer sid : actualStudentIds) {
                ThesisApplication app = new ThesisApplication();
                app.setStudentId(sid);
                app.setTopicId(applyDTO.getTopicId());
                app.setTeamId(applyDTO.getTeamId());
                app.setApplyType("team");
                app.setApplicationStatus(1);
                app.setIsLocked(0);
                app.setApplyTime(LocalDateTime.now());
                app.setReserveUntil(deadline);
                applicationDao.insert(app);
            }
        }
        
        return Result.success("选题申请成功，名额已保留30分钟");
    }
    
    @Override
    @Transactional
    public Result confirmApplication(int studentId, int teamId, Integer topicId) {
        // 单人
        if (teamId <= 0) {
            ThesisApplication app = applicationDao.selectByStudentIdAndStatus(studentId, 1);
            if (app == null) {
                return Result.error("未找到预占记录");
            }
            AuditDTO dto = new AuditDTO();
            dto.setApplicationId(app.getApplicationId());
            dto.setApplicationStatus(2);
            dto.setTeacherId(0);
            dto.setRemark("学生提交审核");
            applicationDao.updateStatus(dto);
            return Result.success("提交成功，等待教师审核");
        }
        
        // 团队
        TeamInfo team = teamDao.selectById(teamId);
        if (team == null) {
            return Result.error("队伍不存在");
        }
        if (!team.getCaptainId().equals(studentId)) {
            return Result.error("只有队长可以提交");
        }
        
        List<Map<String, Object>> members = teamDao.selectMembersByTeamId(teamId);
        List<Integer> actualIds = new ArrayList<>();
        for (Map<String, Object> m : members) {
            Integer sid = (Integer) m.get("student_id");
            if (sid != null) actualIds.add(sid);
        }
        
        // 实际成员 → 审核中(2)
        applicationDao.batchUpdateStatusByTeam(teamId, 1, 2, "队长提交审核，等待教师审核", actualIds);
        
        // 释放多余名额
        applicationDao.releaseExcessByTeam(teamId, actualIds, "提交后释放多余名额");
        
        return Result.success("提交成功，等待教师审核");
    }

    @Override
    public Result cancelApplication(int applicationId, int studentId) {
        ThesisApplication application = applicationDao.selectById(applicationId);
        if (application == null) {
            return Result.error("申请记录不存在");
        }
        
        if (!application.getStudentId().equals(studentId)) {
            return Result.error("无权操作他人的申请");
        }
        
        if (application.getApplicationStatus() == null || application.getApplicationStatus() != 1) {
            return Result.error("该申请已审核，无法取消");
        }
        
        AuditDTO cancelDTO = new AuditDTO();
        cancelDTO.setApplicationId(applicationId);
        cancelDTO.setApplicationStatus(0);
        cancelDTO.setTeacherId(0);
        cancelDTO.setRemark("学生主动取消");
        
        int rows = applicationDao.updateStatus(cancelDTO);
        return rows > 0 ? Result.success("已取消申请，名额已释放") : Result.error("取消失败");
    }
}
