package fr.narwhals.go.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.narwhals.go.domain.Section.SColor;

public class Go implements Serializable {
	public enum EndOfGame {
		None, GiveUp, Winner, Jigo, Unknow
	}

	public final Game game;
	public final History history;
	public final Goban goban;
	public final Player[] players;

	public Go(Game game, Player[] players) {
		this.game = game;
		this.players = players;
		Score score = new Score(game.getRule(), game.getKomi());
		this.goban = new Goban(game.getSize());
		this.history = new History(score, getHandicaps());
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
}
