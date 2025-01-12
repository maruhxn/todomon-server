package com.maruhxn.todomon.core.domain.todo.application;

import com.maruhxn.todomon.core.domain.todo.dao.RepeatInfoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.dto.request.RepeatInfoReqItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RepeatInfoService {

    private final RepeatInfoRepository repeatInfoRepository;

    public RepeatInfo createRepeatInfo(RepeatInfoReqItem repeatInfoReqItem) {
        RepeatInfo repeatInfo = repeatInfoReqItem.toEntity();
        repeatInfoRepository.save(repeatInfo);
        return repeatInfo;
    }

    public void deleteRepeatInfo(RepeatInfo repeatInfo) {
        repeatInfoRepository.delete(repeatInfo);
    }
}
