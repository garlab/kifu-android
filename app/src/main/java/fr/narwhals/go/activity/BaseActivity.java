package fr.narwhals.go.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import fr.narwhals.go.R;

@EActivity
@OptionsMenu(R.menu.menu_main)
public abstract class BaseActivity extends ActionBarActivity {

    @ViewById Toolbar toolBar;
    @StringRes String howToPlayUrl;

    @AfterViews
    void initToolBar() {
        setSupportActionBar(toolBar);
    }

    @OptionsItem
    void actionPreferences() {
        // TODO display preferences fragment
    }

    @OptionsItem
    void actionHowToPlay() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(howToPlayUrl));
        startActivity(intent);
    }
}
