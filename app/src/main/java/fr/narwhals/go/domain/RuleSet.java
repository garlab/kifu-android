package fr.narwhals.go.domain;

public class RuleSet {

    public enum HandicapPlacement {
        Fixed, Free
    }

    public enum Scoring {
        Area, Territory
    }

    private final float komi;
    private final HandicapPlacement handicapPlacement;
    private final Scoring scoring;
    private final boolean superko;
    private final boolean suicideAllowed;
    private final boolean pointInSeki;

    public RuleSet(float komi, HandicapPlacement handicapPlacement, Scoring scoring, boolean superko, boolean suicideAllowed, boolean pointInSeki) {
        this.komi = komi;
        this.handicapPlacement = handicapPlacement;
        this.scoring = scoring;
        this.superko = superko;
        this.suicideAllowed = suicideAllowed;
        this.pointInSeki = pointInSeki;
    }

    public float getKomi() {
        return komi;
    }

    public HandicapPlacement getHandicapPlacement() {
        return handicapPlacement;
    }

    public Scoring getScoring() {
        return scoring;
    }

    public boolean isSuperko() {
        return superko;
    }

    public boolean isSuicideAllowed() {
        return suicideAllowed;
    }

    public boolean isPointInSeki() {
        return pointInSeki;
    }
}
