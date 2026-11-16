package com.example.soundmatch.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soundmatch.R
import com.example.soundmatch.databinding.ItemRecommendationBinding
import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.RecommendationResponse
import com.example.soundmatch.network.CoverRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Os cliques de curtir e excluir só são repassados: quem chama a API é a HistoryActivity.
// O scope vem da Activity, para a busca das capas parar se a tela for fechada.
class HistoryAdapter(
    private val scope: CoroutineScope,
    private val onToggleRating: (RecommendationResponse) -> Unit,
    private val onDelete: (RecommendationResponse) -> Unit
) : ListAdapter<RecommendationResponse, HistoryAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemRecommendationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecommendationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        val genreColor = ContextCompat.getColor(context, Genre.fromTitle(item.archetype)?.colorRes ?: R.color.neon_cyan)

        with(holder.binding) {
            cardCover.strokeColor = genreColor
            tvSong.text = item.recommendedSong
            tvArtist.text = item.songArtist
            tvGenre.text = context.getString(R.string.item_genre_match, item.archetype, item.score)
            tvGenre.setTextColor(genreColor)
            tvMeta.text = context.getString(R.string.item_meta, item.user.name, formatApiDate(item.createdAt))

            val heartColor = if (item.liked) R.color.neon_magenta else R.color.text_muted
            btnLike.setImageResource(if (item.liked) R.drawable.ic_heart else R.drawable.ic_heart_outline)
            btnLike.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, heartColor))
            btnLike.contentDescription = context.getString(
                if (item.liked) R.string.item_liked_action else R.string.item_disliked_action
            )

            btnLike.setOnClickListener { onToggleRating(item) }
            btnDelete.setOnClickListener { onDelete(item) }

            // O card pode ser reaproveitado para outra música enquanto a capa baixa.
            // A tag confere se ele ainda mostra a mesma música antes de trocar a imagem.
            imgCover.setImageResource(R.drawable.ic_vinyl)
            imgCover.tag = item.id
            scope.launch {
                val cover = CoverRepository.coverFor(item.recommendedSong, item.songArtist)
                if (cover != null && imgCover.tag == item.id) imgCover.setImageBitmap(cover)
            }
        }
    }

    // Compara a lista antiga com a nova para o RecyclerView atualizar só os cards que mudaram.
    object DiffCallback : DiffUtil.ItemCallback<RecommendationResponse>() {
        override fun areItemsTheSame(old: RecommendationResponse, new: RecommendationResponse) = old.id == new.id
        override fun areContentsTheSame(old: RecommendationResponse, new: RecommendationResponse) = old == new
    }

    private companion object {
        // A API grava em UTC ("2026-11-15T22:42:10.123456"); aqui vira o horário local.
        val API_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val DISPLAY_DATE_FORMAT = SimpleDateFormat("dd/MM, HH:mm", Locale("pt", "BR"))

        fun formatApiDate(apiDate: String): String = try {
            API_DATE_FORMAT.parse(apiDate.take(19))?.let { DISPLAY_DATE_FORMAT.format(it) } ?: apiDate
        } catch (e: ParseException) {
            apiDate
        }
    }
}
