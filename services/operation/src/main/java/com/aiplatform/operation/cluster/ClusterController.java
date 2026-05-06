package com.aiplatform.operation.cluster;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/clusters")
public class ClusterController {

    @Autowired
    private ClusterService clusterService;

    @PostMapping
    public R<Cluster> register(@RequestBody Cluster cluster) {
        return R.ok(clusterService.register(cluster));
    }

    @GetMapping
    public R<PageResult<Cluster>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return R.ok(clusterService.list(page, size, status));
    }

    @GetMapping("/{id}/status")
    public R<Cluster> getStatus(@PathVariable Long id) {
        return R.ok(clusterService.getStatus(id));
    }

    @GetMapping("/{id}/nodes")
    public R<Map<String, Object>> getNodes(@PathVariable Long id) {
        return R.ok(clusterService.getNodes(id));
    }
}
