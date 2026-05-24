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

    @PostMapping("/submit")
    public Result submitApplication(@RequestBody TopicApplyDTO applyDTO) {
        return applicationService.submitApplication(applyDTO);
    }

    @DeleteMapping("/cancel/{applicationId}")
    public Result cancelApplication(@PathVariable int applicationId, 
                                    @RequestParam int studentId) {
        return applicationService.cancelApplication(applicationId, studentId);
    }
}
