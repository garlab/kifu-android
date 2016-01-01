package fr.narwhals.go.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.narwhals.go.domain.Section.SColor;

public class Go implements Serializable {
	public enum State {
		OnGoing, Territories, Over, Review
	}

	public enum EndOfGame {
		None, Unknown, Standard, GiveUp, Forfeit, Time, Mushobu
	}

	private State state = State.OnGoing;
	private EndOfGame endOfGame = EndOfGame.None;

	public final Game game;
	public final History history;
	public final Goban goban;
	public final Player[] players;
	public final GoEvent eventListener;

	public Go(int size, int handicap, Game.Rule rule, Player blackPlayer, Player whitePlayer, GoEvent eventListener) {
		this.game = new Game(size, handicap, rule);
		Player[] players = { blackPlayer, whitePlayer };
		this.players = players;
		Score score = new Score(game.getRule(), game.getKomi());
		this.goban = new Goban(game.getSize());
		this.history = new History(score, getHandicaps());
		this.eventListener = eventListener;
	}
	
	private List<Stone> getHandicaps() {
		List<Stone> handicaps = new ArrayList<Stone>(game.getHandicap());
		for (Point point : game.getHandicaps()) {
			handicaps.add(new Stone(SColor.BLACK, point, goban));
		}
		return handicaps;
	}
	
	public void clear() {
		goban.clear();
		history.clear();
	}
	
	public Player getCurrentPlayer() {
		return players[getCurrentColor().ordinal()];
	}

	public Player getNextPlayer() {
		int diff = game.getHandicap() == 0 ? 0 : 1;
		int i = (history.getRound() + diff + 1) % 2;
		return players[i];
	}

	public SColor getCurrentColor() {
		int diff = game.getHandicap() == 0 ? 0 : 1;
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
		if (history.getCurrentMove().getStone() == Stone.PASS && getState() == State.OnGoing) {
			setState(State.Territories);
		}
		history.pass();
		eventListener.onNextTurn();
	}
}
