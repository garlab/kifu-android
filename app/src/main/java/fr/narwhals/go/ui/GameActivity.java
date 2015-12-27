package fr.narwhals.go.ui;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
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
import fr.narwhals.go.domain.Player;
import fr.narwhals.go.domain.Section.SColor;
import fr.narwhals.go.domain.Stone;
import fr.narwhals.go.view.BoardView;

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

    @ViewById Button undoButton;
    @ViewById Button passButton;
    @ViewById Button firstButton;
    @ViewById Button previousButton;
    @ViewById Button nextButton;
    @ViewById Button lastButton;
    @ViewById TextView resultTextView;
    @ViewById TextView blackPlayerTextView;
    @ViewById TextView whitePlayerTextView;
    @ViewById LinearLayout gobanLayout;

    final LinearLayout[] barsView = new LinearLayout[Go.State.values().length];
    final TextView[] scoresView = new TextView[2];

    int gobanSize;
    BoardView grid;

    @AfterExtras
    void initGo() {
        this.config = new Config(this);
        this.go = new Go(size, handicap, rule, blackPlayer, whitePlayer, this);

        if (blackPlayer.getAi()) {
            bots[0] = new OffensiveAI(go, blackPlayer, config.aiPass());
        }
        if (whitePlayer.getAi()) {
            bots[1] = new OffensiveAI(go, whitePlayer, config.aiPass());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (config.fullscreen()) {
            int fullscreen = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setFlags(fullscreen, fullscreen);
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

        gobanSize = getScreenSize();

        gobanLayout.getLayoutParams().height = gobanSize;
        gobanLayout.getLayoutParams().width = gobanSize;

        grid = new BoardView(this, config, go, gobanSize);
        gobanLayout.addView(grid);
        onNextTurn();
    }

    @Override
    public void onStateChange(Go.State oldState, Go.State newState) {
        barsView[oldState.ordinal()].setVisibility(View.GONE);
        barsView[newState.ordinal()].setVisibility(View.VISIBLE);
        switch (newState) {
            case Review:
                updateReviewButtons();
                break;
            case Over:
                String result = go.getResult();
                resultTextView.setText(result);
                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                break;
        }
        grid.invalidate();
    }

    @Override
    public void onNextTurn() {
        if (go.getState() == Go.State.OnGoing) {
            if (go.getCurrentPlayer().getAi()) {
                Stone stone = bots[go.getCurrentColor().ordinal()].getMove();
                if (stone == Stone.PASS) {
                    go.pass();
                } else {
                    go.move(stone);
                }
                grid.invalidate();
            }
        }
    }

    @Override
    public void onScoreChange() {
        // TODO: Meilleur affichage
        int blackScore = go.history.score.getCapturedStones(SColor.BLACK)
                + go.history.score.getMarkedDead(SColor.WHITE);
        int whiteScore = go.history.score.getCapturedStones(SColor.WHITE)
                + go.history.score.getMarkedDead(SColor.BLACK);
        scoresView[SColor.BLACK.ordinal()].setText("(" + blackScore + ")");
        scoresView[SColor.WHITE.ordinal()].setText("(" + whiteScore + ")");
    }

    @Click
    void firstButtonClicked() {
        if (go.history.hasPrev()) {
            go.goban.clear();
            go.history.goFirst();
            updateReviewButtons();
            grid.invalidate();
        }
    }

    @Click
    void previousButtonClicked() {
        if (go.history.hasPrev()) {
            go.history.undo();
            updateReviewButtons();
            grid.invalidate();
        }
    }

    @Click
    void nextButtonClicked() {
        if (go.history.hasNext()) {
            go.history.playNext();
            updateReviewButtons();
            grid.invalidate();
        }
    }

    @Click
    void lastButtonClicked() {
        while (go.history.hasNext()) {
            go.history.playNext();
        }
        updateReviewButtons();
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

    void updateReviewButtons() {
        firstButton.setEnabled(go.history.hasPrev());
        previousButton.setEnabled(go.history.hasPrev());
        nextButton.setEnabled(go.history.hasNext());
        lastButton.setEnabled(go.history.hasNext());
    }

    @SuppressWarnings("deprecation")
    int getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        int width, height;

        if (android.os.Build.VERSION.SDK_INT >= 13) {
            android.graphics.Point size = new android.graphics.Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
        } else {
            width = display.getWidth();
            height = display.getHeight();
        }

        return Math.min(width, height);
    }
}