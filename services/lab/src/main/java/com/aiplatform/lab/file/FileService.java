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

    public PageResult<FileEntry> list(Long tenantId, Long projectId, String path,
                                      int page, int size) {
        LambdaQueryWrapper<FileEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileEntry::getTenantId, tenantId);
        wrapper.eq(FileEntry::getProjectId, projectId);
        if (path != null && !path.isEmpty()) {
            wrapper.eq(FileEntry::getPath, path);
        }
        wrapper.orderByAsc(FileEntry::getType).orderByAsc(FileEntry::getName);
        IPage<FileEntry> result = fileEntryMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public FileEntry upload(Long tenantId, Long projectId, String path,
                            MultipartFile file) {
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String storageKey = String.format("%s/%s/files/%s/%s",
                tenantId, projectId, path != null ? path : "", UUID.randomUUID() + "_" + originalName);

        try {
            minioService.upload(storageKey, file.getInputStream(), file.getSize(), file.getContentType());

            FileEntry entry = new FileEntry();
            entry.setTenantId(tenantId);
            entry.setProjectId(projectId);
            entry.setPath(path != null ? path : "/");
            entry.setName(originalName);
            entry.setType("FILE");
            entry.setSizeBytes(file.getSize());
            entry.setStorageKey(storageKey);
            entry.setStatus("ACTIVE");
            fileEntryMapper.insert(entry);

            return entry;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    public InputStream download(Long id) {
        FileEntry entry = fileEntryMapper.selectById(id);
        if (entry == null || entry.getStorageKey() == null) {
            throw new RuntimeException("File not found");
        }
        return minioService.download(entry.getStorageKey());
    }

    public FileEntry getById(Long id) {
        return fileEntryMapper.selectById(id);
    }

    public FileEntry move(Long id, String newPath) {
        FileEntry entry = fileEntryMapper.selectById(id);
        if (entry == null) throw new RuntimeException("File not found");

        String newStorageKey = buildStorageKey(entry.getTenantId(), entry.getProjectId(), newPath, entry.getName());
        minioService.copyObject(entry.getStorageKey(), newStorageKey);
        minioService.delete(entry.getStorageKey());

        entry.setPath(newPath);
        entry.setStorageKey(newStorageKey);
        fileEntryMapper.updateById(entry);
        return entry;
    }

    public FileEntry copy(Long id, String newPath, String newName) {
        FileEntry source = fileEntryMapper.selectById(id);
        if (source == null) throw new RuntimeException("File not found");

        String newStorageKey = buildStorageKey(source.getTenantId(), source.getProjectId(), newPath, newName);
        minioService.copyObject(source.getStorageKey(), newStorageKey);

        FileEntry copy = new FileEntry();
        copy.setTenantId(source.getTenantId());
        copy.setProjectId(source.getProjectId());
        copy.setPath(newPath);
        copy.setName(newName);
        copy.setType(source.getType());
        copy.setSizeBytes(source.getSizeBytes());
        copy.setStorageKey(newStorageKey);
        copy.setStatus("ACTIVE");
        fileEntryMapper.insert(copy);

        return copy;
    }

    public void delete(Long id) {
        FileEntry entry = fileEntryMapper.selectById(id);
        if (entry != null) {
            if (entry.getStorageKey() != null) {
                minioService.delete(entry.getStorageKey());
            }
            fileEntryMapper.deleteById(id);
        }
    }

    public FileEntry createDirectory(Long tenantId, Long projectId, String path, String name) {
        FileEntry dir = new FileEntry();
        dir.setTenantId(tenantId);
        dir.setProjectId(projectId);
        dir.setPath(path);
        dir.setName(name);
        dir.setType("DIRECTORY");
        dir.setSizeBytes(0L);
        dir.setStatus("ACTIVE");
        fileEntryMapper.insert(dir);
        return dir;
    }

    public String preview(Long id) {
        FileEntry entry = fileEntryMapper.selectById(id);
        if (entry == null || entry.getStorageKey() == null) {
            throw new RuntimeException("File not found");
        }
        return minioService.getPresignedUrl(entry.getStorageKey(), 60);
    }

    private String buildStorageKey(Long tenantId, Long projectId, String path, String fileName) {
        return String.format("%s/%s/files/%s/%s", tenantId, projectId,
                path != null ? path : "", UUID.randomUUID() + "_" + fileName);
    }
}
