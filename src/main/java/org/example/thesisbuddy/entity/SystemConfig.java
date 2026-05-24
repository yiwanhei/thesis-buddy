package org.example.thesisbuddy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SystemConfig {
    private Integer configId;
    private String configKey;         // 配置键名
    private String configValue;       // 配置值
    private String configDesc;        // 配置说明
    private LocalDateTime updateTime;
}
