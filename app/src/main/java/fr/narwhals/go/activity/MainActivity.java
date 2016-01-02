package fr.narwhals.go.activity;

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
        actionPreferences();
    }

    @Click
    void helpButtonClicked() {
        actionHowToPlay();
    }
}
