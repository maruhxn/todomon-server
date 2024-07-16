package com.maruhxn.todomon.domain.pet.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Random;

@Getter
@RequiredArgsConstructor
public enum Rarity {
    COMMON("#ffffff"),
    RARE("#69fe48"),
    UNIQUE("#fdf950"),
    EPIC("#3b9ea8"),
    LEGEND("#af5fc1"),
    MYTH("#ef333f"),
    CUSTOM("#000000");

    private final String color;
    private static final Random RANDOM = new Random();

    public static Rarity getRandomRarity() {
        int randomValue = RANDOM.nextInt(100);
        if (randomValue < 60) { // 60%
            return COMMON;
        } else if (randomValue < 80) { // 20%
            return RARE;
        } else if (randomValue < 90) { // 10%
            return UNIQUE;
        } else if (randomValue < 97) { // 7%
            return EPIC;
        } else if (randomValue < 99) { // 2%
            return LEGEND;
        } else {
            return MYTH; // 1%
        }
    }
}
