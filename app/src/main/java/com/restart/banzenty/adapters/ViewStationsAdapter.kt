package com.restart.banzenty.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.restart.banzenty.data.models.StationModel.Station
import com.restart.banzenty.databinding.ViewStationItemBinding

class ViewStationsAdapter(
    val requestManager: RequestManager
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "ViewStationsAdapter"


    private val DIFF_CALLBACK =
        object : DiffUtil.ItemCallback<Station>() {

            override fun areItemsTheSame(
                oldItem: Station,
                newItem: Station
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Station,
                newItem: Station
            ): Boolean {
                return oldItem == newItem
            }

        }
    private val differ = AsyncListDiffer(
        CustomRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )

    inner class CustomRecyclerChangeCallback(private val adapter: ViewStationsAdapter) :
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
            ViewStationItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            requestManager
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

    fun submitList(list: List<Station>) {
        differ.submitList(list)
    }

    class CustomItemViewHolder(
        private val binding: ViewStationItemBinding,
        private val requestManager: RequestManager
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(station: Station) = with(binding.root) {
            requestManager.load(station.companyImage?.url)
                .transform(CenterCrop(), CircleCrop())
                .into(binding.ivStationCompany)
            binding.tvStationName.text = station.name
        }
    }
}
