package com.restart.banzenty.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import com.restart.banzenty.R
import com.restart.banzenty.data.models.PlanModel.Plan.Fuel
import com.restart.banzenty.databinding.FilterItemBinding

class FuelFilterAdapter(val onItemClickedInterface: OnItemClickedInterface) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "FuelFilterAdapter"

    private val DIFF_CALLBACK =
        object : DiffUtil.ItemCallback<Fuel>() {
            override fun areItemsTheSame(
                oldItem: Fuel,
                newItem: Fuel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Fuel,
                newItem: Fuel
            ): Boolean {
                return oldItem == newItem
            }

        }

    private val differ = AsyncListDiffer(
        CustomRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )


    inner class CustomRecyclerChangeCallback(private val adapter: FuelFilterAdapter) :
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
            FilterItemBinding.inflate(
                LayoutInflater.from(parent.context), parent,
                false
            ), onItemClickedInterface
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

    fun submitList(list: List<Fuel>) {
        differ.submitList(list)
    }

    class CustomItemViewHolder(
        private val binding: FilterItemBinding,
        private val onItemClickedInterface: OnItemClickedInterface
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Fuel) = with(binding.root) {
            //populate the service data
            binding.textViewFilterName.text = service.name
            if (service.isChecked) {
                //set the background of the item red
                binding.textViewFilterName.background =
                    ContextCompat.getDrawable(context, R.drawable.custom_selected_filter_frame)
            } else {
                //Remove the background of the item red
                binding.textViewFilterName.background =
                    ContextCompat.getDrawable(context, R.drawable.custom_filter_frame)
            }
            binding.root.setOnClickListener {
                onItemClickedInterface.onServiceItemClicked(
                    adapterPosition
                )
            }
        }
    }

    interface OnItemClickedInterface {
        fun onServiceItemClicked(pos: Int)
    }
}
