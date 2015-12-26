package fr.narwhals.go.domain;

public interface GoEvent {
    void onStateChange(Go.State oldState, Go.State newState);

    void onNextTurn();
}
