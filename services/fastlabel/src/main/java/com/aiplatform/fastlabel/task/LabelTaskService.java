package com.aiplatform.fastlabel.task;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.fastlabel.dataset.LabelDatasetMapper;
import com.aiplatform.fastlabel.item.LabelItem;
import com.aiplatform.fastlabel.item.LabelItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabelTaskService {

    private final LabelTaskMapper taskMapper;
    private final LabelItemMapper itemMapper;

    @Transactional
    public LabelTask create(LabelTask task) {
        task.setStatus("CREATED");
        task.setTotalItems(0);
        task.setLabeledItems(0);
        taskMapper.insert(task);

        if (task.getDatasetId() != null) {
            distributeItems(task);
        }

        return task;
    }

    public PageResult<LabelTask> list(int page, int size, String status, Long datasetId) {
        Page<LabelTask> pageReq = new Page<>(page + 1, size);
        LambdaQueryWrapper<LabelTask> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(LabelTask::getStatus, status);
        }
        if (datasetId != null) {
            wrapper.eq(LabelTask::getDatasetId, datasetId);
        }
        wrapper.orderByDesc(LabelTask::getCreatedAt);
        Page<LabelTask> result = taskMapper.selectPage(pageReq, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public LabelTask getById(Long id) {
        return taskMapper.selectById(id);
    }

    @Transactional
    public LabelTask assign(Long taskId, String assignedTo) {
        LabelTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("Task not found");
        }
        task.setAssignedTo(assignedTo);
        if ("CREATED".equals(task.getStatus())) {
            task.setStatus("IN_PROGRESS");
        }
        taskMapper.updateById(task);

        LambdaQueryWrapper<LabelItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelItem::getTaskId, taskId);
        wrapper.eq(LabelItem::getStatus, "PENDING");
        List<LabelItem> pendingItems = itemMapper.selectList(wrapper);
        for (LabelItem item : pendingItems) {
            item.setAssignedTo(assignedTo);
            itemMapper.updateById(item);
        }

        return task;
    }

    public PageResult<LabelItem> getItems(Long taskId, int page, int size) {
        Page<LabelItem> pageReq = new Page<>(page + 1, size);
        LambdaQueryWrapper<LabelItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelItem::getTaskId, taskId);
        wrapper.orderByAsc(LabelItem::getCreatedAt);
        Page<LabelItem> result = itemMapper.selectPage(pageReq, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Transactional
    public void updateTaskProgress(Long taskId) {
        LabelTask task = taskMapper.selectById(taskId);
        if (task == null) return;

        LambdaQueryWrapper<LabelItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelItem::getTaskId, taskId);
        long total = itemMapper.selectCount(wrapper);

        LambdaQueryWrapper<LabelItem> labeledWrapper = new LambdaQueryWrapper<>();
        labeledWrapper.eq(LabelItem::getTaskId, taskId);
        labeledWrapper.in(LabelItem::getStatus, List.of("LABELED", "REVIEWED"));
        long labeled = itemMapper.selectCount(labeledWrapper);

        task.setTotalItems((int) total);
        task.setLabeledItems((int) labeled);

        if (labeled > 0 && labeled >= total) {
            task.setStatus("REVIEWING");
        }

        taskMapper.updateById(task);
    }

    @Transactional
    public LabelTask completeTask(Long taskId) {
        LabelTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("Task not found");
        }
        task.setStatus("COMPLETED");
        taskMapper.updateById(task);
        return task;
    }

    @Transactional
    protected void distributeItems(LabelTask task) {
        LambdaQueryWrapper<LabelItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelItem::getDatasetId, task.getDatasetId());
        List<LabelItem> items = itemMapper.selectList(wrapper);

        int count = 0;
        for (LabelItem item : items) {
            if (item.getTaskId() == null) {
                item.setTaskId(task.getId());
                item.setAssignedTo(task.getAssignedTo());
                itemMapper.updateById(item);
                count++;
            }
        }

        task.setTotalItems(count);
        taskMapper.updateById(task);
    }
}
