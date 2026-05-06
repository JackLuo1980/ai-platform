package com.aiplatform.lab.file;

import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/lab/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping
    public R<PageResult<FileEntry>> list(@RequestParam String tenantId,
                                         @RequestParam String projectId,
                                         @RequestParam(required = false) String path,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return R.ok(fileService.list(tenantId, projectId, path, page, size));
    }

    @PostMapping("/upload")
    public R<FileEntry> upload(@RequestParam String tenantId,
                               @RequestParam String projectId,
                               @RequestParam(required = false) String path,
                               @RequestParam("file") MultipartFile file) {
        return R.ok(fileService.upload(tenantId, projectId, path, file));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable String id) {
        FileEntry entry = fileService.getById(id);
        if (entry == null) {
            return ResponseEntity.notFound().build();
        }
        InputStream stream = fileService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + entry.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }

    @PostMapping("/{id}/move")
    public R<FileEntry> move(@PathVariable String id, @RequestBody java.util.Map<String, String> body) {
        return R.ok(fileService.move(id, body.get("path")));
    }

    @PostMapping("/{id}/copy")
    public R<FileEntry> copy(@PathVariable String id, @RequestBody java.util.Map<String, String> body) {
        return R.ok(fileService.copy(id, body.get("path"), body.get("name")));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        fileService.delete(id);
        return R.ok();
    }

    @GetMapping("/{id}/preview")
    public R<String> preview(@PathVariable String id) {
        return R.ok(fileService.preview(id));
    }

    @PostMapping("/mkdir")
    public R<FileEntry> mkdir(@RequestBody java.util.Map<String, String> body) {
        return R.ok(fileService.createDirectory(
                body.get("tenantId"), body.get("projectId"),
                body.get("path"), body.get("name")));
    }
}
