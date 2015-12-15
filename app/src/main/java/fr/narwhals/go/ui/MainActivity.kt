package fr.narwhals.go.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import fr.narwhals.go.R

class MainActivity : Activity() {
    internal val howToPlayUrl = "http://en.wikipedia.org/wiki/Go_(game)"

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
    }

    fun play(v: View) {
        val intent = Intent(this, NewGameActivity::class.java)
        startActivity(intent)
    }

    fun preferences(v: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun howToPlay(v: View) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(howToPlayUrl))
        startActivity(intent)
    }
}
