package fr.narwhals.go.activity;

import android.content.Intent;
import android.net.Uri;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import fr.narwhals.go.R;

@EActivity(R.layout.main)
public class MainActivity extends BaseActivity {

    @Click
    void playButtonClicked() {
        NewGameActivity_.intent(this).start();
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
