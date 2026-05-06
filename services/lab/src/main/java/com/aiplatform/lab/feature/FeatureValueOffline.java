package com.aiplatform.lab.feature;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lab_feature_value_offline")
public class FeatureValueOffline {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String tenantId;
    private String groupId;
    private String definitionId;
    private String entityKeyValue;
    private String value;
    private LocalDateTime eventTime;
    private LocalDateTime createdAt;
}
