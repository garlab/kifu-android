package fr.narwhals.go.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.narwhals.go.domain.Section.SColor;

public class Game implements Serializable {
    public enum State {
        OnGoing, Territories, Over, Review
    }

    public enum EndOfGame {
        None, Unknown, Standard, GiveUp, Forfeit, Time, Mushobu
    }

    // TODO: move in Rule
    public enum HandicapPlacement {
        Fixed, Free
    }

    private State state = State.OnGoing;
    private EndOfGame endOfGame = EndOfGame.None;

    public final GameInfo gameInfo;
    public final History history;
    public final Goban goban;
    public final Player[] players;
    public final GoEvent eventListener;

    public Game(int size, int handicap, GameInfo.Rule rule, Player blackPlayer, Player whitePlayer, GoEvent eventListener) {
        this.gameInfo = new GameInfo(size, handicap, rule);
        Player[] players = { blackPlayer, whitePlayer };
        this.players = players;
        Score score = new Score(gameInfo.getRule(), gameInfo.getKomi());
        this.goban = new Goban(gameInfo.getSize());
        this.history = new History(score, getHandicapStones(handicap, goban.getHoshis()));
        this.eventListener = eventListener;
    }

    public void clear() {
        goban.clear();
        history.clear();
        setState(Game.State.OnGoing);
    }

    public Player getCurrentPlayer() {
        return players[getCurrentColor().ordinal()];
    }

    public Player getNextPlayer() {
        int diff = gameInfo.getHandicap() == 0 ? 0 : 1;
        int i = (history.getRound() + diff + 1) % 2;
        return players[i];
    }

    public SColor getCurrentColor() {
        int diff = gameInfo.getHandicap() == 0 ? 0 : 1;
        int i = (history.getRound() + diff) % 2;
        return SColor.values()[i];
    }

    public State getState() {
        return state;
    }

    public String getResult() {
        String winner = getCurrentColor() == SColor.BLACK ? "W+" : "B+";
        switch (endOfGame) {
            case None:
            case Mushobu:
                return "Void";
            case Unknown:
                return "?";
            case Standard:
                return history.score.getResult();
            case GiveUp:
                return winner + "R";
            case Forfeit:
                return winner + "F";
            case Time:
                return winner + "T";
        }
        throw new IllegalStateException("Unsupported endOfGane: " + endOfGame);
    }

    public void setState(State newState) {
        if (this.state != newState) {
            State oldState = this.state;
            this.state = newState;
            eventListener.onStateChange(oldState, newState);
        }
    }

    public void setEndOfGame(EndOfGame endOfGame) {
        this.endOfGame = endOfGame;
        switch (endOfGame) {
            case Standard:
            case GiveUp:
            case Mushobu:
                setState(State.Over);
        }
    }

    public boolean canMove(Stone stone) {
        return stone.isMoveValid() &&
                !history.getCurrentMove().getKo().equals(stone.getPoint());
    }

    public void move(Stone stone) {
        history.move(stone);
        eventListener.onNextTurn();
    }

    public void tryMove(Point point) {
        Stone stone = new Stone(getCurrentColor(), point, goban);
        if (canMove(stone)) {
            move(stone);
        }
    }

    public void pass() {
        Stone pass = new Stone(getCurrentColor(), Point.PASS, goban);
        if (history.hasPassed() && getState() == State.OnGoing) {
            setState(State.Territories);
        }
        history.pass(pass);
        eventListener.onNextTurn();
    }

    private List<Stone> getHandicapStones(int handicap, Point[] hoshis) {
        // TODO: add State Handicap in place of Ongoing when rule = chinese
        List<Stone> handicaps = new ArrayList<Stone>(gameInfo.getHandicap());
        for (Point point : getHandicaps(handicap, hoshis)) {
            handicaps.add(new Stone(SColor.BLACK, point, goban));
        }
        return handicaps;
    }

    private static Point[] getHandicaps(int handicap, Point[] hoshis) {
        Point[] handicaps = new Point[handicap > 1 ? handicap : 0];
        if (handicap > 1) {
            handicaps[0] = hoshis[2];
            handicaps[1] = hoshis[6];
        }
        if (handicap > 2) {
            handicaps[2] = hoshis[8];
        }
        if (handicap > 3) {
            handicaps[3] = hoshis[0];
        }
        if (handicap > 5) {
            handicaps[4] = hoshis[3];
            handicaps[5] = hoshis[5];
        }
        if (handicap > 7) {
            handicaps[6] = hoshis[1];
            handicaps[7] = hoshis[7];
        }
        if (handicap > 4 && handicap % 2 == 1) {
            handicaps[handicap - 1] = hoshis[4];
        }

        return handicaps;
    }
}
