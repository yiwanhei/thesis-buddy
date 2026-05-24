package org.example.thesisbuddy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ThesisApplication {
    private Integer applicationId;
    private Integer studentId;
    private Integer topicId;
    private Integer teamId;
    private String applyType;
    private Integer applicationStatus;
    @Deprecated
    private String applyStatus;
    private Integer isLocked;
    private LocalDateTime applyTime;
    private LocalDateTime reviewTime;
    private Integer reviewerId;
    private String remark;
    private LocalDateTime reserveUntil;  // 预占到期时间
}
