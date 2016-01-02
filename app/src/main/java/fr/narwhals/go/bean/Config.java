package fr.narwhals.go.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.androidannotations.annotations.*;

@EBean public class Config {
    @RootContext Context context;
    SharedPreferences sp;

    @AfterInject
    void initSharedPreferences() {
        sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean showLastMove() {
        return sp.getBoolean("show_last_move", true);
    }

    public boolean numberMoves() {
        return sp.getBoolean("number_moves", false);
    }

    public boolean tagVariations() {
        return sp.getBoolean("tag_variations", true);
    }

    public boolean aiPass() {
        return sp.getBoolean("ai_pass", true);
    }
}
