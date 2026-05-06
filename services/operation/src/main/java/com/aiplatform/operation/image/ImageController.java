package com.aiplatform.operation.image;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping
    public R<Image> create(@RequestBody Image image) {
        return R.ok(imageService.create(image));
    }

    @PutMapping("/{id}")
    public R<Image> update(@PathVariable Long id, @RequestBody Image image) {
        return R.ok(imageService.update(id, image));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        imageService.delete(id);
        return R.ok();
    }

    @GetMapping
    public R<PageResult<Image>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name) {
        return R.ok(imageService.list(page, size, type, name));
    }
}
