package fr.narwhals.go.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.narwhals.go.R;

public class SavedGamesActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.saved_games);

		clean();
		printFiles();
	}

	public void printFiles() {
		int i = 0;
		LinearLayout savedGames = (LinearLayout) findViewById(R.id.saved_games);
		for (String file : fileList()) {
			LinearLayout savedGame = (LinearLayout)getLayoutInflater().inflate(R.layout.saved_game_inflate, null);
			
			LinearLayout gameInfo = (LinearLayout)savedGame.getChildAt(1);
			TextView players = (TextView)gameInfo.getChildAt(0);
			TextView date = (TextView)gameInfo.getChildAt(1);
			TextView result = (TextView)gameInfo.getChildAt(2);
			
			players.setText(file);
			date.setText(String.valueOf(i));
			result.setText("B+R");
			
			savedGames.addView(savedGame);
			++i;
		}
	}
	
	public void clean() {
		for (String file : fileList()) {
			deleteFile(file);
		}
	}
}
