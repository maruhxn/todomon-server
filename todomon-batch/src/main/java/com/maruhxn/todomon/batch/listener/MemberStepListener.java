package com.maruhxn.todomon.batch.listener;

import com.maruhxn.todomon.batch.vo.MemberAchievementDTO;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemberStepListener {

    private final List<MemberAchievementDTO> processedMembers;

    public MemberStepListener(List<MemberAchievementDTO> processedMembers) {
        this.processedMembers = processedMembers;
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        processedMembers.clear();
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
        stepExecution.getJobExecution().getExecutionContext().put("processedMembers", processedMembers);
        return stepExecution.getExitStatus();
    }
}