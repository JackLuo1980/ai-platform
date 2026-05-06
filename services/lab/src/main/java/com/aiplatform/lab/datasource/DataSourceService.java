package com.aiplatform.lab.datasource;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.aiplatform.lab.common.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSourceService {

    private final DataSourceMapper dataSourceMapper;

    public DataSource create(DataSource dataSource) {
        dataSource.setStatus("ACTIVE");
        dataSourceMapper.insert(dataSource);
        return dataSource;
    }

    public DataSource update(DataSource dataSource) {
        dataSourceMapper.updateById(dataSource);
        return dataSourceMapper.selectById(dataSource.getId());
    }

    public void delete(String id) {
        dataSourceMapper.deleteById(id);
    }

    public DataSource getById(String id) {
        return dataSourceMapper.selectById(id);
    }

    public PageResult<DataSource> list(String tenantId, String type, int page, int size) {
        LambdaQueryWrapper<DataSource> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(DataSource::getTenantId, tenantId);
        if (type != null) wrapper.eq(DataSource::getType, type);
        wrapper.orderByDesc(DataSource::getCreatedAt);
        IPage<DataSource> result = dataSourceMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public Map<String, Object> testConnection(String id) {
        DataSource ds = dataSourceMapper.selectById(id);
        if (ds == null) {
            return Map.of("success", false, "message", "Data source not found");
        }
        return switch (ds.getType().toUpperCase()) {
            case "LOCAL" -> testLocal(ds);
            case "FTP" -> testFtp(ds);
            case "S3" -> testS3(ds);
            case "HDFS" -> testHdfs(ds);
            case "JDBC" -> testJdbc(ds);
            default -> Map.of("success", false, "message", "Unknown source type: " + ds.getType());
        };
    }

    private Map<String, Object> testLocal(DataSource ds) {
        JSONObject config = JSON.parseObject(ds.getConfig());
        String path = config.getString("path");
        java.io.File file = new java.io.File(path);
        boolean exists = file.exists();
        return Map.of("success", exists, "message", exists ? "Path accessible" : "Path not found: " + path);
    }

    private Map<String, Object> testFtp(DataSource ds) {
        JSONObject config = JSON.parseObject(ds.getConfig());
        try {
            String host = config.getString("host");
            int port = config.containsKey("port") ? config.getIntValue("port") : 21;
            String user = config.getString("username");
            String pass = config.getString("password");
            org.apache.commons.net.ftp.FTPClient ftp = new org.apache.commons.net.ftp.FTPClient();
            ftp.connect(host, port);
            boolean login = ftp.login(user, pass);
            ftp.disconnect();
            return Map.of("success", login, "message", login ? "FTP connected" : "FTP login failed");
        } catch (Exception e) {
            return Map.of("success", false, "message", "FTP connection failed: " + e.getMessage());
        }
    }

    private Map<String, Object> testS3(DataSource ds) {
        JSONObject config = JSON.parseObject(ds.getConfig());
        try {
            String endpoint = config.getString("endpoint");
            String accessKey = config.getString("accessKey");
            String secretKey = config.getString("secretKey");
            String bucket = config.getString("bucket");
            io.minio.MinioClient client = io.minio.MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            boolean exists = client.bucketExists(io.minio.BucketExistsArgs.builder().bucket(bucket).build());
            return Map.of("success", exists, "message", exists ? "S3 bucket accessible" : "S3 bucket not found");
        } catch (Exception e) {
            return Map.of("success", false, "message", "S3 connection failed: " + e.getMessage());
        }
    }

    private Map<String, Object> testHdfs(DataSource ds) {
        JSONObject config = JSON.parseObject(ds.getConfig());
        try {
            String uri = config.getString("uri");
            java.nio.file.Path hdfsPath = java.nio.file.Paths.get(uri);
            return Map.of("success", true, "message", "HDFS URI accepted: " + uri);
        } catch (Exception e) {
            return Map.of("success", false, "message", "HDFS check failed: " + e.getMessage());
        }
    }

    private Map<String, Object> testJdbc(DataSource ds) {
        JSONObject config = JSON.parseObject(ds.getConfig());
        try {
            String url = config.getString("url");
            String user = config.getString("username");
            String pass = config.getString("password");
            String driver = config.containsKey("driverClassName") ? config.getString("driverClassName") : "org.postgresql.Driver";
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, user, pass);
            conn.close();
            return Map.of("success", true, "message", "JDBC connected successfully");
        } catch (Exception e) {
            return Map.of("success", false, "message", "JDBC connection failed: " + e.getMessage());
        }
    }
}
