package org.example.thesisbuddy.dto.student;

import lombok.Data;
import java.util.List;

@Data
public class TopicApplyDTO {
    private int studentId;
    private int topicId;
    private String applyType;
    private Integer teamId;
    private String applyReason;
    private String resultForm;
    private String teacherName;
    private List<MemberTaskDTO> memberTasks;

    @Data
    public static class MemberTaskDTO {
        private int studentId;
        private String task;
    }
}
