package com.aiplatform.lab.marketplace;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("marketplace_model")
public class MarketplaceModel {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String name;
    private String description;
    private String framework;
    private String task;
    private String tags;
    private String config;
    private LocalDateTime createdAt;
}
