package com.aiplatform.lab.dataset;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lab_dataset_stat")
public class DatasetStat {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String datasetId;
    private Integer version;
    private String columnName;
    private String columnType;
    private String statsJson;
    private LocalDateTime createdAt;
}
