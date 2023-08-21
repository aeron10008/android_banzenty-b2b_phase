package com.restart.banzenty.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.restart.banzenty.data.models.StationService
import com.restart.banzenty.databinding.AvailableServicesItemBinding

class StationServicesAdapter(
    val requestManager: RequestManager
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "StationServicesAdapter"


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

    inner class CustomRecyclerChangeCallback(private val adapter: StationServicesAdapter) :
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
            AvailableServicesItemBinding.inflate(
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

    fun submitList(list: List<StationService>) {
        differ.submitList(list)
    }

    fun preloadGlideImages(
        requestManager: RequestManager,
        list: List<StationService>
    ) {
        for (item in list) {
            requestManager
                .load(item.image?.url)
                .preload()
        }
    }

    class CustomItemViewHolder(
        private val binding: AvailableServicesItemBinding,
        private val requestManager: RequestManager
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(service: StationService) = with(binding.root) {
            requestManager.load(service.image?.url)
                .into(binding.imageViewServiceIcon)
            binding.textViewServiceName.text = service.name
        }
    }
}
