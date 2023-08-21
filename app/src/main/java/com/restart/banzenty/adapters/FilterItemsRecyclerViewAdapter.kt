package com.restart.banzenty.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.restart.banzenty.R
import com.restart.banzenty.databinding.FilterItemBinding
import com.restart.banzenty.ui.station.StationFilterActivity

class FilterItemsRecyclerViewAdapter(private var filters: ArrayList<String>?,
                                     private var activity: StationFilterActivity):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var binding: FilterItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = FilterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ItemHolder(binding.root)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as ItemHolder
        itemHolder.setDetails(position)
    }

    override fun getItemCount(): Int {
        return filters?.size ?: 0
    }

    inner class ItemHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var view: View = itemView
        private lateinit var filterItem : String


        internal fun setDetails(position: Int) {
            this.filterItem = filters!![position]

            binding.textViewFilterName.text = filterItem

            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when(v) {
                itemView -> {
                    binding.textViewFilterName.setBackgroundResource(R.drawable.custom_selected_filter_frame)
                    Toast.makeText(activity, "change item color", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}