package org.example.thesisbuddy.service.student;

import org.example.thesisbuddy.dto.student.TopicApplyDTO;
import org.example.thesisbuddy.common.Result;

public interface StudentApplicationService {
    // 申请选题（创建预占，status=1）
    Result submitApplication(TopicApplyDTO applyDTO);
    
    // 正式提交（status 1→2，进入审核中）
    Result confirmApplication(int studentId, int teamId, Integer topicId);

    // 取消申请
    Result cancelApplication(int applicationId, int studentId);
}
