package com.example.soundmatch.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.soundmatch.R
import com.example.soundmatch.databinding.ActivityUserProfileBinding
import com.example.soundmatch.model.UserProfile

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeningModeSpinner()
        binding.btnGoQuiz.setOnClickListener { onGoQuizClicked() }
    }

    private fun setupListeningModeSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.listening_modes,
            R.layout.item_spinner
        )
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinnerListeningMode.adapter = adapter
    }

    private fun onGoQuizClicked() {
        val name = binding.etName.text?.toString()?.trim().orEmpty()
        val artist = binding.etArtist.text?.toString()?.trim().orEmpty()

        binding.tilName.error = if (name.isEmpty()) getString(R.string.error_required) else null
        binding.tilArtist.error = if (artist.isEmpty()) getString(R.string.error_required) else null

        if (name.isEmpty() || artist.isEmpty()) {
            Toast.makeText(this, R.string.toast_fill_fields, Toast.LENGTH_SHORT).show()
            return
        }

        val profile = UserProfile(
            name = name,
            favoriteArtist = artist,
            listeningMode = binding.spinnerListeningMode.selectedItem.toString(),
            playsInstrument = binding.switchInstrument.isChecked
        )
        openQuiz(profile)
    }

    private fun openQuiz(profile: UserProfile) {
        val intent = Intent(this, SoundQuizActivity::class.java)
            .putExtra(SoundQuizActivity.EXTRA_PROFILE, profile)
        startActivity(intent)
    }
}
