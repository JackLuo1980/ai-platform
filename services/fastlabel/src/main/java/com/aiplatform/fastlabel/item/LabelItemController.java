package com.aiplatform.fastlabel.item;

import com.aiplatform.common.model.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fastlabel/items")
@RequiredArgsConstructor
public class LabelItemController {

    private final LabelItemService itemService;


    @GetMapping
    public R<List<LabelItem>> list(@RequestParam(required = false) Long taskId,
                                   @RequestParam(required = false) String status) {
        return R.ok(itemService.listItems(taskId, status));
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> getById(@PathVariable Long id) {
        return R.ok(itemService.getItemDetail(id));
    }

    @PostMapping("/{id}/annotate")
    public R<LabelItem> annotate(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return R.ok(itemService.annotate(id, body.get("annotation")));
    }

    @PostMapping("/{id}/review")
    public R<LabelItem> review(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String action = body.get("action");
        String comment = body.getOrDefault("comment", "");
        String reviewedBy = body.getOrDefault("reviewedBy", "system");
        return R.ok(itemService.review(id, action, comment, reviewedBy));
    }

    @GetMapping("/task/{taskId}")
    public R<List<Map<String, Object>>> getAnnotationsByTask(@PathVariable Long taskId) {
        return R.ok(itemService.getAnnotationsByTask(taskId));
    }
}
