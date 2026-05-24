package org.example.thesisbuddy.service.impl.student;

import org.example.thesisbuddy.dao.ApplicationDao;
import org.example.thesisbuddy.dao.TopicDao;
import org.example.thesisbuddy.dao.StudentDao;
import org.example.thesisbuddy.dao.TeamDao;
import org.example.thesisbuddy.dto.student.TopicApplyDTO;
import org.example.thesisbuddy.entity.ThesisApplication;
import org.example.thesisbuddy.entity.TeamMember;
import org.example.thesisbuddy.dto.teacher.AuditDTO;
import org.example.thesisbuddy.service.student.StudentApplicationService;
import org.example.thesisbuddy.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentApplicationServiceImpl implements StudentApplicationService {

    @Autowired
    private ApplicationDao applicationDao;
    
    @Autowired
    private TopicDao topicDao;
    
    @Autowired
    private StudentDao studentDao;
    
    @Autowired
    private TeamDao teamDao;

    @Override
    public Result submitApplication(TopicApplyDTO applyDTO) {
        if (studentDao.selectById(applyDTO.getStudentId()) == null) {
            return Result.error("学生不存在");
        }
        
        if (topicDao.selectById(applyDTO.getTopicId()) == null) {
            return Result.error("选题不存在");
        }
        
        ThesisApplication existingApplication = applicationDao.selectByStudentId(applyDTO.getStudentId());
        if (existingApplication != null && existingApplication.getApplicationStatus() != null && existingApplication.getApplicationStatus() != 0) {
            return Result.error("你已经提交过选题申请，不能重复提交");
        }
        
        int remaining = topicDao.selectRemainingCount(applyDTO.getTopicId());
        if (remaining <= 0) {
            return Result.error("该选题人数已满");
        }
        
        // 判断是单人申请还是多人申请
        if ("team".equals(applyDTO.getApplyType()) && applyDTO.getTeamId() != null) {
            // 多人申请：为队伍中的所有成员创建预占记录
            List<TeamMember> members = teamDao.selectMembersByTeamId(applyDTO.getTeamId());
            
            if (members == null || members.isEmpty()) {
                return Result.error("队伍成员为空");
            }
            
            // 检查名额是否足够
            if (remaining < members.size()) {
                return Result.error("该选题剩余名额不足，需要" + members.size() + "个名额，但只剩" + remaining + "个");
            }
            
            // 释放多余的预占名额（保留队伍创建时的预占，释放超出实际成员数的部分）
            applicationDao.releaseTeamReservedSlots(applyDTO.getTeamId(), applyDTO.getTopicId(), 
                "队伍提交申请，释放多余预占名额");
            
            // 为每个队员创建预占记录（如果还没有的话）
            for (TeamMember member : members) {
                // 检查该学生是否已经有预占记录
                ThesisApplication memberApplication = applicationDao.selectByStudentId(member.getStudentId());
                if (memberApplication != null && memberApplication.getApplicationStatus() != null && memberApplication.getApplicationStatus() != 0) {
                    // 如果已经有预占记录，跳过（可能是创建队伍时已经预占了）
                    continue;
                }
                
                ThesisApplication application = new ThesisApplication();
                application.setStudentId(member.getStudentId());
                application.setTopicId(applyDTO.getTopicId());
                application.setTeamId(applyDTO.getTeamId());
                application.setApplyType("team");
                application.setApplicationStatus(1); // 1表示预占状态
                application.setIsLocked(0);
                application.setApplyTime(LocalDateTime.now());
                
                applicationDao.insert(application);
            }
            
            return Result.success("申请提交成功，已为队伍" + members.size() + "名成员保留名额至明天00:00");
        } else {
            // 单人申请：只为当前学生创建预占记录
            ThesisApplication application = new ThesisApplication();
            application.setStudentId(applyDTO.getStudentId());
            application.setTopicId(applyDTO.getTopicId());
            application.setTeamId(applyDTO.getTeamId());
            application.setApplyType(applyDTO.getApplyType());
            application.setApplicationStatus(1);
            application.setIsLocked(0);
            application.setApplyTime(LocalDateTime.now());
            
            int rows = applicationDao.insert(application);
            return rows > 0 ? Result.success("申请提交成功，名额已保留至明天00:00") : Result.error("提交失败");
        }
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
