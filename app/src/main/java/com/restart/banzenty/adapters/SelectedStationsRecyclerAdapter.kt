package com.restart.banzenty.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.restart.banzenty.data.models.StationModel
import com.restart.banzenty.databinding.SelectedStationItemBinding

class SelectedStationsRecyclerAdapter(private val onRemoveItemInterface: OnRemoveItemInterface)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = "SelectedStationsRecyclerAdapter"

    private val DIFF_CALLBACK =
        object : DiffUtil.ItemCallback<StationModel.Station>() {
            override fun areItemsTheSame(
                oldItem: StationModel.Station,
                newItem: StationModel.Station
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: StationModel.Station,
                newItem: StationModel.Station
            ): Boolean {
                return oldItem == newItem
            }
        }

    private val differ = AsyncListDiffer(CustomRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build())

    inner class CustomRecyclerChangeCallback(private val adapter: SelectedStationsRecyclerAdapter)
        : ListUpdateCallback {

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
            SelectedStationItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ), onRemoveItemInterface
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

    fun submitList(list: List<StationModel.Station>) {
        differ.submitList(list)
    }

    class CustomItemViewHolder (
        private val binding: SelectedStationItemBinding,
        private val onRemoveItemInterface: OnRemoveItemInterface
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(station: StationModel.Station) = with(binding.root) {
            binding.tvSelectedStationName.text = station.name
            binding.btnDelete.setOnClickListener {
                onRemoveItemInterface.onItemRemoved(adapterPosition)
            }
        }
    }

    interface OnRemoveItemInterface {
        fun onItemRemoved(pos: Int)
    }
}