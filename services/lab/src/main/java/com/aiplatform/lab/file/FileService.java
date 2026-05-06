package com.aiplatform.lab.file;

import com.aiplatform.lab.common.MinioService;
import com.aiplatform.lab.common.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileEntryMapper fileEntryMapper;
    private final MinioService minioService;

    public PageResult<FileEntry> list(String tenantId, String projectId, String path,
                                      int page, int size) {
        LambdaQueryWrapper<FileEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileEntry::getTenantId, tenantId);
        wrapper.eq(FileEntry::getProjectId, projectId);
        if (path != null && !path.isEmpty()) {
            wrapper.eq(FileEntry::getPath, path);
        }
        wrapper.orderByAsc(FileEntry::getIsDirectory).orderByAsc(FileEntry::getName);
        IPage<FileEntry> result = fileEntryMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public FileEntry upload(String tenantId, String projectId, String path,
                            MultipartFile file) {
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String storagePath = String.format("%s/%s/files/%s/%s",
                tenantId, projectId, path != null ? path : "", UUID.randomUUID() + "_" + originalName);

        try {
            minioService.upload(storagePath, file.getInputStream(), file.getSize(), file.getContentType());

            FileEntry entry = new FileEntry();
            entry.setTenantId(tenantId);
            entry.setProjectId(projectId);
            entry.setPath(path != null ? path : "/");
            entry.setName(originalName);
            entry.setIsDirectory(false);
            entry.setFileSize(file.getSize());
            entry.setStoragePath(storagePath);
            fileEntryMapper.insert(entry);

            return entry;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    public InputStream download(String id) {
        FileEntry entry = fileEntryMapper.selectById(id);
        if (entry == null || entry.getStoragePath() == null) {
            throw new RuntimeException("File not found");
        }
        return minioService.download(entry.getStoragePath());
    }

    public FileEntry getById(String id) {
        return fileEntryMapper.selectById(id);
    }

    public FileEntry move(String id, String newPath) {
        FileEntry entry = fileEntryMapper.selectById(id);
        if (entry == null) throw new RuntimeException("File not found");

        String newStoragePath = buildStoragePath(entry.getTenantId(), entry.getProjectId(), newPath, entry.getName());
        minioService.copyObject(entry.getStoragePath(), newStoragePath);
        minioService.delete(entry.getStoragePath());

        entry.setPath(newPath);
        entry.setStoragePath(newStoragePath);
        fileEntryMapper.updateById(entry);
        return entry;
    }

    public FileEntry copy(String id, String newPath, String newName) {
        FileEntry source = fileEntryMapper.selectById(id);
        if (source == null) throw new RuntimeException("File not found");

        String newStoragePath = buildStoragePath(source.getTenantId(), source.getProjectId(), newPath, newName);
        minioService.copyObject(source.getStoragePath(), newStoragePath);

        FileEntry copy = new FileEntry();
        copy.setTenantId(source.getTenantId());
        copy.setProjectId(source.getProjectId());
        copy.setPath(newPath);
        copy.setName(newName);
        copy.setIsDirectory(source.getIsDirectory());
        copy.setFileSize(source.getFileSize());
        copy.setStoragePath(newStoragePath);
        fileEntryMapper.insert(copy);

        return copy;
    }

    public void delete(String id) {
        FileEntry entry = fileEntryMapper.selectById(id);
        if (entry != null) {
            if (entry.getStoragePath() != null) {
                minioService.delete(entry.getStoragePath());
            }
            fileEntryMapper.deleteById(id);
        }
    }

    public FileEntry createDirectory(String tenantId, String projectId, String path, String name) {
        FileEntry dir = new FileEntry();
        dir.setTenantId(tenantId);
        dir.setProjectId(projectId);
        dir.setPath(path);
        dir.setName(name);
        dir.setIsDirectory(true);
        dir.setFileSize(0L);
        fileEntryMapper.insert(dir);
        return dir;
    }

    public String preview(String id) {
        FileEntry entry = fileEntryMapper.selectById(id);
        if (entry == null || entry.getStoragePath() == null) {
            throw new RuntimeException("File not found");
        }
        return minioService.getPresignedUrl(entry.getStoragePath(), 60);
    }

    private String buildStoragePath(String tenantId, String projectId, String path, String fileName) {
        return String.format("%s/%s/files/%s/%s", tenantId, projectId,
                path != null ? path : "", UUID.randomUUID() + "_" + fileName);
    }
}
