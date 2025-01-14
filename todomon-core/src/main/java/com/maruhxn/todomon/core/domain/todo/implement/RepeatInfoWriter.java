package com.maruhxn.todomon.core.domain.todo.implement;

import com.maruhxn.todomon.core.domain.todo.dao.RepeatInfoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.dto.request.RepeatInfoReqItem;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RepeatInfoWriter {

    private final RepeatInfoRepository repeatInfoRepository;

    public RepeatInfo createRepeatInfo(RepeatInfoReqItem repeatInfoReqItem) {
        RepeatInfo repeatInfo = repeatInfoReqItem.toEntity();
        repeatInfoRepository.save(repeatInfo);
        return repeatInfo;
    }

    public void updateRepeatInfo(UpdateTodoReq req, Todo findTodo) {
        RepeatInfo repeatInfo = this.createRepeatInfo(req.getRepeatInfoReqItem());
        findTodo.setRepeatInfo(repeatInfo);
    }


    private void deleteRepeatInfo(RepeatInfo repeatInfo) {
        repeatInfoRepository.delete(repeatInfo);
    }


    public void removeOldRepeatInfo(Todo todo) {
        RepeatInfo oldRepeatInfo = todo.getRepeatInfo();

        if (oldRepeatInfo != null) {
            todo.setRepeatInfo(null);
            todo.setTodoInstances(null);
            this.deleteRepeatInfo(oldRepeatInfo);
        }
    }

}
