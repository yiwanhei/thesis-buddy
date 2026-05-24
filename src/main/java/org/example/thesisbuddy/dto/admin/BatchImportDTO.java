package org.example.thesisbuddy.dto.admin;

import lombok.Data;
import java.util.List;

@Data//批量导入
public class BatchImportDTO {
    private List<StudentImportItem> students; // 学生列表

    @Data
    public static class StudentImportItem {
        private String account;    // 学号
        private String realName;   // 姓名
        private String gender;     // 性别
        private Integer classId;   // 班级 ID
    }
}
