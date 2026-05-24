package org.example.thesisbuddy.service.impl.teacher;

import org.example.thesisbuddy.dao.ApplicationDao;
import org.example.thesisbuddy.dao.TopicDao;
import org.example.thesisbuddy.dto.teacher.AuditDTO;
import org.example.thesisbuddy.entity.ThesisApplication;
import org.example.thesisbuddy.service.teacher.TeacherAuditService;
import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.task.ReserveReleaseTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeacherAuditServiceImpl implements TeacherAuditService {

    @Autowired
    private ApplicationDao applicationDao;
    
    @Autowired
    private TopicDao topicDao;
    
    @Autowired
    private ReserveReleaseTask reserveReleaseTask;

    @Override
    public Result getPendingList(int teacherId) {
        return Result.success(applicationDao.selectPendingByTeacher(teacherId));
    }

    @Override
    public Result approveApplication(AuditDTO auditDTO) {
        ThesisApplication application = applicationDao.selectById(auditDTO.getApplicationId());
        if (application == null) {
            return Result.error("申请记录不存在");
        }
        
        auditDTO.setApplicationStatus(2);
        auditDTO.setTeacherId(auditDTO.getTeacherId());
        
        int rows = applicationDao.updateStatus(auditDTO);
        return rows > 0 ? Result.success("审核通过") : Result.error("操作失败");
    }

    @Override
    public Result rejectApplication(AuditDTO auditDTO) {
        ThesisApplication application = applicationDao.selectById(auditDTO.getApplicationId());
        if (application == null) {
            return Result.error("申请记录不存在");
        }
        
        auditDTO.setApplicationStatus(0);
        auditDTO.setTeacherId(auditDTO.getTeacherId());
        
        int rows = applicationDao.updateStatus(auditDTO);
        
        if (rows > 0) {
            reserveReleaseTask.releaseSlotsOnRejection(
                application.getTopicId(), 
                "审核驳回：" + (auditDTO.getRemark() != null ? auditDTO.getRemark() : "")
            );
            return Result.success("已驳回，名额已释放");
        }
        
        return Result.error("操作失败");
    }

    @Override
    public Result getAuditHistory(int teacherId) {
        return Result.success(applicationDao.selectHistoryByTeacher(teacherId));
    }
}
