package fr.narwhals.go.domain;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import fr.narwhals.go.domain.Section.SColor;
import fr.narwhals.go.sgf.SgfWriter;

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
	
	public void export(Writer out) throws IOException {
		SgfWriter writer = new SgfWriter(out);
		writeHeader(writer);
		game.toSgf(writer);
		writePlayers(writer);
		writeResult(writer);
		history.toSgf(writer);
		writer.endGameTree();
	}
	
	public void writeHeader(SgfWriter writer) throws IOException {
		writer.beginGameTree();
		writer.beginNode();
		writer.write("GM", 1);
		writer.write("FF", 4);
		writer.write("ST", 2);
		writer.write("CA", "UTF-8");
		writer.write("GN", "Copyright narwhals.fr");
		writer.write("AP", "Kifu Android");
	}
	
	public void writePlayers(SgfWriter writer) throws IOException {
		for (Player player : players) {
			player.toSgf(writer);
		}
	}
	
	public void writeResult(SgfWriter writer) throws IOException {
		// TODO: write result
		writer.write("RE", "?");
	}
}
