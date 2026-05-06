package com.aiplatform.lab.marketplace;

import com.aiplatform.lab.archive.ModelArchive;
import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/lab/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    @GetMapping("/models")
    public R<PageResult<MarketplaceModel>> list(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(required = false) String task,
                                                 @RequestParam(required = false) String framework,
                                                 @RequestParam(required = false) String search) {
        return R.ok(marketplaceService.list(task, framework, search, page, size));
    }

    @GetMapping("/models/{id}")
    public R<MarketplaceModel> getDetail(@PathVariable String id) {
        return R.ok(marketplaceService.getById(id));
    }

    @PostMapping("/models/{id}/add-to-lab")
    public R<ModelArchive> addToLab(@PathVariable String id, @RequestBody Map<String, String> body) {
        String tenantId = body.get("tenantId");
        String name = body.get("name");
        String version = body.getOrDefault("version", "1.0.0");
        return R.ok(marketplaceService.addToLab(id, tenantId, name, version));
    }
}
