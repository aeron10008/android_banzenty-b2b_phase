package com.restart.banzenty.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.restart.banzenty.R
import com.restart.banzenty.data.models.StationModel
import com.restart.banzenty.databinding.NearbyStationItemBinding
import com.restart.banzenty.ui.station.StationDetailsActivity
import com.restart.banzenty.utils.MainUtils

class StationAdapter(
    val requestManager: RequestManager
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "StationAdapter"

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
    private val differ = AsyncListDiffer(
        CustomRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )


    inner class CustomRecyclerChangeCallback(private val adapter: StationAdapter) :
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
            NearbyStationItemBinding.inflate(
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

    fun submitList(list: List<StationModel.Station>) {
        differ.submitList(list)
    }

    fun preloadGlideImages(
        requestManager: RequestManager,
        list: List<StationModel.Station>
    ) {
        for (item in list) {
            requestManager
                .load(item.company?.image?.url)
                .preload()
        }
    }

    class CustomItemViewHolder(
        private val binding: NearbyStationItemBinding,
        private val requestManager: RequestManager
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(station: StationModel.Station) = with(binding.root) {
            requestManager.load(station.company?.image?.url).into(binding.imageViewStationPic)
            binding.tvName.text = station.name
            binding.tvOpening.text = station.workingHours
            binding.ivPartner.visibility = if (station.hasContract == 1) View.VISIBLE else View.GONE
            if (station.address != null) {
                binding.tvAddress.visibility = View.VISIBLE
                binding.tvAddress.text = station.address
            } else {
                binding.tvAddress.visibility = View.INVISIBLE
            }
            if ((station.distance ?: "0.0").toFloat() < 1) {
                val meters = station.distance!!.split(".")
                binding.textViewDistance.text = context.getString(
                    R.string.d_meters,
                   if (meters[1].length>3) meters[1].substring(0,3).removePrefix("0")else meters[1]
                )
//            binding.textViewDistance.text =
//                    context.getString(R.string.less_than_km)
            } else binding.textViewDistance.text =
                context.getString(R.string.distance_km, (station.distance ?: "0.0").toFloat())
            binding.btnDirection.setOnClickListener {
                try {
                    MainUtils.openGoogleMap(
                        context,
                        station.lat,
                        station.lng
                    )
                } catch (e: Exception) {
                }
            }

            binding.root.setOnClickListener {
                try {
                    val stationIntent = Intent(context, StationDetailsActivity::class.java)
                    stationIntent.putExtra("station", station)
                    context.startActivity(stationIntent)
                } catch (e: Exception) {
                }
            }
        }
    }
}
