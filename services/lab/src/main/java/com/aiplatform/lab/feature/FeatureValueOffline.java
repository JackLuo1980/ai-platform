package com.aiplatform.lab.feature;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feature_values_offline")
public class FeatureValueOffline {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private String entityKey;
    private String featureJson;
    private LocalDateTime eventTimestamp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
