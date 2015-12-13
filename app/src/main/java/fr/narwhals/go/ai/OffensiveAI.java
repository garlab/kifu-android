package fr.narwhals.go.ai;

import java.util.List;

import fr.narwhals.go.domain.Game;
import fr.narwhals.go.domain.Go;
import fr.narwhals.go.domain.Liberty;
import fr.narwhals.go.domain.Player;
import fr.narwhals.go.domain.Point;
import fr.narwhals.go.domain.Section;
import fr.narwhals.go.domain.Stone;
import fr.narwhals.go.domain.StoneGroup;

public class OffensiveAI extends AI {
	private static final long serialVersionUID = -6177700203588599051L;

	public enum Strategy {
		FUSEKI, NORMAL
	}

	private final Go go;
	private final Player player;
	private Strategy strategy;

	public OffensiveAI(Go go, Player player) {
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
		}

		public void compute() {
			int actualNumberOfLiberties = stone.getActualNumberOfLiberties();
			int actualNeighborLiberties = stone.getActualNumberOfNeighborLiberties();

			libertyValue = (double) (actualNumberOfLiberties - actualNeighborLiberties) / 8;

			for (StoneGroup groupNeighbor : stone.getGroupNeighbors()) {
				if (groupNeighbor.getColor() == stone.getColor()) {
					if (groupNeighbor.getNumberOfLiberties() == 1 && actualNumberOfLiberties > 1) {
						this.saveValue += groupNeighbor.getNumberOfStones();
						this.territoryValue += groupNeighbor.getNumberOfStones();
					}
				} else if (groupNeighbor.getColor() == stone.getOpponentColor()) {
					if (groupNeighbor.getNumberOfLiberties() == 1) {
						this.captureValue += (double) (groupNeighbor.getNumberOfStones());
						this.territoryValue += groupNeighbor.getNumberOfStones();
					} else {
						if (actualNumberOfLiberties > 1) {
							this.libertyReduced = (double) ((double) (groupNeighbor.getNumberOfStones() * 2) / (double) (groupNeighbor
									.getNumberOfLiberties() - 1));
						}
					}
				}
			}

			if (actualNumberOfLiberties == 1 && captureValue <= 0) {
				saveValue = 0;
				territoryValue = 0;
				libertyReduced = 0;
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
