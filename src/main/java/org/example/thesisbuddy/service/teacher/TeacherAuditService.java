package org.example.thesisbuddy.service.teacher;

import org.example.thesisbuddy.dto.teacher.AuditDTO;
import org.example.thesisbuddy.common.Result;

public interface TeacherAuditService {
    // 查看待审核列表
    Result getPendingList(int teacherId);

    // 通过申请
    Result approveApplication(AuditDTO auditDTO);

    // 驳回申请
    Result rejectApplication(AuditDTO auditDTO);

    // 查看审核历史
    Result getAuditHistory(int teacherId);

    // 获取统计数据（待审数、历史数）
    Result getStats(int teacherId);
}
