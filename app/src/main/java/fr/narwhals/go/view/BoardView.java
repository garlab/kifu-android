package fr.narwhals.go.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EView;

import java.util.List;

import fr.narwhals.go.bean.Config;
import fr.narwhals.go.R;
import fr.narwhals.go.domain.Game;
import fr.narwhals.go.domain.Liberty;
import fr.narwhals.go.domain.Move;
import fr.narwhals.go.domain.Point;
import fr.narwhals.go.domain.Section;
import fr.narwhals.go.domain.Stone;
import fr.narwhals.go.domain.StoneGroup;
import fr.narwhals.go.domain.Territory;
import fr.narwhals.go.activity.H;

@EView
public class BoardView extends View {
    private Paint paint = new Paint();

    private final int crossColorValid = Color.rgb(0, 0, 200);
    private final int crossColorInvalid = Color.rgb(200, 0, 0);
    private final int textColor = Color.rgb(60, 44, 23); // 3C2C17
    private final int koColor = Color.rgb(200, 110, 15);

    @Bean Config config;

    private int screenSize;
    private int sectionSize;
    private int shapeSize;
    private int libertySize;

    private Bitmap blackStoneBitmap;
    private Bitmap whiteStoneBitmap;
    private Bitmap blackStoneDeadBitmap;
    private Bitmap whiteStoneDeadBitmap;

    private Game game;
    private Point currentPoint = null;

    public BoardView(Context context) {
        super(context);
    }

    public void init(Game game, int screenSize) {
        setFocusable(true);

        this.game = game;
        this.screenSize = screenSize;
        this.sectionSize = screenSize / game.gameInfo.getSize();
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
        game.history.score.setStoneGroups(game.goban.getStoneGroups());
        game.history.score.setTerritories(game.goban.getTerritories());
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        int x = (int) ((me.getX()) * game.gameInfo.getSize() / screenSize);
        int y = (int) ((me.getY()) * game.gameInfo.getSize() / screenSize);

        Point point = currentPoint;
        currentPoint = null;

        if (x < 0 || y < 0 || x >= game.gameInfo.getSize() || y >= game.gameInfo.getSize()) {
            return true;
        }

        switch (game.getState()) {
            case OnGoing:
            case Review:
                if (me.getAction() == MotionEvent.ACTION_DOWN || me.getAction() == MotionEvent.ACTION_MOVE) {
                    currentPoint = new Point(x + 1, y + 1);
                } else if (me.getAction() == MotionEvent.ACTION_UP) {
                    game.tryMove(point);
                }
                invalidate();
                break;

            case Territories:
                if (me.getAction() == MotionEvent.ACTION_UP) {
                    point = new Point(x + 1, y + 1);
                    Stone stone = game.goban.getStone(point);
                    if (stone != null) {
                        stone.getStoneGroup().mark();
                        refreshScore();
                        invalidate();
                    }
                }
                break;
        }

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        switch (game.getState()) {
            case OnGoing:
            case Review:
                Move move = game.history.getCurrentMove();
                drawBoard(canvas);
                if (currentPoint != null) {
                    drawCross(currentPoint, game.gameInfo.getSize(), crossColorInvalid, canvas);
                    drawStone(currentPoint, game.getCurrentColor(), game.gameInfo.getSize(), canvas);
                }
                if (config.showLastMove()) {
                    drawHint(move.getStone(), game.gameInfo.getSize(), canvas);
                }
                List<Stone> stones = game.goban.getStones();
                drawStones(stones, game.gameInfo.getSize(), canvas);
                if (config.numberMoves()) {
                    // TODO: use history instead and remove the round property in stone
                    drawNumbers(stones, game.gameInfo.getSize(), canvas);
                }
                drawShapes(move, game.gameInfo.getSize(), canvas);
                if (config.tagVariations()) {
                    drawPaths(game.history.getChildren(), game.gameInfo.getSize(), canvas);
                }
                if (move.getKo() != Point.NO_KO) {
                    drawKo(move.getKo(), game.gameInfo.getSize(), canvas);
                }
                break;

            case Territories:
            case Over:
                drawBoard(canvas);
                drawStoneGroups(game.goban.getStoneGroups(), game.gameInfo.getSize(), canvas);
                drawTerritories(game.goban.getTerritories(), game.gameInfo.getSize(), canvas);
                break;
        }
    }

