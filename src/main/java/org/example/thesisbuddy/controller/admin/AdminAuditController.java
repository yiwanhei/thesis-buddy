package org.example.thesisbuddy.controller.admin;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.dao.ApplicationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.example.thesisbuddy.dto.teacher.AuditDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/audit")
public class AdminAuditController {

    @Autowired
    private ApplicationDao applicationDao;

    // 分页查询申请记录
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Integer status,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size) {
        int offset = (page - 1) * size;
        Map<String, Object> data = new HashMap<>();
        data.put("list", applicationDao.selectAllApplications(status, offset, size));
        data.put("total", applicationDao.countAllApplications(status));
        data.put("page", page);
        data.put("size", size);
        return Result.success(data);
    }

    // 管理员审核（2=通过 4=驳回）
    @PostMapping("/review")
    public Result review(@RequestParam int applicationId,
                         @RequestParam int applicationStatus,
                         @RequestParam(required = false) String remark,
                         HttpServletRequest request) {
        // 1. 检查申请是否存在
        var app = applicationDao.selectById(applicationId);
        if (app == null) {
            return Result.error("申请记录不存在");
        }

        // 2. 检查当前状态是否为待审核(1)状态
        if (app.getApplicationStatus() != 1) {
            return Result.error("该申请当前状态无法审核");
        }

        // 3. 校验审核操作类型
        if (applicationStatus != 2 && applicationStatus != 4) {
            return Result.error("审核操作类型错误（2=通过, 4=驳回）");
        }

        // 4. 执行审核
        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setApplicationId(applicationId);
        auditDTO.setApplicationStatus(applicationStatus);
        auditDTO.setRemark(remark);
        // 从请求属性中获取adminId（由JwtAuthFilter设置）
        Integer adminId = (Integer) request.getAttribute("adminId");
        auditDTO.setReviewerId(adminId);

        int rows = applicationDao.updateStatus(auditDTO);
        return rows > 0 ? Result.success("审核完成") : Result.error("审核失败");
    }
}
