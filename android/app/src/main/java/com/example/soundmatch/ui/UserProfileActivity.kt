package com.example.soundmatch.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.soundmatch.R
import com.example.soundmatch.databinding.ActivityUserProfileBinding
import com.example.soundmatch.logic.ArtistName
import com.example.soundmatch.logic.TrackPicker
import com.example.soundmatch.model.ArtistSong
import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.Instrument
import com.example.soundmatch.model.Situation
import com.example.soundmatch.model.UserProfile
import com.example.soundmatch.network.ImageLoader
import com.example.soundmatch.network.RetrofitClient
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Perfil em formato de conversa: uma pergunta por vez, na mesma Activity.
// Os 4 passos estão no XML e aqui só trocamos qual deles fica visível.
class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private var currentStep = 0

    // Resultado da busca do artista no iTunes. Fica null até a busca terminar.
    private var foundArtist: String? = null
    private var artistGenre: Genre? = null
    private var artistSongs: List<ArtistSong> = emptyList()
    private var artistSearched = false

    private val steps: List<View> by lazy {
        listOf(binding.stepName, binding.stepArtist, binding.stepSituation, binding.stepInstrument)
    }

    private val situationChips by lazy {
        mapOf(
            binding.chipRelaxing.id to Situation.RELAXING,
            binding.chipGym.id to Situation.GYM,
            binding.chipStreet.id to Situation.STREET,
            binding.chipParty.id to Situation.PARTY,
            binding.chipStudying.id to Situation.STUDYING
        )
    }

    // "Não toco" fica fora do mapa: get() devolve null, que é justamente "sem instrumento".
    private val instrumentChips by lazy {
        mapOf(
            binding.chipGuitar.id to Instrument.GUITAR,
            binding.chipDrums.id to Instrument.DRUMS,
            binding.chipBass.id to Instrument.BASS,
            binding.chipKeys.id to Instrument.KEYS,
            binding.chipVocals.id to Instrument.VOCALS
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.setOnClickListener { onNextClicked() }
        binding.btnBack.setOnClickListener { showStep(currentStep - 1) }

        // O "Enter" do teclado faz o mesmo que o botão Continuar.
        binding.etName.setOnEditorActionListener { _, _, _ -> onNextClicked(); true }
        binding.etArtist.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) onNextClicked()
            true
        }
        // Mudou o nome do artista: a busca anterior não vale mais.
        binding.etArtist.doAfterTextChanged { resetArtistSearch() }

        // O botão "voltar" do celular volta um passo antes de sair da tela.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentStep > 0) showStep(currentStep - 1) else finish()
            }
        })

        showStep(0)
    }

    private fun showStep(step: Int) {
        currentStep = step
        steps.forEachIndexed { index, view ->
            view.visibility = if (index == step) View.VISIBLE else View.GONE
        }
        binding.tvStep.text = getString(R.string.profile_step, step + 1, steps.size)
        binding.progressSteps.progress = step + 1
        binding.tvLead.text = leadFor(step)
        binding.btnBack.visibility = if (step == 0) View.GONE else View.VISIBLE
        moveSun(step)
        updateNextButton()
    }

    // A frase de cima usa a resposta anterior, como numa conversa.
    private fun leadFor(step: Int): String {
        val name = binding.etName.text.toString().trim()
        val artist = foundArtist ?: binding.etArtist.text.toString().trim()
        return when (step) {
            STEP_ARTIST -> getString(R.string.step_lead_artist, name)
            STEP_SITUATION -> getString(R.string.step_lead_situation, artist)
            STEP_INSTRUMENT -> getString(R.string.step_lead_instrument, name)
            else -> getString(R.string.step_lead_name)
        }
    }

    // O sol começa quase escondido no horizonte e sobe um pouco a cada passo.
    private fun moveSun(step: Int) {
        val stepsLeft = steps.lastIndex - step
        val offset = stepsLeft * SUN_STEP_DP * resources.displayMetrics.density
        binding.imgSun.animate().translationY(offset).setDuration(500).start()
    }

    private fun updateNextButton() {
        binding.btnNext.setText(
            when {
                currentStep == STEP_ARTIST && !artistSearched -> R.string.btn_search_artist
                currentStep == steps.lastIndex -> R.string.btn_go_quiz
                else -> R.string.btn_next
            }
        )
    }

    private fun onNextClicked() {
        when (currentStep) {
            STEP_NAME -> if (requireText(binding.tilName, binding.etName.text)) showStep(STEP_ARTIST)
            STEP_ARTIST -> onArtistNext()
            STEP_SITUATION -> {
                if (situationChips[binding.chipGroupSituation.checkedChipId] != null) {
                    showStep(STEP_INSTRUMENT)
                } else {
                    Toast.makeText(this, R.string.toast_pick_option, Toast.LENGTH_SHORT).show()
                }
            }
            STEP_INSTRUMENT -> {
                if (binding.chipGroupInstrument.checkedChipId != View.NO_ID) {
                    openQuiz()
                } else {
                    Toast.makeText(this, R.string.toast_pick_option, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 1º toque: busca o artista e mostra o card. 2º toque (já conferido): segue.
    private fun onArtistNext() {
        if (!requireText(binding.tilArtist, binding.etArtist.text)) return
        if (artistSearched) showStep(STEP_SITUATION) else searchArtist()
    }

    private fun requireText(layout: TextInputLayout, text: CharSequence?): Boolean {
        val ok = !text.isNullOrBlank()
        layout.error = if (ok) null else getString(R.string.error_required)
        return ok
    }

    // Uma busca só no iTunes: o 1º resultado dá o nome oficial, o gênero e a capa,
    // e os outros viram as opções da 1ª pergunta do quiz.
    private fun searchArtist() {
        val typed = binding.etArtist.text.toString().trim()
        hideKeyboard()

        lifecycleScope.launch {
            binding.progressArtist.visibility = View.VISIBLE
            binding.btnNext.isEnabled = false
            try {
                val results = RetrofitClient.itunes.search(typed, limit = ARTIST_SEARCH_LIMIT).results
                val track = results.firstOrNull()
                if (track?.artistName == null) {
                    showArtistNotFound()
                } else {
                    val artist = ArtistName.clean(typed, track.artistName)
                    foundArtist = artist
                    artistGenre = track.primaryGenreName?.let { Genre.fromITunes(it) }
                    artistSongs = TrackPicker.artistSongs(results, artist)
                    showArtistCard(track.primaryGenreName.orEmpty())
                    track.artworkUrl100?.let { url ->
                        ImageLoader.load(ImageLoader.bigCover(url))?.let { binding.imgArtist.setImageBitmap(it) }
                    }
                }
            } catch (e: IOException) {
                showArtistNotFound()   // sem internet: segue só com o que a pessoa digitou
            } catch (e: HttpException) {
                showArtistNotFound()
            } finally {
                artistSearched = true
                binding.progressArtist.visibility = View.GONE
                binding.btnNext.isEnabled = true
                updateNextButton()
            }
        }
    }

    private fun showArtistCard(iTunesGenre: String) {
        binding.tvArtistName.text = foundArtist
        binding.tvArtistGenre.text = if (artistGenre != null) {
            iTunesGenre
        } else {
            getString(R.string.artist_genre_unknown, iTunesGenre)
        }
        // Contorno e pílula na cor do perfil do artista (ciano se o gênero não entrou em nenhum)
        val color = ContextCompat.getColor(this, artistGenre?.colorRes ?: R.color.neon_cyan)
        binding.cardArtist.strokeColor = color
        binding.tvArtistGenre.backgroundTintList = ColorStateList.valueOf(color)
        binding.cardArtist.visibility = View.VISIBLE
        binding.tvArtistNotFound.visibility = View.GONE
    }

    private fun showArtistNotFound() {
        foundArtist = null
        artistGenre = null
        artistSongs = emptyList()
        binding.cardArtist.visibility = View.GONE
        binding.tvArtistNotFound.visibility = View.VISIBLE
    }

    private fun resetArtistSearch() {
        if (!artistSearched) return
        artistSearched = false
        foundArtist = null
        artistGenre = null
        artistSongs = emptyList()
        binding.imgArtist.setImageResource(R.drawable.ic_vinyl)
        binding.cardArtist.visibility = View.GONE
        binding.tvArtistNotFound.visibility = View.GONE
        updateNextButton()
    }

    private fun hideKeyboard() {
        getSystemService(InputMethodManager::class.java)
            .hideSoftInputFromWindow(binding.etArtist.windowToken, 0)
    }

    private fun openQuiz() {
        val profile = UserProfile(
            name = binding.etName.text.toString().trim(),
            favoriteArtist = foundArtist ?: binding.etArtist.text.toString().trim(),
            situation = situationChips.getValue(binding.chipGroupSituation.checkedChipId),
            instrument = instrumentChips[binding.chipGroupInstrument.checkedChipId],
            artistGenre = artistGenre,
            artistSongs = artistSongs
        )
        val intent = Intent(this, SoundQuizActivity::class.java)
            .putExtra(SoundQuizActivity.EXTRA_PROFILE, profile)
        startActivity(intent)
    }

    companion object {
        private const val STEP_NAME = 0
        private const val STEP_ARTIST = 1
        private const val STEP_SITUATION = 2
        private const val STEP_INSTRUMENT = 3
        private const val SUN_STEP_DP = 18f
        private const val ARTIST_SEARCH_LIMIT = 25
    }
}
