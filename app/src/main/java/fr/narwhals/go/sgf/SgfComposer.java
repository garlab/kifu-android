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
    private StringBuilder sb = new StringBuilder();

    public SgfComposer(Go go) {
        beginGameTree();
        appendHeader();
        append(go.game);
        for (Player player : go.players) {
            append(player);
        }
        append("RE", "?"); // TODO handle result
        append(go.history);
        endGameTree();
    }

    public String toString() {
        return sb.toString();
    }

    private void beginGameTree() {
        sb.append("(");
    }

    private void endGameTree() {
        sb.append(")");
    }

    private void beginNode() {
        sb.append(";");
    }

    private void append(String key, String value) {
        sb.append(key + "[" + value + "]");
    }

    private void append(String key, int value) {
        append(key, String.valueOf(value));
    }

    private void append(String key, double value) {
        append(key, String.valueOf(value));
    }

    private void append(String key, Point value) {
        append(key, getString(value));
    }

    public void append(String key, Point[] values) {
        sb.append(key);
        for (Point value : values) {
            sb.append(String.format("[%s]", getString(value)));
        }
    }

    private void append(String key, List<Date> dates) {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        sb.append(key);
        for (Date date : dates) {
            sb.append(String.format("[%s]", fmt.format(date)));
        }
    }

    private void append(Stone stone) {
        append(stone.getColor().getKey(), stone.getPoint());
    }

    private void append(Move move) {
        append(move.getStone());
        if (move.getKo() != Point.NO_KO) {
            append("KO", move.getKo());
        }
    }

    private void append(Player player) {
        append("P" + player.getColor().getKey(), player.getName());
    }

    private void append(Game game) {
        append("SZ", game.getSize());
        //append("TM", game.getTime());
        append("RU", game.getRule().toString());
        append("HA", game.getHandicap());

        /*
        append("KM", game.getKomi());
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
        //*/
    }

    private void append(History history) {
        // TODO DSF on nodes
    }

    private void appendHeader() {
        beginNode();
        append("GM", 1);
        append("FF", 4);
        append("ST", 2);
        append("CA", "UTF-8");
        append("GN", "Copyright narwhals.fr");
        append("AP", "Kifu Android");
    }

    private static String getString(Point point) {
        char[] value = { (char) (point.getX() - 1 + 'a'), (char) (point.getY() - 1 + 'a')};
        return new String(value);
    }
}
