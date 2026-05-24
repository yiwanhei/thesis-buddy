package org.example.thesisbuddy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data//班级实体
public class ClassInfo {
    private Integer classId;
    private String className;
    private Integer campusId;
    private Integer majorId;
    private Integer gradeId;
    private Integer classNo;
    private LocalDateTime createTime;
}
