package org.example.thesisbuddy.controller.teacher;

import org.example.thesisbuddy.common.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/audit")
public class TeacherAuditController {

    // 查看待审核列表
    @GetMapping("/pending")
    public Result getPendingList(@RequestParam int teacherId) {
        // TODO: 实现查询逻辑
        return Result.success();
    }

    // 通过申请
    @PostMapping("/approve")
    public Result approveApplication(@RequestBody Object auditDTO) {
        // TODO: 实现通过逻辑
        return Result.success();
    }

    // 驳回申请（附理由）
    @PostMapping("/reject")
    public Result rejectApplication(@RequestBody Object auditDTO) {
        // TODO: 实现驳回逻辑
        return Result.success();
    }

    // 查看审核历史
    @GetMapping("/history")
    public Result getAuditHistory(@RequestParam int teacherId) {
        // TODO: 实现查询逻辑
        return Result.success();
    }
}
