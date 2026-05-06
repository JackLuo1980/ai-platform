package com.aiplatform.lab.dataset;

import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lab/datasets")
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetService datasetService;
    private final DatasetStatsService datasetStatsService;

    @PostMapping
    public R<Dataset> create(@RequestBody Dataset dataset) {
        return R.ok(datasetService.create(dataset));
    }

    @PutMapping("/{id}")
    public R<Dataset> update(@PathVariable String id, @RequestBody Dataset dataset) {
        dataset.setId(id);
        return R.ok(datasetService.update(dataset));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        datasetService.delete(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<Dataset> get(@PathVariable String id) {
        return R.ok(datasetService.getById(id));
    }

    @GetMapping
    public R<PageResult<Dataset>> list(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       @RequestParam(required = false) String tenantId,
                                       @RequestParam(required = false) String projectId) {
        return R.ok(datasetService.list(tenantId, projectId, page, size));
    }

    @PostMapping("/upload")
    public R<Dataset> upload(@RequestParam String tenantId,
                             @RequestParam String projectId,
                             @RequestParam String name,
                             @RequestParam(required = false) String description,
                             @RequestParam("file") MultipartFile file) {
        return R.ok(datasetService.upload(tenantId, projectId, name, description, file));
    }

    @GetMapping("/{id}/preview")
    public R<PageResult<Map<String, String>>> preview(@PathVariable String id,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return R.ok(datasetService.preview(id, page, size));
    }

    @GetMapping("/{id}/stats")
    public R<List<DatasetStat>> stats(@PathVariable String id,
                                      @RequestParam(required = false) Integer version) {
        return R.ok(datasetStatsService.getStats(id, version));
    }

    @PostMapping("/{id}/stats/compute")
    public R<List<DatasetStat>> computeStats(@PathVariable String id,
                                              @RequestParam(defaultValue = "1") int version) {
        return R.ok(datasetStatsService.computeStats(id, version));
    }

    @GetMapping("/{id}/versions")
    public R<List<DatasetVersion>> listVersions(@PathVariable String id) {
        return R.ok(datasetService.listVersions(id));
    }
}
