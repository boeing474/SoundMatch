package com.example.soundmatch.ui

import android.content.Intent
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.soundmatch.R
import com.example.soundmatch.databinding.ActivitySoundQuizBinding
import com.example.soundmatch.databinding.ItemArtistCardBinding
import com.example.soundmatch.logic.DiscoveryEngine
import com.example.soundmatch.logic.ShownSongs
import com.example.soundmatch.logic.SoundMatchEngine
import com.example.soundmatch.model.DeezerArtist
import com.example.soundmatch.model.Energy
import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.Popularity
import com.example.soundmatch.model.QuizAnswers
import com.example.soundmatch.model.SoundResult
import com.example.soundmatch.model.UserProfile
import com.example.soundmatch.network.DiscoveryRepository
import com.example.soundmatch.network.ImageLoader
import com.example.soundmatch.network.networkOrNull
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

// Quiz com uma pergunta por vez, na mesma Activity (como o perfil).
// Os cards das perguntas estão no XML e aqui só trocamos quais ficam visíveis.
class SoundQuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoundQuizBinding
    private lateinit var profile: UserProfile
    private var currentStep = 0

    // Um passo do quiz: os cards que aparecem nele e, se houver, a pergunta
    // de escolha única que precisa de resposta para liberar o botão.
    private class Step(val cards: List<View>, val question: RadioGroup? = null)

    // Com artistas parecidos (vindos do Deezer), o quiz é o da descoberta.
    // Sem eles fica o quiz reserva: clima + outros estilos, e a música sai do catálogo fixo.
    private val hasRelatedArtists: Boolean get() = profile.relatedArtists.isNotEmpty()

    private val steps: List<Step> by lazy {
        if (hasRelatedArtists) {
            listOf(
                Step(listOf(binding.cardKnown)),
                Step(listOf(binding.cardEnergy), binding.rgEnergy),
                Step(listOf(binding.cardPopularity, binding.cardRisk), binding.rgPopularity)
            )
        } else {
            listOf(
                Step(listOf(binding.cardEnergy), binding.rgEnergy),
                Step(listOf(binding.cardStyles, binding.cardRisk))
            )
        }
    }

    private val allCards: List<View> by lazy {
        listOf(binding.cardKnown, binding.cardEnergy, binding.cardPopularity, binding.cardStyles, binding.cardRisk)
    }

    // Cada card da pergunta dos parecidos e o artista que ele representa.
    private val artistCards = mutableMapOf<MaterialCardView, DeezerArtist>()

    private val styleChecks by lazy {
        mapOf(
            binding.cbPop to Genre.POP,
            binding.cbRock to Genre.ROCK,
            binding.cbHiphop to Genre.HIPHOP,
            binding.cbElectronic to Genre.ELECTRONIC,
            binding.cbBrazilian to Genre.BRAZILIAN
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoundQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profile = IntentCompat.getSerializableExtra(intent, EXTRA_PROFILE, UserProfile::class.java)
            ?: run { finish(); return }

        if (hasRelatedArtists) showKnownQuestion()
        Energy.entries.forEach { addOption(binding.rgEnergy, it.label, it.hint) }
        Popularity.entries.forEach { addOption(binding.rgPopularity, it.label, it.hint) }
        binding.tvRiskHint.setText(if (hasRelatedArtists) R.string.risk_hint else R.string.risk_hint_reserve)
        binding.progressQuiz.max = steps.size

        // Respondeu a pergunta do passo: libera o botão.
        listOf(binding.rgEnergy, binding.rgPopularity).forEach { group ->
            group.setOnCheckedChangeListener { _, _ -> updateNextButton() }
        }
        binding.btnNext.setOnClickListener { onNextClicked() }
        binding.btnBack.setOnClickListener { showStep(currentStep - 1) }

        // O "voltar" do celular volta uma pergunta antes de sair da tela.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentStep > 0) showStep(currentStep - 1) else finish()
            }
        })

        showStep(0)
    }

    // Esconde todos os cards e mostra só os do passo pedido.
    private fun showStep(step: Int) {
        currentStep = step
        allCards.forEach { it.isVisible = it in steps[step].cards }

        binding.tvProgress.text = getString(R.string.quiz_progress, step + 1, steps.size)
        binding.progressQuiz.progress = step + 1
        binding.btnBack.isVisible = step > 0
        binding.scrollQuiz.scrollTo(0, 0)
        updateNextButton()
    }

    // No último passo o botão vira "Ver Minha Música". Enquanto a pergunta
    // do passo não tem resposta, ele fica desabilitado.
    private fun updateNextButton() {
        val question = steps[currentStep].question
        binding.btnNext.isEnabled = question == null || question.checkedRadioButtonId != View.NO_ID
        binding.btnNext.setText(if (currentStep == steps.lastIndex) R.string.btn_see_result else R.string.btn_next)
    }

    private fun onNextClicked() {
        if (currentStep == steps.lastIndex) showResult() else showStep(currentStep + 1)
    }

    // "Outros artistas que você curte": um card com foto para cada parecido com o favorito.
    // O card marcado ganha o contorno ciano e o ícone de confirmação (isChecked do MaterialCardView).
    private fun showKnownQuestion() {
        binding.tvKnownHint.text = getString(R.string.known_hint, profile.favoriteArtist)
        profile.relatedArtists.forEach { artist ->
            val item = ItemArtistCardBinding.inflate(layoutInflater, binding.gridArtists, false)
            item.tvArtist.text = artist.name
            item.root.setOnClickListener {
                item.root.isChecked = !item.root.isChecked
                updateKnownCount()
            }
            binding.gridArtists.addView(item.root)
            artistCards[item.root] = artist
            artist.picture?.let { loadPhoto(item.imgArtist, it) }
        }
        updateKnownCount()
    }

    // A foto vem da internet. Se o download falhar, o card fica com o vinil.
    private fun loadPhoto(image: ImageView, url: String) {
        lifecycleScope.launch {
            networkOrNull { ImageLoader.load(url) }?.let { image.setImageBitmap(it) }
        }
    }

    // "2 marcados · 6 na disputa", logo abaixo dos cards.
    private fun updateKnownCount() {
        val known = artistCards.keys.count { it.isChecked }
        val left = artistCards.size - known
        binding.tvKnownCount.text = when {
            known == 0 -> getString(R.string.known_none)
            left == 0 -> getString(R.string.known_all)
            else -> resources.getQuantityString(R.plurals.known_count, known, known, left)
        }
    }

    // Cada opção aparece com o nome em negrito e a explicação menor embaixo.
    private fun addOption(group: RadioGroup, label: String, hint: String) {
        val muted = ContextCompat.getColor(this, R.color.text_secondary)
        val option = layoutInflater.inflate(R.layout.item_quiz_option, group, false) as RadioButton
        option.id = View.generateViewId()
        option.text = buildSpannedString {
            bold { append(label) }
            append("\n")
            inSpans(RelativeSizeSpan(0.85f), ForegroundColorSpan(muted)) { append(hint) }
        }
        group.addView(option)
    }

    private fun showResult() {
        val answers = QuizAnswers(
            energy = Energy.entries[selectedIndex(binding.rgEnergy)],
            likedGenres = styleChecks.filterKeys { it.isChecked }.values.toList(),
            surprise = binding.switchRisk.isChecked,
            knownArtists = artistCards.filterKeys { it.isChecked }.values.toList(),
            popularity = Popularity.entries.getOrNull(selectedIndex(binding.rgPopularity))
        )
        if (hasRelatedArtists) discover(answers) else recommendFromCatalog(answers)
    }

    // Busca as músicas dos artistas parecidos e escolhe a mais perto do clima que ainda não saiu.
    // Se a busca falhar ou não vier nada que dê para medir, a música sai do catálogo fixo.
    private fun discover(answers: QuizAnswers) {
        lifecycleScope.launch {
            setSearching(true)
            val candidates = networkOrNull { DiscoveryRepository.candidatesFor(profile, answers) }.orEmpty()
            val result = DiscoveryEngine.recommend(profile, answers, candidates, ShownSongs.all)
                ?: SoundMatchEngine.recommend(profile, answers)
            ShownSongs.remember(result.song)   // para o próximo quiz não repetir esta música
            setSearching(false)
            openResult(result)
        }
    }

    private fun recommendFromCatalog(answers: QuizAnswers) {
        val result = SoundMatchEngine.recommend(profile, answers)

        // Quem quer se arriscar precisa marcar um estilo além do de sempre.
        if (answers.surprise && !result.surprise) {
            val message = getString(R.string.toast_surprise_needs_style, result.genre?.title)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            return
        }
        openResult(result)
    }

    // Durante a busca aparece o aviso com a ProgressBar e os botões ficam travados.
    private fun setSearching(searching: Boolean) {
        binding.layoutSearching.isVisible = searching
        binding.btnNext.isEnabled = !searching
        binding.btnBack.isEnabled = !searching
    }

    private fun openResult(result: SoundResult) {
        val intent = Intent(this, ResultActivity::class.java)
            .putExtra(ResultActivity.EXTRA_RESULT, result)
        startActivity(intent)
    }

    // A posição da opção marcada, que é a mesma posição no enum (Energy ou Popularity).
    // Sem opção marcada devolve -1.
    private fun selectedIndex(group: RadioGroup): Int =
        group.indexOfChild(group.findViewById<View>(group.checkedRadioButtonId))

    companion object {
        const val EXTRA_PROFILE = "extra_profile"
    }
}
