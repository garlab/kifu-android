package fr.narwhals.go.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import fr.narwhals.go.R;

@EActivity(R.layout.main)
public class MainActivity extends Activity {
    final static String howToPlayUrl = "http://en.wikipedia.org/wiki/Go_(game)";

    @Click
    void playButtonClicked() {
        startActivity(H.newGame(this));
    }

    @Click
    void preferencesButtonClicked() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Click
    void helpButtonClicked() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(howToPlayUrl));
        startActivity(intent);
    }
}
