package fr.narwhals.go.ai;

import java.util.List;

import fr.narwhals.go.domain.GameInfo;
import fr.narwhals.go.domain.Game;
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

    private final Game game;
    private final Player player;
    private Strategy strategy;
    private boolean aiMustPass;

    public OffensiveAI(Game game, Player player, boolean aiMustPass) {
        this.game = game;
        this.player = player;
        this.strategy = getStrategy(game.gameInfo);
        this.aiMustPass = aiMustPass;
    }

    private Strategy getStrategy(GameInfo gameInfo) {
        if (gameInfo.getSize() == 19) {
            return Strategy.FUSEKI;
        } else {
            return Strategy.NORMAL;
        }
    }

    @Override
    public void play() {
        Stone stone = getMove();
        if (stone.getPoint() == Point.PASS) {
            game.pass();
        } else {
            game.move(stone);
        }
    }

    private Stone getMove() {
        Stone prev = game.history.getCurrentMove().getStone();
        if (aiMustPass && prev.getPoint() == Point.PASS) {
            return new Stone(game.history.getRound(), player.getColor(), Point.PASS, game.goban);
        }

        switch (strategy) {
            case FUSEKI:
                Point corner = getBestCorner();
                if (corner == null) {
                    this.strategy = Strategy.NORMAL;
                    return getMove();
                } else {
                    return new Stone(game.history.getRound(), player.getColor(), corner, game.goban);
                }
            case NORMAL:
                Value move = getMax();
                return move.stone;
        }

        return new Stone(game.history.getRound(), player.getColor(), Point.PASS, game.goban);
    }

    public Point getBestCorner() {
        boolean isBest;
        for (Point hoshi : game.goban.getHoshis()) {
            isBest = true;
            if (game.goban.isLiberty(hoshi)) {
                for (Section neighbor : game.goban.getNeighbors(hoshi)) {
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
        Value max = new Value(new Stone(game.history.getRound(), player.getColor(), Point.PASS, game.goban));
        List<Liberty> liberties = game.goban.getShuffledLiberties();

        for (Section section : liberties) {
            Stone stone = new Stone(game.history.getRound(), player.getColor(), section.getPoint(), game.goban);
            if (stone.isMoveValid() && !game.history.getCurrentMove().getKo().equals(stone.getPoint())) {
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

        public Value(Stone stone) {
            this.stone = stone;
            if (stone.getPoint() != Point.PASS) {
                compute();
            }
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
