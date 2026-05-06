package com.aiplatform.operation.environment;

import com.aiplatform.common.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("environments")
public class Environment extends BaseEntity {

    private String name;

    @TableField("image_id")
    private Long imageId;

    @TableField("packages_json")
    private String packagesJson;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public String getPackagesJson() {
        return packagesJson;
    }

    public void setPackagesJson(String packagesJson) {
        this.packagesJson = packagesJson;
    }
}
