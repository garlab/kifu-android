package fr.narwhals.go.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import fr.narwhals.go.R;

public class MainActivity extends Activity {
	final String howToPlayUrl = "http://en.wikipedia.org/wiki/Go_(game)";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
	
	public void play(View v) {
		Intent intent = new Intent(this, NewGameActivity.class);
		startActivity(intent);
	}
	
	public void preferences(View v) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	public void howToPlay(View v) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(howToPlayUrl));
		startActivity(intent);
	}
}
