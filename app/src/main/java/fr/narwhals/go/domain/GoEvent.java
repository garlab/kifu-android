package fr.narwhals.go.domain;

public interface GoEvent {
    void onStateChange(Game.State oldState, Game.State newState);

    void onNextTurn();

    void onScoreChange();
}
