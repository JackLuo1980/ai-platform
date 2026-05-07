package com.aiplatform.lab.datasource;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("data_sources")
public class DataSource extends BaseEntity {
    private String name;
    private String type;
    private String config;
    private String description;
    private String status;
}
