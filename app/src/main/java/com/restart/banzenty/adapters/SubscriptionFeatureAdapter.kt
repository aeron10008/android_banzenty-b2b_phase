package com.restart.banzenty.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.restart.banzenty.R
import com.restart.banzenty.data.models.StationService
import com.restart.banzenty.databinding.SubscriptionFeatureItemBinding
import java.util.Locale

class SubscriptionFeatureAdapter() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = "StationServiceAdapter"

    private val DIFF_CALLBACK =
        object : DiffUtil.ItemCallback<StationService>() {
            override fun areItemsTheSame(
                oldItem: StationService,
                newItem: StationService
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: StationService,
                newItem: StationService
            ): Boolean {
                return oldItem == newItem
            }
        }

    private val differ = AsyncListDiffer(
        CustomRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )

    inner class CustomRecyclerChangeCallback(private val adapter: SubscriptionFeatureAdapter) :
        ListUpdateCallback {

        override fun onInserted(position: Int, count: Int) {
            adapter.notifyItemRangeChanged(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyDataSetChanged()
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyDataSetChanged()
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapter.notifyItemRangeChanged(position, count, payload)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CustomItemViewHolder(
            SubscriptionFeatureItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CustomItemViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return differ.currentList[position].id
    }

    fun submitList(list: List<StationService>) {
        differ.submitList(list)
    }

    class CustomItemViewHolder(
        private val binding: SubscriptionFeatureItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stationService: StationService) = with(binding.root) {
            if (stationService.discount == 0) {
                binding.tvSubscriptionFeature.text = stationService.name
            } else {
                binding.tvSubscriptionFeature.text = String.format(
                    Locale.getDefault(),
                    "%s %s %s",
                    stationService.name,
                    itemView.context.getString(
                        R.string.discount, stationService.discount
                    ),
                    "%"
                )

            }
            if (stationService.limit != null)
                binding.tvSubscriptionFeature.text =
                    String.format(
                        Locale.getDefault(),
                        "%d %s",
                        stationService.limit?.minus(stationService.used ?: 0),
                        binding.tvSubscriptionFeature.text.toString().trim()
                    )

        }
    }
}