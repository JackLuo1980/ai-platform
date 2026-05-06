package com.aiplatform.operation.cluster;

import com.aiplatform.common.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("clusters")
public class Cluster extends BaseEntity {

    private String name;

    @TableField("api_server_url")
    private String apiServerUrl;

    @TableField("token_secret")
    private String tokenSecret;

    private String status;

    @TableField("node_info_json")
    private String nodeInfoJson;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiServerUrl() {
        return apiServerUrl;
    }

    public void setApiServerUrl(String apiServerUrl) {
        this.apiServerUrl = apiServerUrl;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNodeInfoJson() {
        return nodeInfoJson;
    }

    public void setNodeInfoJson(String nodeInfoJson) {
        this.nodeInfoJson = nodeInfoJson;
    }
}
