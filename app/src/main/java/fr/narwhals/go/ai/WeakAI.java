package fr.narwhals.go.ai;

import java.util.List;

import android.util.Log;
import fr.narwhals.go.domain.Game;
import fr.narwhals.go.domain.Go;
import fr.narwhals.go.domain.Liberty;
import fr.narwhals.go.domain.Player;
import fr.narwhals.go.domain.Point;
import fr.narwhals.go.domain.Section;
import fr.narwhals.go.domain.Stone;
import fr.narwhals.go.domain.StoneGroup;

public class WeakAI extends AI {

	public enum Strategy {
		FUSEKI, NORMAL
	}

	private final Go go;
	private final Player player;
	private Strategy strategy;

	public WeakAI(Go go, Player player) {
		this.go = go;
		this.player = player;
		this.strategy = getStrategy(go.game);
	}

	private Strategy getStrategy(Game game) {
		if (game.getSize() == 19) {
			return Strategy.FUSEKI;
		} else {
			return Strategy.NORMAL;
		}
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public Stone getMove() {
		Stone stone = null;
		Log.d("Strategy", this.strategy.toString());
		switch (strategy) {
		case FUSEKI:
			Point corner = getBestCorner();
			if (corner == null) {
				this.strategy = Strategy.NORMAL;
				stone = getMove();
			} else {
				stone = new Stone(player.getColor(), corner, go.goban);
			}
			break;
		case NORMAL:
			Value move = getMax();
			Log.e("WeakAI", move.toString());
			stone = move.stone;
			break;
		default:
			stone = Stone.PASS;
			break;
		}
		return stone;
	}

	public Point getBestCorner() {
		boolean isBest;
		for (Point hoshi : go.game.getHoshis()) {
			isBest = true;
			if (go.goban.isLiberty(hoshi)) {
				for (Section neighbor : go.goban.getNeighbors(hoshi)) {
					if (neighbor instanceof Stone) {
						isBest = false;
						break;
					}
				}
				if (isBest) {
					return hoshi;
				}
			}
		}
		return null;
	}

	public Value getMax() {
		Value max = new Value();
		List<Liberty> liberties = go.goban.getShuffledLiberties();

		for (Section section : liberties) {
			Stone stone = new Stone(player.getColor(), section.getPoint(), go.goban);
			if (stone.isMoveValid() && !go.history.getCurrentMove().getKo().equals(stone.getPoint())) {
				Value current = new Value(stone);
				if (current.getSum() > max.getSum()) {
					max = current;
				}
			}
		}
		return max;
	}

	static class Value {
		final Stone stone;
		double saveValue = 0; //
		double captureValue = 0; // Nombre de pierres capturées
		double libertyValue = 0; // Libertés ajouté aux groupes allié
		double territoryValue = 0; // Nombre de territoires gagné
		double libertyReduced = 0; // Valeur de réduction de libertée des
									// groupes adverse

		public Value() {
			this.stone = Stone.PASS;
		}

		public Value(Stone stone) {
			this.stone = stone;
			compute();
			if (getSum() > 0) {
				Log.i(stone.getPoint().toString(), toString());
			}
		}

		public void compute() {
			int totalNumberOfLibertiesAdded = 0;
			int actualNumberOfLiberties = stone.getActualLiberties().size();

			for (StoneGroup groupNeighbor : stone.getGroupNeighbors()) {

				if (groupNeighbor.getColor() == stone.getColor()) {

					if (actualNumberOfLiberties > groupNeighbor.getLiberties().size()) {
						int numberOfLibertiesAdded = groupNeighbor.getLibertiesAdded(stone);
						totalNumberOfLibertiesAdded += numberOfLibertiesAdded;
						saveValue += (double) (groupNeighbor.getStones().size() * 2 * numberOfLibertiesAdded)
								/ (double) groupNeighbor.getLiberties().size();

					} else if (actualNumberOfLiberties == 1) {
						libertyValue = -2;
					}
				} else if (groupNeighbor.getColor() == stone.getOpponentColor()) {
					if (actualNumberOfLiberties != 1 || groupNeighbor.getLiberties().size() == 1) {
						captureValue += (double) (groupNeighbor.getStones().size() * 2)
								/ (double) groupNeighbor.getLiberties().size();
					}
				}
			}

			if (actualNumberOfLiberties == 1) {
				if (captureValue == 2) { /* Snape back */
					captureValue = 0;
				}
			}

			if (captureValue != 0) {
				libertyValue = 0;
			}

			if (captureValue == 0 && saveValue == 0 && totalNumberOfLibertiesAdded == 0) {
				territoryValue--;
			}
		}

		double getSum() {
			return saveValue + captureValue + libertyValue + territoryValue + libertyReduced;
		}

		@Override
		public String toString() {
			return "saveValue: " + saveValue + "\ncaptureValue: " + captureValue + "\nlibertyValue: " + libertyValue
					+ "\nterritoryValue: " + territoryValue + "\nlibertyReduced: " + libertyReduced;
		}
	}
}
