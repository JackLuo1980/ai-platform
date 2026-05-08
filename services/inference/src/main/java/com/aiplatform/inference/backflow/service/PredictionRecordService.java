package com.aiplatform.inference.backflow.service;

import com.aiplatform.inference.backflow.entity.PredictionRecord;
import com.aiplatform.inference.backflow.mapper.PredictionRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionRecordService {

    private final PredictionRecordMapper recordMapper;

    public void save(PredictionRecord record) {
        recordMapper.insert(record);
    }

    public List<PredictionRecord> findByServiceId(Long serviceId, Boolean backflowed, int limit) {
        LambdaQueryWrapper<PredictionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PredictionRecord::getServiceId, serviceId);
        if (backflowed != null) {
            wrapper.eq(PredictionRecord::getBackflowed, backflowed);
        }
        wrapper.orderByDesc(PredictionRecord::getCreatedAt);
        wrapper.last("LIMIT " + limit);
        return recordMapper.selectList(wrapper);
    }

    public long countByServiceId(Long serviceId, Boolean backflowed) {
        LambdaQueryWrapper<PredictionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PredictionRecord::getServiceId, serviceId);
        if (backflowed != null) {
            wrapper.eq(PredictionRecord::getBackflowed, backflowed);
        }
        return recordMapper.selectCount(wrapper);
    }

    public void markBackflowed(List<Long> ids) {
        for (Long id : ids) {
            PredictionRecord record = recordMapper.selectById(id);
            if (record != null) {
                record.setBackflowed(true);
                recordMapper.updateById(record);
            }
        }
    }
}
