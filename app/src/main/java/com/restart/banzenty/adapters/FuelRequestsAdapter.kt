package com.restart.banzenty.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.restart.banzenty.R
import com.restart.banzenty.data.models.FuelRequestModel
import com.restart.banzenty.databinding.RequestItemBinding
import com.restart.banzenty.utils.MainUtils

class FuelRequestsAdapter(
    val requestManager: RequestManager
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "FuelRequestsAdapter"


    private val DIFF_CALLBACK =
        object : DiffUtil.ItemCallback<FuelRequestModel.FuelRequest>() {

            override fun areItemsTheSame(
                oldItem: FuelRequestModel.FuelRequest,
                newItem: FuelRequestModel.FuelRequest
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: FuelRequestModel.FuelRequest,
                newItem: FuelRequestModel.FuelRequest
            ): Boolean {
                return oldItem == newItem
            }

        }
    private val differ = AsyncListDiffer(
        CustomRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )


    inner class CustomRecyclerChangeCallback(private val adapter: FuelRequestsAdapter) :
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
            RequestItemBinding.inflate(
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

    fun submitList(list: List<FuelRequestModel.FuelRequest>) {
        differ.submitList(list)
    }

    fun preloadGlideImages(
        requestManager: RequestManager,
        list: List<FuelRequestModel.FuelRequest>
    ) {
        for (item in list) {
            requestManager
                .load(item.serviceType?.image)
                .preload()
        }
    }

    class CustomItemViewHolder(
        private val binding: RequestItemBinding,
        private val requestManager: RequestManager
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(request: FuelRequestModel.FuelRequest) = with(binding.root) {
            requestManager.load(request.serviceType?.image?.url).into(binding.ivService)
//            requestManager.load(R.drawable.ic_fuel).into(binding.textViewRequestLetter)
            binding.textViewRequestPlanName.text = request.serviceType?.name
            binding.textViewRequestId.text = request.externalNumber
            binding.textViewStationName.text = request.station?.name
            binding.textViewDateAndTime.text = MainUtils.getDateTime(request.createdAt)
            if ((request.amount ?: "0.0").toDouble() != 0.0) {
                binding.rlAmount.visibility = View.VISIBLE
                binding.textViewAmount.text =
                    context.getString(R.string.f_litres, (request.amount ?: "0.0").toDouble())
            } else {
                binding.rlAmount.visibility = View.GONE
            }
//            if (request.fromSubscription != null) {
//                binding.tvSubscriptionCredit.text =
//                    context.getString(R.string.egp, request.fromSubscription)
//                binding.rlSubscriptionCredit.visibility = View.VISIBLE
//            } else binding.rlSubscriptionCredit.visibility = View.GONE

//            if (request.subscriptionName != null) {
//                binding.tvPaymentType.setText(R.string.subscription)
//            } else {
//                if ((request.price ?: "0.0").toDouble() != 0.0)
//                    binding.tvPaymentType.setText(R.string.cash)
//                else
//                    binding.tvPaymentType.setText(R.string.reward)
//            }
//            binding.textViewTotal.text = context.getString(R.string.egp, request.price ?: "")

            binding.rlSubscriptionCredit.visibility = View.GONE

//            if (request.subscriptionName != null) {
//                binding.tvPaymentType.setText(R.string.subscription)
//                if ((request.price ?: "0.0").toDouble() == 0.0) {
//                    binding.textViewTotal.setText(R.string.reward)
//                } else {
//                    binding.textViewTotal.text =
//                        context.getString(R.string.egp, request.price ?: "")
//                }
//            }

            binding.textViewTotal.text =
                context.getString(R.string.egp, request.price ?: "0.0")

            if ((request.price ?: "0.0").toDouble() != 0.0) {
                binding.tvPaymentType.setText(R.string.subscription)
            } else {
                binding.tvPaymentType.setText(R.string.reward)
            }
        }
    }
}
