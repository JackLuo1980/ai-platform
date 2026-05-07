package com.aiplatform.lab.operator;

import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lab/operators")
@RequiredArgsConstructor
public class OperatorController {

    private final OperatorService operatorService;

    @PostMapping
    public R<Operator> create(@RequestBody Operator operator) {
        return R.ok(operatorService.create(operator));
    }

    @PutMapping("/{id}")
    public R<Operator> update(@PathVariable Long id, @RequestBody Operator operator) {
        operator.setId(id);
        return R.ok(operatorService.update(operator));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        operatorService.delete(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<Operator> get(@PathVariable Long id) {
        return R.ok(operatorService.getById(id));
    }

    @GetMapping
    public R<PageResult<Operator>> list(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(required = false) Long tenantId,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) String type) {
        return R.ok(operatorService.list(tenantId, category, type, page, size));
    }

    @PostMapping("/{id}/test")
    public R<Map<String, Object>> test(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        return R.ok(operatorService.test(id, params));
    }

    @GetMapping("/{id}/versions")
    public R<List<OperatorVersion>> listVersions(@PathVariable Long id) {
        return R.ok(operatorService.listVersions(id));
    }

    @GetMapping("/presets")
    public R<List<Map<String, Object>>> listPresets() {
        return R.ok(operatorService.listPresets());
    }
}
