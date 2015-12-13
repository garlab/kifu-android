package fr.narwhals.go.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import fr.narwhals.go.R;
import fr.narwhals.go.ai.OffensiveAI;
import fr.narwhals.go.domain.Game;
import fr.narwhals.go.domain.Game.Rule;
import fr.narwhals.go.domain.Go;
import fr.narwhals.go.domain.Player;
import fr.narwhals.go.domain.Section.SColor;

public class NewGameActivity extends Activity implements OnSeekBarChangeListener {

	private SeekBar handicapView;
	private RadioGroup sizeView;
	private RadioGroup ruleView;
	private TextView h_value;
	private EditText whiteView;
	private EditText blackView;
	private CheckBox aiBlackView;
	private CheckBox aiWhiteView;

	public final static int[] sizes = { 9, 13, 19 };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.new_game);

		handicapView = (SeekBar) findViewById(R.id.handicap);
		sizeView = (RadioGroup) findViewById(R.id.size);
		ruleView = (RadioGroup) findViewById(R.id.rule);
		h_value = (TextView) findViewById(R.id.hvalue);
		whiteView = (EditText) findViewById(R.id.player_white);
		blackView = (EditText) findViewById(R.id.player_black);
		aiBlackView = (CheckBox) findViewById(R.id.ai_black);
		aiWhiteView = (CheckBox) findViewById(R.id.ai_white);

		h_value.setText(String.valueOf(handicapView.getProgress()));
		handicapView.setOnSeekBarChangeListener(this);
	}

	public void play(View v) {
		Game game = new Game(getSize(), getHandicap(), getRule());
		Player[] players = getPlayers(game.getHandicap());
		Go go = new Go(game, players);
		addAI(go);

		Intent intent = new Intent(this, GameActivity.class);
		intent.putExtra("go", go);
		startActivity(intent);
	}

	public void addAI(Go go) {
		Player[] players = go.players;
		if (aiBlackView.isChecked()) {
			int i = SColor.BLACK.ordinal();
			players[i].setAI(new OffensiveAI(go, players[i]));
		}
		if (aiWhiteView.isChecked()) {
			int i = SColor.WHITE.ordinal();
			players[i].setAI(new OffensiveAI(go, players[i]));
		}
	}

	public Player[] getPlayers(int handicap) {

		String whiteName = whiteView.getText().toString();
		String blackName = blackView.getText().toString();

		if (whiteName.equals("")) {
			whiteName = getString(R.string.white);
		}

		if (blackName.equals("")) {
			blackName = getString(R.string.black);
		}

		Player[] players = new Player[2];
		players[SColor.BLACK.ordinal()] = new Player(SColor.BLACK, blackName);
		players[SColor.WHITE.ordinal()] = new Player(SColor.WHITE, whiteName);

		return players;
	}

	public int getHandicap() {
		return handicapView.getProgress();
	}

	public Rule getRule() {
		RadioButton rb = (RadioButton) findViewById(ruleView.getCheckedRadioButtonId());
		return rb.getId() == R.id.japanese ? Rule.Japanese : Rule.Chinese;
	}

	public int getSize() {
		int sizeId = sizeView.indexOfChild(findViewById(sizeView.getCheckedRadioButtonId()));
		return sizes[sizeId];
	}

	public void plus(View v) {
		handicapView.setProgress(handicapView.getProgress() + 1);
		h_value.setText(String.valueOf(handicapView.getProgress()));
	}

	public void minus(View v) {
		handicapView.setProgress(handicapView.getProgress() - 1);
		h_value.setText(String.valueOf(handicapView.getProgress()));
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		h_value.setText(String.valueOf(handicapView.getProgress()));
	}
}