    private void drawBoard(Canvas canvas) {
        drawGrids(game.gameInfo.getSize(), canvas);
        drawHoshis(game.goban.getHoshis(), game.gameInfo.getSize(), canvas);
    }

    private void drawGrids(int size, Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1);

        int b = screenSize / (2 * size);
        int v2 = b + (size - 1) * screenSize / size;

        for (int i = 0; i < size; ++i) {
            int v1 = b + i * screenSize / size;
            canvas.drawLine(b, v1, v2, v1, paint);
            canvas.drawLine(v1, b, v1, v2, paint);
        }
    }

    private void drawHoshis(Point[] hoshis, int size, Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
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
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        for (Stone stone : stones) {
            if (stone.getRound() != -1) {
                String round = String.valueOf(stone.getRound() + 1);
                paint.setColor(stone.getColor() == Section.SColor.BLACK ? Color.WHITE : Color.BLACK);
                drawLabel(stone.getPoint(), round, size, canvas);
            }
        }
    }

    private void drawStone(Point point, Section.SColor color, int size, Canvas canvas) {
        int left = (point.getX() - 1) * screenSize / size;
        int top = (point.getY() - 1) * screenSize / size;
        Bitmap bitmap = color.equals(Section.SColor.BLACK) ? blackStoneBitmap : whiteStoneBitmap;
        canvas.drawBitmap(bitmap, left, top, paint);
    }

    private void drawStoneDead(Point point, Section.SColor color, int size, Canvas canvas) {
        int left = (point.getX() - 1) * screenSize / size;
        int top = (point.getY() - 1) * screenSize / size;
        Bitmap bitmap = color.equals(Section.SColor.BLACK) ? blackStoneDeadBitmap : whiteStoneDeadBitmap;
        canvas.drawBitmap(bitmap, left, top, paint);
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
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        char label = 'A';
        //passButton.setTextColor(textColor);
        for (Stone stone : stones) {
            if (stone.getPoint() == Point.PASS) {
                //passButton.setTextColor(crossColorInvalid);
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
        
        int diff = Math.max(label.length() - 1, 1) * 12;
        paint.setTextSize(sectionSize - diff);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, left, top - diff / 2, paint);
    }

    private void drawTerritories(List<Territory> territories, int size, Canvas canvas) {
        for (Territory territory : territories) {
            if (territory.getColor() != Section.SColor.SHARED && territory.getColor() != Section.SColor.NONE) {
                drawTerritory(territory, size, canvas);
            }
        }
    }

    private void drawTerritory(Territory territory, int size, Canvas canvas) {
        for (Liberty liberty : territory.getLiberties()) {
            drawLiberty(liberty.getPoint(), territory.getColor(), size, canvas);
        }
    }

    private void drawLiberty(Point liberty, Section.SColor color, int size, Canvas canvas) {
        int left = (liberty.getX() - 1) * screenSize / size + (sectionSize - shapeSize) / 2;
        int top = (liberty.getY() - 1) * screenSize / size + (sectionSize - shapeSize) / 2;
        paint.setColor(color == Section.SColor.BLACK ? Color.BLACK : Color.WHITE);

        canvas.drawRect(left + libertySize / 2, top + libertySize / 2, left + libertySize, top + libertySize, paint);
    }

    private void drawStoneGroups(List<StoneGroup> stoneGroups, int size, Canvas canvas) {
        for (StoneGroup stoneGroup : stoneGroups) {
            if (stoneGroup.isDead()) {
                Section.SColor opponent = stoneGroup.getColor().getOpponentColor();
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
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(koColor);
        paint.setStrokeWidth(3);

        drawSquare(ko, size, canvas);
    }
}