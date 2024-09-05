package com.maruhxn.todomon.batch.listener;

import com.maruhxn.todomon.batch.vo.MemberAchievementDTO;
import org.springframework.batch.core.ItemReadListener;

public class MultiThreadItemReadListener implements ItemReadListener<MemberAchievementDTO> {
    @Override
    public void beforeRead() {
    }

    @Override
    public void afterRead(MemberAchievementDTO item) {
        System.out.println("Thread : " + Thread.currentThread().getName() + ", read Item : " + item.getMemberId());
    }

    @Override
    public void onReadError(Exception ex) {
    }
}
