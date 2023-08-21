package com.restart.banzenty.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.restart.banzenty.data.models.RewardsModel
import com.restart.banzenty.databinding.RedeemingServiceItemBinding
import com.restart.banzenty.ui.main.RedeemCodeActivity

class RewardsRecyclerAdapter(val requestManager: RequestManager, val activity: Activity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = "rewardsAdapter"
    lateinit var binding: RedeemingServiceItemBinding

    private val DIFF_CALLBACK =
        object : DiffUtil.ItemCallback<RewardsModel.Reward>() {
            override fun areItemsTheSame(
                oldItem: RewardsModel.Reward,
                newItem: RewardsModel.Reward
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: RewardsModel.Reward,
                newItem: RewardsModel.Reward
            ): Boolean {
                return oldItem == newItem
            }
        }

    private val differ = AsyncListDiffer(
        CustomRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )

    inner class CustomRecyclerChangeCallback(private val adapter: RewardsRecyclerAdapter) :
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
            RedeemingServiceItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            requestManager, activity
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CustomItemViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return differ.currentList[position].id
    }

    fun submitList(list: List<RewardsModel.Reward>) {
        differ.submitList(list)
    }

    fun preloadGlideImages(requestManager: RequestManager, list: List<RewardsModel.Reward>) {
        for (item in list) {
            requestManager
                .load(item.image?.url).preload()
        }
    }

    class CustomItemViewHolder(
        private val binding: RedeemingServiceItemBinding,
        private val requestManager: RequestManager,
        private val activity: Activity
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reward: RewardsModel.Reward) = with(binding.root) {
            requestManager.load(reward.image?.url).into(binding.imageViewServicePic)
            binding.textViewServiceName.text = reward.name
            itemView.setOnClickListener {
                val intent = Intent(activity, RedeemCodeActivity::class.java)
                intent.putExtra("reward_id", reward.id)
                activity.startActivity(intent)
            }
        }
    }
}