package fr.narwhals.go.ui;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.*;

import fr.narwhals.go.Config;
import fr.narwhals.go.R;
import fr.narwhals.go.ai.AI;
import fr.narwhals.go.ai.OffensiveAI;
import fr.narwhals.go.domain.Game;
import fr.narwhals.go.domain.Go;
import fr.narwhals.go.domain.GoEvent;
import fr.narwhals.go.domain.Liberty;
import fr.narwhals.go.domain.Move;
import fr.narwhals.go.domain.Player;
import fr.narwhals.go.domain.Point;
import fr.narwhals.go.domain.Section.SColor;
import fr.narwhals.go.domain.Stone;
import fr.narwhals.go.domain.StoneGroup;
import fr.narwhals.go.domain.Territory;

@NoTitle
@EActivity(R.layout.game)
public class GameActivity extends Activity implements GoEvent {

    Config config;

    @Extra int size;
    @Extra int handicap;
    @Extra Game.Rule rule;
    @Extra Player blackPlayer;
    @Extra Player whitePlayer;

    Go go;
    final AI bots[] = new AI[2];

    @ViewById Button firstButton;
    @ViewById Button undoButton;
    @ViewById Button passButton;
    @ViewById Button nextButton;
    @ViewById Button lastButton;
    @ViewById TextView resultTextView;
    @ViewById TextView blackPlayerTextView;
    @ViewById TextView whitePlayerTextView;
    @ViewById LinearLayout gobanLayout;

    final LinearLayout[] barsView = new LinearLayout[Go.State.values().length];
    final TextView[] scoresView = new TextView[2];

    GridView grid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        this.config = new Config(this);

