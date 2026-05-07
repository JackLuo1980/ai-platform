package com.aiplatform.lab.file;

import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/lab/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public R<FileEntry> upload(@RequestParam Long tenantId,
                               @RequestParam Long projectId,
                               @RequestParam(required = false) String path,
                               @RequestParam("file") MultipartFile file) {
        return R.ok(fileService.upload(tenantId, projectId, path, file));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(fileService.download(id)));
    }

    @PutMapping("/{id}/move")
    public R<FileEntry> move(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return R.ok(fileService.move(id, body.get("newPath")));
    }

    @PutMapping("/{id}/copy")
    public R<FileEntry> copy(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return R.ok(fileService.copy(id, body.get("newPath"), body.get("newName")));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<FileEntry> get(@PathVariable Long id) {
        return R.ok(fileService.getById(id));
    }

    @GetMapping
    public R<PageResult<FileEntry>> list(@RequestParam Long tenantId,
                                         @RequestParam Long projectId,
                                         @RequestParam(required = false) String path,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return R.ok(fileService.list(tenantId, projectId, path, page, size));
    }

    @PostMapping("/directory")
    public R<FileEntry> createDirectory(@RequestParam Long tenantId,
                                        @RequestParam Long projectId,
                                        @RequestParam String path,
                                        @RequestParam String name) {
        return R.ok(fileService.createDirectory(tenantId, projectId, path, name));
    }

    @GetMapping("/{id}/preview")
    public R<String> preview(@PathVariable Long id) {
        return R.ok(fileService.preview(id));
    }
}
