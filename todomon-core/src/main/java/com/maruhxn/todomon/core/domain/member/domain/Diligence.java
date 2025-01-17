package com.maruhxn.todomon.core.domain.member.domain;

import com.maruhxn.todomon.core.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;

import static com.maruhxn.todomon.core.global.common.Constants.GAUGE_INCREASE_RATE;
import static com.maruhxn.todomon.core.global.common.Constants.REWARD_UNIT;

@Entity
@Getter
public class Diligence extends BaseEntity {

    @Column(nullable = false)
    @ColumnDefault("0")
    private double gauge = 0;

    @Column(nullable = false)
    @ColumnDefault("1")
    private int level = 1;

    @OneToOne(mappedBy = "diligence", fetch = FetchType.LAZY)
    private Member member;

    public void setMember(Member member) {
        this.member = member;
    }

    public void levelUp(int level) {
        this.level += level;
    }

    public void increaseGaugeByTodoCnt(int todoCnt) {
        this.gauge += (GAUGE_INCREASE_RATE / this.level) * todoCnt;
        while (this.gauge >= 100) { // 게이지가 100 이상이 될 경우, 레벨이 증가한다.
            levelUp(1);
            this.rewardForLevelUp();
            this.gauge -= 100;
        }
    }

    private void rewardForLevelUp() {
        this.member.addScheduledReward(REWARD_UNIT * this.level);
    }

    public void decreaseGaugeByTodoCnt(int todoCnt) {
        this.gauge -= (GAUGE_INCREASE_RATE / this.level) * todoCnt;
        while (this.gauge < 0) {
            this.undoRewardForLevelUp();
            if (level <= 1) {
                this.gauge = 0;
            } else {
                levelUp(-1);
                this.gauge += 100;
            }
        }
    }

    private void undoRewardForLevelUp() {
        this.member.subtractScheduledReward(REWARD_UNIT * this.level);
    }
}
