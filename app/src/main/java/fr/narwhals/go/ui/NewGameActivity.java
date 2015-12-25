package fr.narwhals.go.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.*;

import org.androidannotations.annotations.*;

import fr.narwhals.go.R;
import fr.narwhals.go.domain.Game.Rule;
import fr.narwhals.go.domain.Player;
import fr.narwhals.go.domain.Section.SColor;

@NoTitle
@EActivity(R.layout.new_game)
public class NewGameActivity extends Activity {

    @ViewById(R.id.handicap) SeekBar handicapView;
    @ViewById(R.id.size) RadioGroup sizeView;
    @ViewById(R.id.rule) RadioGroup ruleView;
    @ViewById(R.id.hvalue) TextView h_value;
    @ViewById(R.id.player_white) EditText whiteView;
    @ViewById(R.id.player_black) EditText blackView;
    @ViewById(R.id.ai_black) CheckBox aiBlackView;
    @ViewById(R.id.ai_white) CheckBox aiWhiteView;

    public final static int[] sizes = { 9, 13, 19 };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Click(R.id.play)
    void play() {
        Intent intent = new Intent(this, GameActivity_.class);
        intent.putExtra("size", getSize());
        intent.putExtra("handicap", getHandicap());
        intent.putExtra("rule", getRule());

        intent.putExtra("blackPlayer", getPlayer(SColor.BLACK,
                blackView.getText().toString(),
                getString(R.string.black),
                aiBlackView.isChecked()
        ));

        intent.putExtra("whitePlayer", getPlayer(SColor.WHITE,
                whiteView.getText().toString(),
                getString(R.string.white),
                aiWhiteView.isChecked()
        ));

        startActivity(intent);
    }

    @Click(R.id.h_plus)
    void plus() {
        handicapView.setProgress(handicapView.getProgress() + 1);
        h_value.setText(String.valueOf(handicapView.getProgress()));
    }

    @Click(R.id.h_minus)
    void minus() {
        handicapView.setProgress(handicapView.getProgress() - 1);
        h_value.setText(String.valueOf(handicapView.getProgress()));
    }

    @SeekBarProgressChange(R.id.handicap)
    void updateHandicap() {
        h_value.setText(String.valueOf(handicapView.getProgress()));
    }

    @AfterViews
    void initHandicap() {
        h_value.setText(String.valueOf(handicapView.getProgress()));
    }

    Player getPlayer(SColor color, String name, String defaultName, boolean ai) {
        return new Player(color, name.equals("") ? defaultName : name, ai);
    }

    int getHandicap() {
        return handicapView.getProgress();
    }

    Rule getRule() {
        RadioButton rb = (RadioButton) findViewById(ruleView.getCheckedRadioButtonId());
        return rb.getId() == R.id.japanese ? Rule.Japanese : Rule.Chinese;
    }

    int getSize() {
        int sizeId = sizeView.indexOfChild(findViewById(sizeView.getCheckedRadioButtonId()));
        return sizes[sizeId];
    }
}
