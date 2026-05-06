package com.aiplatform.operation.environment;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.operation.image.ImageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentService {

    @Autowired
    private EnvironmentMapper environmentMapper;

    @Autowired
    private ImageMapper imageMapper;

    public PageResult<Environment> list(int page, int size, Long imageId) {
        Page<Environment> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<Environment> wrapper = new LambdaQueryWrapper<>();
        if (imageId != null) {
            wrapper.eq(Environment::getImageId, imageId);
        }
        wrapper.orderByDesc(Environment::getCreatedAt);
        Page<Environment> result = environmentMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public Environment create(Environment environment) {
        environmentMapper.insert(environment);
        return environment;
    }

    public Environment update(Long id, Environment environment) {
        environment.setId(id);
        environmentMapper.updateById(environment);
        return environment;
    }

    public void delete(Long id) {
        environmentMapper.deleteById(id);
    }
}
