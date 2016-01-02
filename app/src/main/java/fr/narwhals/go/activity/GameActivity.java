package fr.narwhals.go.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.androidannotations.annotations.*;

import java.io.File;

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
import fr.narwhals.go.sgf.SgfComposer;
import fr.narwhals.go.util.FileUtil;
import fr.narwhals.go.view.BoardView;

@EActivity(R.layout.game)
@OptionsMenu(R.menu.menu_game)
public class GameActivity extends BaseActivity implements GoEvent {

    @Extra int size;
    @Extra int handicap;
    @Extra Game.Rule rule;
    @Extra Player blackPlayer;
    @Extra Player whitePlayer;

    Config config;
    Go go;
    String fileName;
    final AI bots[] = new AI[2];

    @ViewById Button undoButton;
    @ViewById Button passButton;
    @ViewById Button firstButton;
    @ViewById Button previousButton;
    @ViewById Button nextButton;
    @ViewById Button lastButton;
    @ViewById LinearLayout gobanLayout;

    final LinearLayout[] barsView = new LinearLayout[Go.State.values().length];

    BoardView grid;

    @AfterExtras
    void initGo() {
        this.config = new Config(this);
        this.go = new Go(size, handicap, rule, blackPlayer, whitePlayer, this);
        this.fileName = "kifu_" + System.currentTimeMillis() + ".sgf";

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
    }

    @AfterViews
    void initViews() {
        this.barsView[Go.State.OnGoing.ordinal()] = (LinearLayout) findViewById(R.id.ongoing_bar);
        this.barsView[Go.State.Territories.ordinal()] = (LinearLayout) findViewById(R.id.territory_bar);
        this.barsView[Go.State.Over.ordinal()] = (LinearLayout) findViewById(R.id.over_bar);
        this.barsView[Go.State.Review.ordinal()] = (LinearLayout) findViewById(R.id.review_bar);

        int gobanSize = getScreenSize();

        gobanLayout.getLayoutParams().height = gobanSize;
        gobanLayout.getLayoutParams().width = gobanSize;

        grid = new BoardView(this, config, go, gobanSize);
        gobanLayout.addView(grid);

        toolBar.setTitle(blackPlayer.getName() + " vs " + whitePlayer.getName());

        onNextTurn();
    }

    @OptionsItem
    void actionSave() {
        String sgf = new SgfComposer(go).toString();
        File sgfFile = FileUtil.saveSgf(fileName, sgf);
        if (sgfFile != null) {
            Toast.makeText(this, "Game saved at " + sgfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } else {
            // TODO: use a snackbar with retry button
            Toast.makeText(this, "Error while saving game", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStateChange(Go.State oldState, Go.State newState) {
        barsView[oldState.ordinal()].setVisibility(View.GONE);
        barsView[newState.ordinal()].setVisibility(View.VISIBLE);
        switch (newState) {
            case OnGoing:
                showCurrentPlayer();
                break;
            case Territories:
                toolBar.setSubtitle("Select dead stones");
                break;
            case Review:
                showCurrentPlayer();
                updateReviewButtons();
                break;
            case Over:
                toolBar.setSubtitle(go.getResult());
                break;
        }
        grid.invalidate();
    }

    @Override
    public void onNextTurn() {
        switch (go.getState()) {
            case OnGoing:
                showCurrentPlayer();
                if (go.getCurrentPlayer().getAi()) {
                    Stone stone = bots[go.getCurrentColor().ordinal()].getMove();
                    if (stone == Stone.PASS) {
                        go.pass();
                    } else {
                        go.move(stone);
                    }
                    grid.invalidate();
                }
                break;
            case Review:
                showCurrentPlayer();
                break;
        }
    }

    @Override
    public void onScoreChange() {
        // TODO: Display captured stones somewhere
        int blackScore = go.history.score.getCapturedStones(SColor.BLACK)
                + go.history.score.getMarkedDead(SColor.WHITE);
        int whiteScore = go.history.score.getCapturedStones(SColor.WHITE)
                + go.history.score.getMarkedDead(SColor.BLACK);
        //scoresView[SColor.BLACK.ordinal()].setText("(" + blackScore + ")");
        //scoresView[SColor.WHITE.ordinal()].setText("(" + whiteScore + ")");
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
        go.setState(Go.State.OnGoing);
    }
    
    void showCurrentPlayer() {
        SColor color = go.getCurrentColor();
        toolBar.setLogo(color.equals(SColor.BLACK) ?
                R.drawable.stone_black :
                R.drawable.stone_white
        );
        toolBar.setLogoDescription(color.toString());
        toolBar.setSubtitle(go.getCurrentPlayer().getName() + " to play");
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