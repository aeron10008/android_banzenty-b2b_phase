package com.restart.banzenty.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.bumptech.glide.RequestManager
import com.restart.banzenty.data.models.HomeModel
import com.restart.banzenty.databinding.ItemAdBinding
import com.smarteist.autoimageslider.SliderViewAdapter

class AdsAdapter(private val requestManager: RequestManager) :
    SliderViewAdapter<AdsAdapter.SliderAdapterVH>() {

    private val DIFF_CALLBACK =
        object : DiffUtil.ItemCallback<HomeModel.Banner>() {

            override fun areItemsTheSame(
                oldItem: HomeModel.Banner,
                newItem: HomeModel.Banner
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: HomeModel.Banner,
                newItem: HomeModel.Banner
            ): Boolean {
                return oldItem == newItem
            }

        }
    private val differ = AsyncListDiffer(
        CustomRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )


    inner class CustomRecyclerChangeCallback(private val adapter: AdsAdapter) :
        ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
//            adapter.notifyItemRangeChanged(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyDataSetChanged()
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyDataSetChanged()
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
//            adapter.notifyItemRangeChanged(position, count, payload)
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        return SliderAdapterVH(
            ItemAdBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            requestManager
        )

    }

    override fun onBindViewHolder(holder: SliderAdapterVH?, position: Int) {
        when (holder) {
            is SliderAdapterVH -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getCount(): Int {
        return differ.currentList.size
    }
    fun submitList(list: List<HomeModel.Banner>) {
        differ.submitList(list)
    }


    class SliderAdapterVH(
        val binding: ItemAdBinding,
        val requestManager: RequestManager
    ) : ViewHolder(binding.root) {
        fun bind(banner: HomeModel.Banner) = with(binding.root) {
            requestManager.load(banner.image?.url).into(binding.ivPic)
        }
    }
}