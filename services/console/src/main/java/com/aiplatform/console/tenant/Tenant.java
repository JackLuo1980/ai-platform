package com.aiplatform.console.tenant;

import com.aiplatform.common.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("tenants")
public class Tenant extends BaseEntity {

    private String name;
    private String status;

    @TableField("quota_json")
    private String quotaJson;

    @TableField("product_auth_json")
    private String productAuthJson;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQuotaJson() {
        return quotaJson;
    }

    public void setQuotaJson(String quotaJson) {
        this.quotaJson = quotaJson;
    }

    public String getProductAuthJson() {
        return productAuthJson;
    }

    public void setProductAuthJson(String productAuthJson) {
        this.productAuthJson = productAuthJson;
    }
}
