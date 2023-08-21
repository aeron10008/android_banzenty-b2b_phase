package com.restart.banzenty.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.restart.banzenty.data.models.NotificationModel
import com.restart.banzenty.databinding.NotificationItemBinding

class NotificationsAdapter(
    val requestManager: RequestManager
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "NotificationsAdapter"

    private val DIFF_CALLBACK =
        object : DiffUtil.ItemCallback<NotificationModel.Notification>() {

            override fun areItemsTheSame(
                oldItem: NotificationModel.Notification,
                newItem: NotificationModel.Notification
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: NotificationModel.Notification,
                newItem: NotificationModel.Notification
            ): Boolean {
                return oldItem == newItem
            }

        }
    private val differ = AsyncListDiffer(
        CustomRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )


    inner class CustomRecyclerChangeCallback(private val adapter: NotificationsAdapter) :
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
            NotificationItemBinding.inflate(
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

    fun submitList(list: List<NotificationModel.Notification>) {
        differ.submitList(list)
    }

    fun preloadGlideImages(
        requestManager: RequestManager,
        list: List<NotificationModel.Notification>
    ) {
        for (item in list) {
            requestManager
                .load(item.image)
                .preload()
        }
    }

    class CustomItemViewHolder(
        private val binding: NotificationItemBinding,
        private val requestManager: RequestManager
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: NotificationModel.Notification) = with(binding.root) {
            binding.tvTitle.text = notification.title
            requestManager.load(notification.image).into(binding.ivPic)
            binding.ivUnreadSign.visibility = View.GONE
//            if (notification.isRead)
//                binding.ivUnreadSign.visibility = View.GONE
//            else binding.ivUnreadSign.visibility = View.VISIBLE
        }
    }
}
