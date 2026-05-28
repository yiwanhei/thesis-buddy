package org.example.thesisbuddy.service.impl.teacher;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.dao.ApplicationDao;
import org.example.thesisbuddy.dao.TeacherDao;
import org.example.thesisbuddy.dao.TeamDao;
import org.example.thesisbuddy.dto.teacher.AuditDTO;
import org.example.thesisbuddy.entity.TeacherAccount;
import org.example.thesisbuddy.entity.ThesisApplication;
import org.example.thesisbuddy.service.teacher.TeacherAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class TeacherAuditServiceImpl implements TeacherAuditService {

    private static final Logger logger = LoggerFactory.getLogger(TeacherAuditServiceImpl.class);

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private TeacherDao teacherDao;

    @Autowired
    private TeamDao teamDao;

    @Override
    public Result getPendingList(int teacherId) {
        return Result.success(applicationDao.selectPendingByTeacherWithDetails(teacherId));
    }

    @Override
    public Result getAuditHistory(int teacherId) {
        return Result.success(applicationDao.selectHistoryByTeacherWithDetails(teacherId));
    }

    @Override
    @Transactional
    public Result approveApplication(AuditDTO auditDTO) {
        // 验证申请是否存在
        ThesisApplication application = applicationDao.selectById(auditDTO.getApplicationId());
        if (application == null) return Result.error("申请记录不存在");
        // v2规则：教师审核status=2（审核中）的记录
        if (application.getApplicationStatus() != 2) return Result.error("该申请当前状态不是审核中，无法通过");

        // 验证教师身份
        TeacherAccount teacher = teacherDao.selectById(auditDTO.getReviewerId());
        if (teacher == null) return Result.error("审核人不存在");

        // 更新状态为已通过（3）
        auditDTO.setApplicationStatus(3);
        int rows = applicationDao.updateStatus(auditDTO);
        if (rows <= 0) return Result.error("操作失败");

        // 如果是团队申请，批量通过所有队员，更新队伍状态为confirmed
        if ("team".equals(application.getApplyType()) && application.getTeamId() != null) {
            try {
                // 批量更新其他队员的status=2→3
                applicationDao.batchApproveByTeam(application.getTeamId(),
                    auditDTO.getReviewerId(), "教师审核通过");
                // 更新队伍状态为confirmed
                teamDao.updateTeamStatus(application.getTeamId(), "confirmed");
                logger.info("团队审核通过: teamId={}", application.getTeamId());
            } catch (Exception e) {
                logger.error("团队审核后续处理失败 teamId={}", application.getTeamId(), e);
            }
        }

        return Result.success("已通过");
    }

    @Override
    @Transactional
    public Result rejectApplication(AuditDTO auditDTO) {
        // 验证申请是否存在
        ThesisApplication application = applicationDao.selectById(auditDTO.getApplicationId());
        if (application == null) return Result.error("申请记录不存在");
        // v2规则：教师审核status=2（审核中）的记录
        if (application.getApplicationStatus() != 2) return Result.error("该申请当前状态不是审核中，无法驳回");

        // 验证教师身份
        TeacherAccount teacher = teacherDao.selectById(auditDTO.getReviewerId());
        if (teacher == null) return Result.error("审核人不存在");

        if (auditDTO.getRemark() == null || auditDTO.getRemark().isEmpty()) {
            return Result.badRequest("驳回理由不能为空");
        }

        // 更新状态为已驳回（0），释放所有名额
        auditDTO.setApplicationStatus(0);
        int rows = applicationDao.updateStatus(auditDTO);
        if (rows <= 0) return Result.error("操作失败");

        // 如果是团队申请，释放该队伍所有成员的名额
        if ("team".equals(application.getApplyType()) && application.getTeamId() != null) {
            try {
                applicationDao.releaseTeamAllReservations(application.getTeamId(), "教师驳回，释放所有名额");
                logger.info("驳回后释放团队名额: teamId={}", application.getTeamId());
            } catch (Exception e) {
                logger.error("释放团队名额失败 teamId={}", application.getTeamId(), e);
            }
        }

        return Result.success("已驳回");
    }

    @Override
    public Result getStats(int teacherId) {
        Map<String, Object> stats = new HashMap<>();
        long pendingCount = applicationDao.selectPendingByTeacherWithDetails(teacherId).size();
        long historyCount = applicationDao.selectHistoryByTeacherWithDetails(teacherId).size();
        stats.put("pendingCount", pendingCount);
        stats.put("historyCount", historyCount);
        return Result.success(stats);
    }
}
