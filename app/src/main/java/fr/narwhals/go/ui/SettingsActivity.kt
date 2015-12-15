package fr.narwhals.go.ui

import android.os.Bundle
import android.preference.PreferenceActivity
import fr.narwhals.go.R

class SettingsActivity : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
            TODO: use Preference fragment,
            see http://stackoverflow.com/questions/6822319/what-to-use-instead-of-addpreferencesfromresource-in-a-preferenceactivity
        */
        addPreferencesFromResource(R.xml.preferences)
    }
}
