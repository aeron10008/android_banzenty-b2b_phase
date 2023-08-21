package com.restart.banzenty.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import com.restart.banzenty.R
import com.restart.banzenty.data.models.StationModel
import com.restart.banzenty.databinding.FilterItemBinding

class CompanyFilterAdapter(val onItemClickedInterface: OnItemClickedInterface) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "CompanyFilterAdapter"

    private val DIFF_CALLBACK =
        object : DiffUtil.ItemCallback<StationModel.Station.StationCompany>() {
            override fun areItemsTheSame(
                oldItem: StationModel.Station.StationCompany,
                newItem: StationModel.Station.StationCompany
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: StationModel.Station.StationCompany,
                newItem: StationModel.Station.StationCompany
            ): Boolean {
                return oldItem == newItem
            }

        }

    private val differ = AsyncListDiffer(
        CustomRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )


    inner class CustomRecyclerChangeCallback(private val adapter: CompanyFilterAdapter) :
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
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClickedInterface
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

    fun submitList(list: List<StationModel.Station.StationCompany>) {
        differ.submitList(list)
        Log.i(TAG, "submitList: companies list submitted")
    }


    class CustomItemViewHolder(
        private val binding: FilterItemBinding,
        val onItemClickedInterface: OnItemClickedInterface
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(company: StationModel.Station.StationCompany) = with(binding.root) {
            //populate the company data
            binding.textViewFilterName.text = company.name
            if (company.isChecked) {
                //set the background of the item red
                binding.textViewFilterName.background =
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.custom_selected_filter_frame
                    )
            }else{
                //Remove the background of the item red
                binding.textViewFilterName.background =
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.custom_filter_frame
                    )
            }
            binding.root.setOnClickListener { onItemClickedInterface.onCompanyItemClicked(adapterPosition) }
        }
    }

    interface OnItemClickedInterface {
        fun onCompanyItemClicked(pos: Int)
    }

}