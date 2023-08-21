package com.restart.banzenty.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.restart.banzenty.databinding.AvailableServicesItemBinding
import com.restart.banzenty.data.models.OldService
import com.restart.banzenty.ui.station.StationDetailsActivity

class AvailableServicesRecyclerAdapter(private val oldServices: ArrayList<OldService>,
                                       private val activity: StationDetailsActivity
)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var binding: AvailableServicesItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = AvailableServicesItemBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)

        return ItemHolder(binding.root)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as ItemHolder
        itemHolder.setDetails(position)
    }

    override fun getItemCount(): Int {
        return oldServices.size
    }

    inner class ItemHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemView: View = itemView
        lateinit var oldService : OldService

        internal fun setDetails(position: Int) {
            this.oldService = oldServices[position]

            binding.imageViewServiceIcon.setImageDrawable(oldService.serviceLogo)
            binding.textViewServiceName.text = oldService.serviceName
        }
    }
}