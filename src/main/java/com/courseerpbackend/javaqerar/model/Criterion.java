package com.courseerpbackend.javaqerar.model;

/**
 * Fixed criteria order used throughout the AHP pairwise matrix and the
 * TOPSIS decision matrix. PRICE is the only cost-type criterion (lower is
 * better); the rest are benefit-type (higher is better).
 */
public enum Criterion {
    CPU(true),
    RAM(true),
    STORAGE(true),
    GPU(true),
    PRICE(false);

    private final boolean benefit;

    Criterion(boolean benefit) {
        this.benefit = benefit;
    }

    public boolean isBenefit() {
        return benefit;
    }

    public static final Criterion[] ORDER = values();
}