        if (config.fullscreen()) {
            int fullscreen = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setFlags(fullscreen, fullscreen);
        }
    }

    @AfterExtras
    void initGo() {
        Player[] players = { blackPlayer, whitePlayer };
        Game game = new Game(size, handicap, rule);

        go = new Go(game, players, this);

        if (players[0].getAi()) {
            bots[0] = new OffensiveAI(go, players[0]);
        }
        if (players[1].getAi()) {
            bots[1] = new OffensiveAI(go, players[1]);
        }
    }

    @AfterViews
    void initViews() {
        this.barsView[Go.State.OnGoing.ordinal()] = (LinearLayout) findViewById(R.id.ongoing_bar);
        this.barsView[Go.State.Territories.ordinal()] = (LinearLayout) findViewById(R.id.territory_bar);
        this.barsView[Go.State.Over.ordinal()] = (LinearLayout) findViewById(R.id.over_bar);
        this.barsView[Go.State.Review.ordinal()] = (LinearLayout) findViewById(R.id.review_bar);

        this.scoresView[SColor.BLACK.ordinal()] = (TextView) findViewById(R.id.black_score);
        this.scoresView[SColor.WHITE.ordinal()] = (TextView) findViewById(R.id.white_score);

        blackPlayerTextView.setText(blackPlayer.getName());
        whitePlayerTextView.setText(whitePlayer.getName());

        int screenSize = getScreenSize();

        gobanLayout.getLayoutParams().height = screenSize;
        gobanLayout.getLayoutParams().width = screenSize;

        grid = new GridView(this, go, screenSize);
        gobanLayout.addView(grid);
        onNextTurn();
    }

    @Override
    public void onStateChange(Go.State oldState, Go.State newState) {
        barsView[oldState.ordinal()].setVisibility(View.GONE);
        barsView[newState.ordinal()].setVisibility(View.VISIBLE);
        switch (newState) {
            case Over:
                String result = go.getResult();
                resultTextView.setText(result);
                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                break;
        }
        grid.invalidate();
    }

    private void botMove() {
        // TODO: pass the config value as a parameter to the ai
        Stone prev = go.history.getCurrentMove().getStone();
        if (config.aiPass() && prev == Stone.PASS) {
            go.pass();
        } else {
            Stone stone = bots[go.getCurrentColor().ordinal()].getMove();
            if (stone == Stone.PASS) {
                go.pass();
            } else {
                go.move(stone);
            }
        }
    }

    @Override
    public void onNextTurn() {
        if (go.getState() == Go.State.OnGoing) {
            if (go.getCurrentPlayer().getAi()) {
                botMove();
                grid.invalidate();
            }
        }
    }

    @Click
    void firstButtonClicked() {
        if (go.history.hasPrev()) {
            go.goban.clear();
            go.history.goFirst();
            grid.invalidate();
        }
    }

    @Click
    void previousButtonClicked() {
        if (go.history.hasPrev()) {
            go.history.undo();
            grid.invalidate();
        }
    }

    @Click
    void nextButtonClicked() {
        if (go.history.hasNext()) {
            go.history.playNext();
            grid.invalidate();
        }
    }

    @Click
    void lastButtonClicked() {
        while (go.history.hasNext()) {
            go.history.playNext();
        }
        grid.invalidate();
    }

    @Click
    void undoButtonClicked() {
        if (go.history.hasPrev()) {
            go.history.undo();
            grid.invalidate();
        }
    }

    @Click
    public void passButtonClicked() {
        go.pass();
        grid.invalidate();
    }

    @Click
    void giveUpButtonClicked() {
        go.setEndOfGame(Go.EndOfGame.GiveUp);
    }

    @Click
    void cancelButtonClicked() {
        go.setState(Go.State.OnGoing);
    }

    @Click
    void proceedButtonClicked() {
        go.setEndOfGame(Go.EndOfGame.Standard);
    }

    @Click
    void reviewButtonClicked() {
        go.setState(Go.State.Review);
    }

    @Click
    void playAgainButtonClicked() {
        go.clear();
        resultTextView.setText("");
        go.setState(Go.State.OnGoing);
    }

    private int getScreenSize() {
        int width = getWindowManager().getDefaultDisplay().getWidth();
        int height = getWindowManager().getDefaultDisplay().getHeight();
        return width > height ? height : width;
    }

    class GridView extends View implements OnTouchListener {
        private Paint paint = new Paint();

        private final int screenSize;
        private final int sectionSize;
        private final int shapeSize;
        private final int libertySize;

        private final int crossColorValid = Color.rgb(0, 0, 200);
        private final int crossColorInvalid = Color.rgb(200, 0, 0);
        private final int textColor = Color.rgb(60, 44, 23); // 3C2C17
        private final int koColor = Color.rgb(200, 110, 15);

        private Bitmap blackStoneBitmap;
        private Bitmap whiteStoneBitmap;
        private Bitmap blackStoneDeadBitmap;
        private Bitmap whiteStoneDeadBitmap;

        private Go go;
        private Point currentPoint = null;

        public GridView(Context context, Go go, int screenSize) {
            super(context);
            setOnTouchListener(this);
            setFocusable(true);

            this.go = go;
            this.screenSize = screenSize;
            this.sectionSize = screenSize / go.game.getSize();
            this.shapeSize = sectionSize * 8 / 10;
            this.libertySize = sectionSize / 2;

            blackStoneBitmap = H.getBitmap(R.drawable.stone_black, sectionSize, sectionSize, getResources());
            whiteStoneBitmap = H.getBitmap(R.drawable.stone_white, sectionSize, sectionSize, getResources());
            blackStoneDeadBitmap = H.getBitmap(R.drawable.stone_alpha_black, sectionSize, sectionSize, getResources());
            whiteStoneDeadBitmap = H.getBitmap(R.drawable.stone_alpha_white, sectionSize, sectionSize, getResources());

            paint.setAntiAlias(true);

            this.setMinimumHeight(screenSize);
            this.setMinimumWidth(screenSize);
        }

        public void refreshScore() {
            go.history.score.setStoneGroups(go.goban.getStoneGroups());
            go.history.score.setTerritories(go.goban.getTerritories());
        }

        @Override
        public boolean onTouch(View v, MotionEvent me) {
            int x = (int) ((me.getX()) * go.game.getSize() / screenSize);
            int y = (int) ((me.getY()) * go.game.getSize() / screenSize);

            Point point = currentPoint;
            currentPoint = null;

            if (x < 0 || y < 0 || x >= go.game.getSize() || y >= go.game.getSize()) {
                return true;
            }

            switch (go.getState()) {
            case OnGoing:
            case Review:
                if (me.getAction() == MotionEvent.ACTION_DOWN || me.getAction() == MotionEvent.ACTION_MOVE) {
                    currentPoint = new Point(x + 1, y + 1);
                } else if (me.getAction() == MotionEvent.ACTION_UP) {
                    go.tryMove(point);
                }
                break;

            case Territories:
                if (me.getAction() == MotionEvent.ACTION_UP) {
                    point = new Point(x + 1, y + 1);
                    Stone stone = go.goban.getStone(point);
                    if (stone != null) {
                        stone.getStoneGroup().mark();
                        refreshScore();
                    }
                }
                break;
            }
            invalidate();
            return true;
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            switch (go.getState()) {
            case OnGoing:
            case Review:
                Move move = go.history.getCurrentMove();
                drawGrids(go.game.getSize(), canvas);
                drawHoshis(go.game.getHoshis(), go.game.getSize(), canvas);
                if (currentPoint != null) {
                    drawCross(currentPoint, go.game.getSize(), crossColorInvalid, canvas);
                    drawStone(currentPoint, go.getCurrentColor(), go.game.getSize(), canvas);
                }
                if (config.showLastMove()) {
                    drawHint(move.getStone(), go.game.getSize(), canvas);
                }
                List<Stone> stones = go.goban.getStones();
                drawStones(stones, go.game.getSize(), canvas);
                if (config.numberMoves()) {
                    // TODO use history instead and remove the round property in stone
                    drawNumbers(stones, go.game.getSize(), canvas);
                }
                drawShapes(move, go.game.getSize(), canvas);
                if (config.tagVariations()) {
                    drawPaths(go.history.getChildren(), go.game.getSize(), canvas);
                }
                if (move.getKo() != Point.NO_KO) {
                    drawKo(move.getKo(), go.game.getSize(), canvas);
                }

                firstButton.setEnabled(go.history.hasPrev());
                undoButton.setEnabled(go.history.hasPrev());
                nextButton.setEnabled(go.history.hasNext());
                lastButton.setEnabled(go.history.hasNext());
                break;

            case Territories:
            case Over:
                drawGrids(go.game.getSize(), canvas);
                drawHoshis(go.game.getHoshis(), go.game.getSize(), canvas);
                drawStoneGroups(go.goban.getStoneGroups(), go.game.getSize(), canvas);
                drawTerritories(go.goban.getTerritories(), go.game.getSize(), canvas);
                break;
            }

            // TODO: Meilleur affichage
            int blackScore = go.history.score.getCapturedStones(SColor.BLACK)
                    + go.history.score.getMarkedDead(SColor.WHITE);
            int whiteScore = go.history.score.getCapturedStones(SColor.WHITE)
                    + go.history.score.getMarkedDead(SColor.BLACK);
            scoresView[SColor.BLACK.ordinal()].setText("(" + blackScore + ")");
            scoresView[SColor.WHITE.ordinal()].setText("(" + whiteScore + ")");
        }

        private void drawGrids(int size, Canvas canvas) {
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(1);

            int b = screenSize / (2 * size);

            for (int i = 0; i < size; ++i) {
                int v1 = b + i * screenSize / size;
                int v2 = b + (size - 1) * screenSize / size;
                canvas.drawLine(b, v1, v2, v1, paint);
                canvas.drawLine(v1, b, v1, v2, paint);
            }
        }

        private void drawHoshis(List<Point> hoshis, int size, Canvas canvas) {
            paint.setStyle(Style.FILL);
            paint.setColor(Color.BLACK);
            int hoshiSize = screenSize / (130); // TODO: choisir une valeur
                                                // moins arbitraire
            for (Point point : hoshis) {
                int left = screenSize / (2 * size) + (point.getX() - 1) * screenSize / size - hoshiSize;
                int top = screenSize / (2 * size) + (point.getY() - 1) * screenSize / size - hoshiSize;
                int right = screenSize / (2 * size) + (point.getX() - 1) * screenSize / size + hoshiSize;
                int bottom = screenSize / (2 * size) + (point.getY() - 1) * screenSize / size + hoshiSize;
                canvas.drawRect(left, top, right, bottom, paint);
            }
        }

        private void drawCross(Point point, int size, int color, Canvas canvas) {
            paint.setColor(color);
            paint.setStrokeWidth(5);

            int v_x1 = screenSize / (2 * size);
            int v_y1 = screenSize / (2 * size) + (point.getY() - 1) * screenSize / size;
            int v_x2 = screenSize / (2 * size) + (size - 1) * screenSize / size;
            int v_y2 = v_y1;
            canvas.drawLine(v_x1, v_y1, v_x2, v_y2, paint);

            int h_x1 = screenSize / (2 * size) + (point.getX() - 1) * screenSize / size;
            int h_y1 = screenSize / (2 * size);
            int h_x2 = h_x1;
            int h_y2 = screenSize / (2 * size) + (size - 1) * screenSize / size;
            canvas.drawLine(h_x1, h_y1, h_x2, h_y2, paint);
        }

        private void drawStones(List<Stone> stones, int size, Canvas canvas) {
            for (Stone stone : stones) {
                drawStone(stone.getPoint(), stone.getColor(), size, canvas);
            }
        }

        private void drawNumbers(List<Stone> stones, int size, Canvas canvas) {
            for (Stone stone : stones) {
                if (stone.getRound() != -1) {
                    drawNumber(stone, size, canvas);
                }
            }
        }

        private void drawStone(Point point, SColor color, int size, Canvas canvas) {
            int left = (point.getX() - 1) * screenSize / size;
            int top = (point.getY() - 1) * screenSize / size;
            Bitmap bitmap = color.equals(SColor.BLACK) ? blackStoneBitmap : whiteStoneBitmap;
            canvas.drawBitmap(bitmap, left, top, paint);
        }

        private void drawStoneDead(Point point, SColor color, int size, Canvas canvas) {
            int left = (point.getX() - 1) * screenSize / size;
            int top = (point.getY() - 1) * screenSize / size;
            Bitmap bitmap = color.equals(SColor.BLACK) ? blackStoneDeadBitmap : whiteStoneDeadBitmap;
            canvas.drawBitmap(bitmap, left, top, paint);
        }

        private void drawNumber(Stone stone, int size, Canvas canvas) {
            String round = String.valueOf(stone.getRound());
            paint.setColor(stone.getColor() == SColor.BLACK ? Color.WHITE : Color.BLACK);
            paint.setStyle(Style.FILL);
            paint.setStrokeWidth(1);
            drawLabel(stone.getPoint(), round, size, canvas);
        }

        private void drawShapes(Move move, int size, Canvas canvas) {
            if (move.hasCircles()) {
                drawCircles(move.getCircles(), size, canvas);
            }
            if (move.hasSquares()) {
                drawSquares(move.getSquares(), size, canvas);
            }
            if (move.hasTriangles()) {
                drawTriangles(move.getTriangles(), size, canvas);
            }
        }

        private void drawCircles(List<Point> circles, int size, Canvas canvas) {
            for (Point circle : circles) {
                drawCircle(circle, size, canvas);
            }
        }

        private void drawSquares(List<Point> squares, int size, Canvas canvas) {
            for (Point square : squares) {
                drawSquare(square, size, canvas);
            }
        }

        private void drawTriangles(List<Point> triangles, int size, Canvas canvas) {
            for (Point triangle : triangles) {
                drawTriangle(triangle, size, canvas);
            }
        }

        private void drawCircle(Point circle, int size, Canvas canvas) {
            int left = (circle.getX() - 1) * screenSize / size + (sectionSize - shapeSize) / 2;
            int top = (circle.getY() - 1) * screenSize / size + (sectionSize - shapeSize) / 2;
            paint.setColor(Color.BLACK); // TODO: set good color (if on a black stone : white, else black)

            canvas.drawCircle(left, top, shapeSize, paint);
        }

        private void drawSquare(Point square, int size, Canvas canvas) {
            int left = (square.getX() - 1) * screenSize / size + (sectionSize - shapeSize) / 2;
            int top = (square.getY() - 1) * screenSize / size + (sectionSize - shapeSize) / 2;
            paint.setColor(Color.BLACK); // TODO: set good color (if on a black stone : white, else black)

            canvas.drawRect(left + 1, top + 1, left + shapeSize - 2, top + shapeSize - 2, paint);
        }

        private void drawTriangle(Point triangle, int size, Canvas canvas) {
            // TODO: draw triangle
        }

        private void drawHint(Stone stone, int size, Canvas canvas) {
            if (stone != null) {
                Point coord = stone.getPoint();
                int left = (coord.getX() - 1) * screenSize / size + (sectionSize) / 2;
                int top = (coord.getY() - 1) * screenSize / size + (sectionSize) / 2;

                paint.setStrokeWidth(4);
                paint.setColor(crossColorInvalid);

                canvas.drawCircle(left, top, sectionSize / 2 + 4, paint);
            }
        }

        private void drawPaths(List<Stone> stones, int size, Canvas canvas) {
            paint.setStyle(Style.FILL);
            paint.setStrokeWidth(1);

            char label = 'A';
            passButton.setTextColor(textColor);
            for (Stone stone : stones) {
                if (stone == Stone.PASS) {
                    passButton.setTextColor(crossColorInvalid);
                } else {
                    drawPath(stone.getPoint(), String.valueOf(label), size, canvas);
                    label++;
                }
            }
        }

        private void drawPath(Point point, String label, int size, Canvas canvas) {
            int left = (point.getX() - 1) * screenSize / size;
            int top = (point.getY() - 1) * screenSize / size;

            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(10);

            canvas.drawRect(left + 1, top + 1, left + sectionSize - 2, top + sectionSize - 2, paint);

            paint.setColor(Color.WHITE);
            drawLabel(point, label, size, canvas);
        }

        private void drawLabel(Point point, String label, int size, Canvas canvas) {
            int left = screenSize / (2 * size) + (point.getX() - 1) * screenSize / size;
            int top = (point.getY()) * screenSize / size - sectionSize / 8;

            // TODO: Adapter la taille proprement
            int diff = (label.length() - 1) * 12;
            paint.setTextSize(sectionSize - diff);
            paint.setTextAlign(Align.CENTER);
            canvas.drawText(label, left, top - diff / 2, paint);
        }

        private void drawTerritories(List<Territory> territories, int size, Canvas canvas) {
            for (Territory territory : territories) {
                if (territory.getColor() != SColor.SHARED && territory.getColor() != SColor.NONE) {
                    drawTerritory(territory, size, canvas);
                }
            }
        }

        private void drawTerritory(Territory territory, int size, Canvas canvas) {
            for (Liberty liberty : territory.getLiberties()) {
                drawLiberty(liberty.getPoint(), territory.getColor(), size, canvas);
            }
        }

        private void drawLiberty(Point liberty, SColor color, int size, Canvas canvas) {
            int left = (liberty.getX() - 1) * screenSize / size + (sectionSize - shapeSize) / 2;
            int top = (liberty.getY() - 1) * screenSize / size + (sectionSize - shapeSize) / 2;
            paint.setColor(color == SColor.BLACK ? Color.BLACK : Color.WHITE);

            canvas.drawRect(left + libertySize / 2, top + libertySize / 2, left + libertySize, top + libertySize, paint);
        }

        private void drawStoneGroups(List<StoneGroup> stoneGroups, int size, Canvas canvas) {
            for (StoneGroup stoneGroup : stoneGroups) {
                if (stoneGroup.isDead()) {
                    SColor opponent = stoneGroup.getColor().getOpponentColor();
                    for (Stone stone : stoneGroup.getStones()) {
                        drawStoneDead(stone.getPoint(), stone.getColor(), size, canvas);
                        drawLiberty(stone.getPoint(), opponent, size, canvas);
                    }
                } else {
                    for (Stone stone : stoneGroup.getStones()) {
                        drawStone(stone.getPoint(), stone.getColor(), size, canvas);
                    }
                }
            }
        }

        private void drawKo(Point ko, int size, Canvas canvas) {
            paint.setStyle(Style.STROKE);
            paint.setColor(koColor);
            paint.setStrokeWidth(3);

            drawSquare(ko, size, canvas);
        }
    }
}