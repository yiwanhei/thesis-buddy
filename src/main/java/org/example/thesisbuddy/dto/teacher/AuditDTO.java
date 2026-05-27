package org.example.thesisbuddy.dto.teacher;

import lombok.Data;

@Data
public class AuditDTO {
    private int applicationId;
    private int teacherId;
    private Integer reviewerId;
    private String action;
    private String remark;
    private Integer applicationStatus;
}
