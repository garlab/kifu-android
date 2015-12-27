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

public class OffensiveAI implements AI {

	public enum Strategy {
		FUSEKI, NORMAL
	}

	private final Go go;
	private final Player player;
	private Strategy strategy;
	private boolean aiMustPass;

	public OffensiveAI(Go go, Player player, boolean aiMustPass) {
		this.go = go;
		this.player = player;
		this.strategy = getStrategy(go.game);
		this.aiMustPass = aiMustPass;
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
        Stone prev = go.history.getCurrentMove().getStone();
        if (aiMustPass && prev == Stone.PASS) {
            return Stone.PASS;
        }

		switch (strategy) {
            case FUSEKI:
                Point corner = getBestCorner();
                if (corner == null) {
                    this.strategy = Strategy.NORMAL;
                    return getMove();
                } else {
                    return new Stone(player.getColor(), corner, go.goban);
                }
            case NORMAL:
                Value move = getMax();
                return move.stone;
        }

        return Stone.PASS;
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
			int actualNumberOfLiberties = stone.getActualLiberties().size();
			int actualNeighborLiberties = stone.getActualNeighborLiberties().size();

			libertyValue = (double) (actualNumberOfLiberties - actualNeighborLiberties) / 8;

			for (StoneGroup groupNeighbor : stone.getGroupNeighbors()) {
				if (groupNeighbor.getColor() == stone.getColor()) {
					if (groupNeighbor.getLiberties().size() == 1 && actualNumberOfLiberties > 1) {
						this.saveValue += groupNeighbor.getStones().size();
						this.territoryValue += groupNeighbor.getStones().size();
					}
				} else if (groupNeighbor.getColor() == stone.getOpponentColor()) {
					if (groupNeighbor.getLiberties().size() == 1) {
						this.captureValue += (double) (groupNeighbor.getStones().size());
						this.territoryValue += groupNeighbor.getStones().size();
					} else {
						if (actualNumberOfLiberties > 1) {
							this.libertyReduced = (double) ((double) (groupNeighbor.getStones().size() * 2) / (double) (groupNeighbor
									.getLiberties().size() - 1));
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
