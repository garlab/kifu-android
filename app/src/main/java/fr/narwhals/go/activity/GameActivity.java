package fr.narwhals.go.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.androidannotations.annotations.*;

import fr.narwhals.go.bean.Config;
import fr.narwhals.go.R;
import fr.narwhals.go.ai.AI;
import fr.narwhals.go.ai.OffensiveAI;
import fr.narwhals.go.bean.SgfHandler;
import fr.narwhals.go.domain.GameInfo;
import fr.narwhals.go.domain.Game;
import fr.narwhals.go.domain.GoEvent;
import fr.narwhals.go.domain.Player;
import fr.narwhals.go.domain.Point;
import fr.narwhals.go.domain.Section.SColor;
import fr.narwhals.go.domain.Stone;
import fr.narwhals.go.view.BoardView;

@EActivity(R.layout.game_layout)
@OptionsMenu(R.menu.menu_game)
public class GameActivity extends BaseActivity implements GoEvent {

    @Extra int size;
    @Extra int handicap;
    @Extra GameInfo.Rule rule;
    @Extra Player blackPlayer;
    @Extra Player whitePlayer;
    private Game game;

    @Bean Config config;
    @Bean SgfHandler sgfHandler;
    final AI bots[] = new AI[2];

    @ViewById Button undoButton;
    @ViewById Button passButton;
    @ViewById Button firstButton;
    @ViewById Button previousButton;
    @ViewById Button nextButton;
    @ViewById Button lastButton;
    @ViewById LinearLayout gobanLayout;

    final LinearLayout[] barsView = new LinearLayout[Game.State.values().length];

    BoardView grid;

    @AfterExtras
    void initGo() {
        this.game = new Game(size, handicap, rule, blackPlayer, whitePlayer, this);
    }

    @AfterInject
    void initBots() {
        if (blackPlayer.getAi()) {
            bots[0] = new OffensiveAI(game, blackPlayer, config.aiPass());
        }
        if (whitePlayer.getAi()) {
            bots[1] = new OffensiveAI(game, whitePlayer, config.aiPass());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @AfterViews
    void initViews() {
        this.barsView[Game.State.OnGoing.ordinal()] = (LinearLayout) findViewById(R.id.ongoing_bar);
        this.barsView[Game.State.Territories.ordinal()] = (LinearLayout) findViewById(R.id.territory_bar);
        this.barsView[Game.State.Over.ordinal()] = (LinearLayout) findViewById(R.id.over_bar);
        this.barsView[Game.State.Review.ordinal()] = (LinearLayout) findViewById(R.id.review_bar);

        int gobanSize = getScreenSize();

        gobanLayout.getLayoutParams().height = gobanSize;
        gobanLayout.getLayoutParams().width = gobanSize;

        grid = new BoardView(this, config, game, gobanSize);
        gobanLayout.addView(grid);

        toolBar.setTitle(blackPlayer.getName() + " vs " + whitePlayer.getName());

        onNextTurn();
    }

    @OptionsItem
    @Background(serial = "save")
    void actionSave() {
        sgfHandler.save(game);
    }

    @Override
    public void onStateChange(Game.State oldState, Game.State newState) {
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
                toolBar.setSubtitle(game.getResult());
                break;
        }
        grid.invalidate();
    }

    @Override
    public void onNextTurn() {
        switch (game.getState()) {
            case OnGoing:
                showCurrentPlayer();
                if (game.getCurrentPlayer().getAi()) {
                    bots[game.getCurrentColor().ordinal()].play();
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
        int blackScore = game.history.score.getCapturedStones(SColor.BLACK)
                + game.history.score.getMarkedDead(SColor.WHITE);
        int whiteScore = game.history.score.getCapturedStones(SColor.WHITE)
                + game.history.score.getMarkedDead(SColor.BLACK);
        //scoresView[SColor.BLACK.ordinal()].setText("(" + blackScore + ")");
        //scoresView[SColor.WHITE.ordinal()].setText("(" + whiteScore + ")");
    }

    @Click
    void firstButtonClicked() {
        if (game.history.hasPrev()) {
            game.goban.clear();
            game.history.goFirst();
            updateReviewButtons();
            grid.invalidate();
        }
    }

    @Click
    void previousButtonClicked() {
        if (game.history.hasPrev()) {
            game.history.undo();
            updateReviewButtons();
            grid.invalidate();
        }
    }

    @Click
    void nextButtonClicked() {
        if (game.history.hasNext()) {
            game.history.playNext();
            updateReviewButtons();
            grid.invalidate();
        }
    }

    @Click
    void lastButtonClicked() {
        while (game.history.hasNext()) {
            game.history.playNext();
        }
        updateReviewButtons();
        grid.invalidate();
    }

    @Click
    void undoButtonClicked() {
        if (game.history.hasPrev()) {
            game.history.undo();
            grid.invalidate();
        }
    }

    @Click
    public void passButtonClicked() {
        game.pass();
        grid.invalidate();
    }

    @Click
    void giveUpButtonClicked() {
        game.setEndOfGame(Game.EndOfGame.GiveUp);
    }

    @Click
    void cancelButtonClicked() {
        game.setState(Game.State.OnGoing);
    }

    @Click
    void proceedButtonClicked() {
        game.setEndOfGame(Game.EndOfGame.Standard);
    }

    @Click
    void reviewButtonClicked() {
        game.setState(Game.State.Review);
    }

    @Click
    void playAgainButtonClicked() {
        sgfHandler.initFileName();
        game.clear();
    }
    
    void showCurrentPlayer() {
        SColor color = game.getCurrentColor();
        toolBar.setLogo(color.equals(SColor.BLACK) ?
                R.drawable.stone_black :
                R.drawable.stone_white
        );
        toolBar.setLogoDescription(color.toString());
        toolBar.setSubtitle(game.getCurrentPlayer().getName() + " to play");
    }

    void updateReviewButtons() {
        firstButton.setEnabled(game.history.hasPrev());
        previousButton.setEnabled(game.history.hasPrev());
        nextButton.setEnabled(game.history.hasNext());
        lastButton.setEnabled(game.history.hasNext());
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