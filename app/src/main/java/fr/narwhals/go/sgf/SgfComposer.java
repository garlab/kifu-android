package fr.narwhals.go.sgf;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import fr.narwhals.go.domain.Game;
import fr.narwhals.go.domain.Go;
import fr.narwhals.go.domain.History;
import fr.narwhals.go.domain.Move;
import fr.narwhals.go.domain.Player;
import fr.narwhals.go.domain.Point;
import fr.narwhals.go.domain.Stone;

public class SgfComposer {
    private StringBuilder sb;

    public SgfComposer() {
        this.sb = new StringBuilder();
    }

    public SgfComposer compose(Go game) {
        append(game);
        return this;
    }

    public String toString() {
        return sb.toString();
    }

    private void append(Go game) {
        appendHeader();
        append(game.game);
        for (Player player : game.players) {
            append(player);
        }
        append("RE", game.getResult());
        append(game.history);
        endGameTree();
    }

    private void beginGameTree() {
        sb.append("(");
    }

    private void endGameTree() {
        sb.append(")");
    }

    private void beginNode() {
        sb.append("\n;");
    }

    private void append(String key, int value) {
        sb.append(key + "[" + value + "]");
    }

    private void append(String key, int value, int defaultValue) {
        if (value != defaultValue) {
            sb.append(key + "[" + value + "]");
        }
    }

    private void append(String key, double value) {
        sb.append(key + "[" + value + "]");
    }

    private void append(String key, String value) {
        if (value != null) {
            sb.append(key + "[" + value + "]");
        }
    }

    private void append(String key, Point value) {
        if (value != null) {
            sb.append(key + "[" + value + "]");
        }
    }

    private void append(String key, Point[] values) {
        sb.append(key);
        for (Point value : values) {
            sb.append("[" + value + "]");
        }
    }

    private void append(String key, Date[] dates) {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        sb.append(key);
        for (Date date : dates) {
            sb.append("[" + fmt.format(date) + "]");
        }
    }

    private void append(Stone stone) {
        append(stone.getColor().getKey(), stone.getPoint());
    }

    private void append(Move move) {
        beginNode();
        append(move.getStone());
        if (move.getKo() != Point.NO_KO) {
            append("KO", move.getKo());
        }
    }

    private void append(Player player) {
        append("P" + player.getColor().getKey(), player.getName());
    }

    private void append(Game game) {
        append("RU", game.getRule().toString());
        append("SZ", game.getSize(), 19);
        append("KM", game.getKomi());
        append("TM", game.getTime(), 0);
        append("HA", game.getHandicap(), 0);
        //append("AB", game.getHandicaps());

        append("GN", game.getName());
        append("DT", game.getDates());
        append("CP", game.getCopyright());
        append("GC", game.getComment());
        append("EV", game.getEvent());
        append("RO", game.getRound());
        append("PC", game.getPlace());
        append("SO", game.getSource());
        append("AN", game.getAnnotation());
        append("US", game.getUser());
    }

    private void append(History history) {
        List<History.Node> children = history.getRoot().getChildren();
        if (children.size() == 1) {
            append(children.get(0));
        } else if (children.size() > 1) {
            for (History.Node child : children) {
                beginGameTree();
                append(child);
                endGameTree();
            }
        }
    }

    private void append(History.Node node) {
        for (; node.getChildren().size() == 1; node = node.getChildren().get(0)) {
            append(node.getMove());
        }
        append(node.getMove());
        for (History.Node child : node.getChildren()) {
            beginGameTree();
            append(child);
            endGameTree();
        }
    }

    private void appendHeader() {
        sb.append("(;");
        append("GM", 1); // Game, 1 = Go
        append("FF", 4, 1); // File Format
        append("ST", 0); // Style, can be 0-3
        append("CA", "UTF-8");
        append("AP", "Kifu-Android");
        sb.append("\n");
    }
}
