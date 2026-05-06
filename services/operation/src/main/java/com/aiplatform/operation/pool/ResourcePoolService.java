package com.aiplatform.operation.pool;

import com.aiplatform.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourcePoolService {

    @Autowired
    private ResourcePoolMapper resourcePoolMapper;

    public PageResult<ResourcePool> list(int page, int size, Long clusterId) {
        Page<ResourcePool> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<ResourcePool> wrapper = new LambdaQueryWrapper<>();
        if (clusterId != null) {
            wrapper.eq(ResourcePool::getClusterId, clusterId);
        }
        wrapper.orderByDesc(ResourcePool::getCreatedAt);
        Page<ResourcePool> result = resourcePoolMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public ResourcePool create(ResourcePool pool) {
        resourcePoolMapper.insert(pool);
        return pool;
    }

    public ResourcePool update(Long id, ResourcePool pool) {
        pool.setId(id);
        resourcePoolMapper.updateById(pool);
        return pool;
    }

    public void delete(Long id) {
        resourcePoolMapper.deleteById(id);
    }
}
