package com.maruhxn.todomon.core.domain.todo.implement;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TodoModifier {

    private final RewardManager rewardManager;
    private final RepeatInfoWriter repeatInfoWriter;
    private final TodoInstanceCreator todoInstanceCreator;

    public void updateTodo(Todo todo, UpdateTodoReq req) {
        todo.update(req);

        if (req.getRepeatInfoReqItem() != null) {
            repeatInfoWriter.updateRepeatInfo(req, todo);
            todoInstanceCreator.generateAndSaveInstances(todo);
        }
    }

    public void doStatusUpdateProcess(Todo todo, boolean isDone, Member findMember) {
        if (!todo.isDone() && isDone) {
            todo.updateIsDone(true);
            rewardManager.reward(findMember, 1);
        } else if (todo.isDone() && !isDone) {
            todo.updateIsDone(false);
            rewardManager.withdrawReward(findMember, 1);
        }
    }
}
