package org.example.thesisbuddy.controller.teacher;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.dto.teacher.AuditDTO;
import org.example.thesisbuddy.service.teacher.TeacherAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/audit")
public class TeacherAuditController {

    @Autowired
    private TeacherAuditService teacherAuditService;

    // 查看待审核列表
    @GetMapping("/pending")
    public Result getPendingList(@RequestParam int teacherId) {
        return teacherAuditService.getPendingList(teacherId);
    }

    // 通过申请
    @PostMapping("/approve")
    public Result approveApplication(@RequestBody AuditDTO auditDTO) {
        return teacherAuditService.approveApplication(auditDTO);
    }

    // 驳回申请（附理由）
    @PostMapping("/reject")
    public Result rejectApplication(@RequestBody AuditDTO auditDTO) {
        return teacherAuditService.rejectApplication(auditDTO);
    }

    // 查看审核历史
    @GetMapping("/history")
    public Result getAuditHistory(@RequestParam int teacherId) {
        return teacherAuditService.getAuditHistory(teacherId);
    }

    // 获取统计数据
    @GetMapping("/stats")
    public Result getStats(@RequestParam int teacherId) {
        return teacherAuditService.getStats(teacherId);
    }
}
