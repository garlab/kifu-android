package fr.narwhals.go.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import fr.narwhals.go.R;

public class SettingsActivity extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
