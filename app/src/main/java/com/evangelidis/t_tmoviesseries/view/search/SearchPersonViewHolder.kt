package com.evangelidis.t_tmoviesseries.view.search

import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.databinding.ItemPersonBinding
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.model.Multisearch
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.ItemsManager

class SearchPersonViewHolder(private val binding: ItemPersonBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(trend: Multisearch, callback: SearchCallback) {
        with(binding) {
            itemPersonName.text = trend.name
            itemPersonPopularFor.text = trend.knownForDepartment
            trend.knownFor?.let {
                it.sortedBy { it.voteCount }
                itemPersonKnowFor.text = it.firstOrNull()?.title
                itemPersonKnowFor.show()
            }
            ItemsManager.getGlideImage(itemView.context, Constants.IMAGE_SMALL_BASE_URL.plus(trend.profilePath), itemPersonPoster)

            root.setOnClickListener { callback.navigateToPerson(trend.id) }
        }
    }
}
