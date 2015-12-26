package fr.narwhals.go.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import org.androidannotations.annotations.*;

import fr.narwhals.go.R;
import fr.narwhals.go.domain.Game.Rule;
import fr.narwhals.go.domain.Player;
import fr.narwhals.go.domain.Section.SColor;

@NoTitle
@EActivity(R.layout.new_game)
public class NewGameActivity extends Activity {

    @ViewById SeekBar handicapSeekBar;
    @ViewById RadioGroup sizeRadioGroup;
    @ViewById RadioGroup ruleRadioGroup;
    @ViewById TextView handicapTextView;
    @ViewById EditText whitePlayerEditText;
    @ViewById EditText blackPlayerEditText;
    @ViewById CheckBox blackAiCheckBox;
    @ViewById CheckBox whiteAiCheckBox;

    final static int[] sizes = { 9, 13, 19 };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Click
    void playButtonClicked() {
        Intent intent = new Intent(this, GameActivity_.class);
        intent.putExtra("size", getSize());
        intent.putExtra("handicap", getHandicap());
        intent.putExtra("rule", getRule());

        intent.putExtra("blackPlayer", getPlayer(SColor.BLACK,
                blackPlayerEditText.getText().toString(),
                getString(R.string.black),
                blackAiCheckBox.isChecked()
        ));

        intent.putExtra("whitePlayer", getPlayer(SColor.WHITE,
                whitePlayerEditText.getText().toString(),
                getString(R.string.white),
                whiteAiCheckBox.isChecked()
        ));

        startActivity(intent);
    }

    @Click
    void decreaseHandicapButtonClicked() {
        handicapSeekBar.setProgress(handicapSeekBar.getProgress() - 1);
        handicapTextView.setText(String.valueOf(handicapSeekBar.getProgress()));
    }

    @Click
    void increaseHandicapButtonClicked() {
        handicapSeekBar.setProgress(handicapSeekBar.getProgress() + 1);
        handicapTextView.setText(String.valueOf(handicapSeekBar.getProgress()));
    }

    @SeekBarProgressChange
    void handicapSeekBar() {
        handicapTextView.setText(String.valueOf(handicapSeekBar.getProgress()));
    }

    @AfterViews
    void initHandicap() {
        handicapTextView.setText(String.valueOf(handicapSeekBar.getProgress()));
    }

    Player getPlayer(SColor color, String name, String defaultName, boolean ai) {
        return new Player(color, "".equals(name) ? defaultName : name, ai);
    }

    int getHandicap() {
        return handicapSeekBar.getProgress();
    }

    Rule getRule() {
        RadioButton rb = (RadioButton) findViewById(ruleRadioGroup.getCheckedRadioButtonId());
        return rb.getId() == R.id.japanese ? Rule.Japanese : Rule.Chinese;
    }

    int getSize() {
        View checkedButton = findViewById(sizeRadioGroup.getCheckedRadioButtonId());
        int sizeId = sizeRadioGroup.indexOfChild(checkedButton);
        return sizes[sizeId];
    }
}
