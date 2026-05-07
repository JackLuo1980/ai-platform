package com.aiplatform.lab.datasource;

import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/lab/datasources")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceService dataSourceService;

    @PostMapping
    public R<DataSource> create(@RequestBody DataSource dataSource) {
        return R.ok(dataSourceService.create(dataSource));
    }

    @PutMapping("/{id}")
    public R<DataSource> update(@PathVariable Long id, @RequestBody DataSource dataSource) {
        dataSource.setId(id);
        return R.ok(dataSourceService.update(dataSource));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        dataSourceService.delete(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<DataSource> get(@PathVariable Long id) {
        return R.ok(dataSourceService.getById(id));
    }

    @GetMapping
    public R<PageResult<DataSource>> list(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(required = false) Long tenantId) {
        return R.ok(dataSourceService.list(tenantId, type, page, size));
    }

    @PostMapping("/{id}/test")
    public R<Map<String, Object>> testConnection(@PathVariable Long id) {
        return R.ok(dataSourceService.testConnection(id));
    }
}
