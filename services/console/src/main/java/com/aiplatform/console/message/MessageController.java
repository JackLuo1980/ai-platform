package com.aiplatform.console.message;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping
    public R<PageResult<Message>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean isRead) {
        return R.ok(messageService.list(page, size, userId, isRead));
    }

    @PutMapping("/{id}/read")
    public R<Void> markRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        messageService.markRead(id, userId);
        return R.ok();
    }
}
