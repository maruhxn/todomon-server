package com.maruhxn.todomon.batch.listener;

import com.maruhxn.todomon.batch.vo.MemberAchievementDTO;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;

public class MultiThreadItemWriteListener implements ItemWriteListener<MemberAchievementDTO> {

    @Override
    public void beforeWrite(Chunk<? extends MemberAchievementDTO> items) {

    }

    @Override
    public void afterWrite(Chunk<? extends MemberAchievementDTO> items) {
        System.out.println("Thread : " + Thread.currentThread().getName() + ", write Items : " + items.size());
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends MemberAchievementDTO> items) {
    }
}
