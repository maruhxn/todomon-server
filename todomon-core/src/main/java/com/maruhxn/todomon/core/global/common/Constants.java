package com.maruhxn.todomon.core.global.common;

public interface Constants {

    // HEADER
    String ACCESS_TOKEN_HEADER = "Authorization";
    String REFRESH_TOKEN_HEADER = "Refresh";

    // Reward
    Integer REWARD_UNIT = 100;
    Double GAUGE_INCREASE_RATE = 10.0;
    Double REWARD_LEVERAGE_RATE = 0.1;

    // Pet
    Double PET_GAUGE_INCREASE_RATE = 2.0;

    // Pet House
    Integer MAX_PET_HOUSE_SIZE = 20;

    // Items
    String CHANGE_PET_NAME_ITEM_NAME = "펫 이름 변경권";
    String UPSERT_TITLE_NAME_ITEM_NAME = "칭호 변경권";
}
