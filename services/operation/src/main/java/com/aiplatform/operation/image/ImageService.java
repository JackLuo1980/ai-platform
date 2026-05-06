package com.aiplatform.operation.image;

import com.aiplatform.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

    @Autowired
    private ImageMapper imageMapper;

    public PageResult<Image> list(int page, int size, String type, String name) {
        Page<Image> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Image::getType, type);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(Image::getName, name);
        }
        wrapper.orderByDesc(Image::getCreatedAt);
        Page<Image> result = imageMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public Image create(Image image) {
        imageMapper.insert(image);
        return image;
    }

    public Image update(Long id, Image image) {
        image.setId(id);
        imageMapper.updateById(image);
        return image;
    }

    public void delete(Long id) {
        imageMapper.deleteById(id);
    }
}
