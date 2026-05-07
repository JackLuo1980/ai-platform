package com.aiplatform.lab.dataset;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dataset_stats")
public class DatasetStat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long datasetId;
    private String columnStatsJson;
    private String correlationJson;
    private String missingValuesJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
