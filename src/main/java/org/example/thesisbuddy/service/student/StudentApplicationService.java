package org.example.thesisbuddy.service.student;

import org.example.thesisbuddy.dto.student.TopicApplyDTO;
import org.example.thesisbuddy.common.Result;

public interface StudentApplicationService {
    // 提交选题申请
    Result submitApplication(TopicApplyDTO applyDTO);

    // 取消申请
    Result cancelApplication(int applicationId, int studentId);
}
