package com.aiplatform.lab.marketplace;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("marketplace_models")
public class MarketplaceModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long projectId;
    private Long archiveId;
    private String name;
    private String displayName;
    private String description;
    private String category;
    private String tagsJson;
    private String framework;
    private String taskType;
    private String inputSchemaJson;
    private String outputSchemaJson;
    private String demoEndpointUrl;
    private String iconUri;
    private String documentationUri;
    private String licenseType;
    private String visibility;
    private Long downloadCount;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private String approvalStatus;
    private LocalDateTime publishedAt;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
