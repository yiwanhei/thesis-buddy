package org.example.thesisbuddy.controller.student;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.dto.student.TopicApplyDTO;
import org.example.thesisbuddy.service.student.StudentApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/application")
public class StudentApplicationController {

    @Autowired
    private StudentApplicationService applicationService;

    // 申请选题（创建预占，status=1）
    @PostMapping("/submit")
    public Result submitApplication(@RequestBody TopicApplyDTO applyDTO) {
        return applicationService.submitApplication(applyDTO);
    }
    
    // 正式提交（status 1→2，进入审核中）
    @PutMapping("/confirm")
    public Result confirmApplication(@RequestParam int studentId,
                                     @RequestParam(defaultValue = "0") int teamId,
                                     @RequestParam(required = false) Integer topicId) {
        return applicationService.confirmApplication(studentId, teamId, topicId);
    }

    @DeleteMapping("/cancel/{applicationId}")
    public Result cancelApplication(@PathVariable int applicationId, 
                                    @RequestParam int studentId) {
        return applicationService.cancelApplication(applicationId, studentId);
    }
}
