package com.restart.banzenty.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.restart.banzenty.R
import com.restart.banzenty.data.models.PlanModel
import com.restart.banzenty.data.models.StationService
import com.restart.banzenty.databinding.SubscriptionItemBinding
import java.util.Locale

class SubscriptionsAdapter(
    val requestManager: RequestManager,
    val onItemClickedInterface: OnItemClickedInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var activeSubscriptionId: Int = -1
    private var pendingSubscriptionId: Int = -1
    private val TAG = "SubscriptionsAdapter"

    private val DIFF_CALLBACK =
        object : DiffUtil.ItemCallback<PlanModel.Plan>() {
            override fun areItemsTheSame(
                oldItem: PlanModel.Plan,
                newItem: PlanModel.Plan
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: PlanModel.Plan,
                newItem: PlanModel.Plan
            ): Boolean {
                return oldItem == newItem
            }
        }

    private val differ = AsyncListDiffer(
        CustomRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )

    inner class CustomRecyclerChangeCallback(private val adapter: SubscriptionsAdapter) :
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
            SubscriptionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ), requestManager,
            activeSubscriptionId,
            pendingSubscriptionId,
            onItemClickedInterface
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

    fun submitList(list: List<PlanModel.Plan>) {
        differ.submitList(list)
    }

    fun setActiveSubscriptionId(activeSubscriptionId: Int) {
        this.activeSubscriptionId = activeSubscriptionId
    }

    fun setPendingSubscriptionId(pendingSubscriptionId: Int) {
        this.pendingSubscriptionId = pendingSubscriptionId
    }

    class CustomItemViewHolder(
        private val binding: SubscriptionItemBinding,
        private val requestManager: RequestManager,
        private val activeSubscriptionId: Int,
        private val pendingSubscriptionId: Int,
        private val onItemClickedInterface: OnItemClickedInterface
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: PlanModel.Plan) = with(binding.root) {
            if (activeSubscriptionId != -1) {
                if (plan.id == activeSubscriptionId) {
                    binding.buttonSubscribe.setText(R.string.subscribed)
                    binding.buttonSubscribe.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.right_ic,
                        0,
                        0,
                        0
                    )
                    binding.buttonSubscribe.background =
                        ContextCompat.getDrawable(
                            itemView.context,
                            R.drawable.custom_subscribed_button
                        )
                } else {
                    binding.buttonSubscribe.setText(R.string.subscribe)
                    binding.buttonSubscribe.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        0,
                        0
                    )
                    binding.buttonSubscribe.background =
                        ContextCompat.getDrawable(
                            itemView.context,
                            R.drawable.custom_subscribed_inactive_button
                        )
                }
            } else if (pendingSubscriptionId != -1) {
                if (plan.id == pendingSubscriptionId) {
                    binding.buttonSubscribe.setText(R.string.reviewing)
                    binding.buttonSubscribe.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        0,
                        0
                    )
                    binding.buttonSubscribe.background =
                        ContextCompat.getDrawable(
                            itemView.context,
                            R.drawable.custom_subscribe_button
                        )
                } else {
                    binding.buttonSubscribe.setText(R.string.subscribe)
                    binding.buttonSubscribe.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        0,
                        0
                    )
                    binding.buttonSubscribe.background =
                        ContextCompat.getDrawable(
                            itemView.context,
                            R.drawable.custom_subscribed_inactive_button
                        )
                }
            } else {
                binding.buttonSubscribe.background =
                    ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.custom_subscribe_button
                    )
            }
            binding.tvSubscriptionName.text = plan.name
            binding.tvSubscriptionPrice.text = plan.price


  /*
           val subscriptionFeaturesList = plan.subscriptionFeatures ?: ArrayList()
            subscriptionFeaturesList.add(
                0,
                StationService(
                    0,
                    "${
                        itemView.context.getString(
                            R.string.f_litres,
                            plan.liters
                        )
                    } (${itemView.context.getString(R.string.depends_on_fuel_price)})"
                ),
            )
            subscriptionFeaturesList.add(
                1,
                StationService(0, itemView.context.getString(R.string.fuel_s, plan.fuel.name)),
            )
       val subscriptionFeatureStringBuilder = StringBuilder()
            subscriptionFeaturesList.forEachIndexed { index, subscriptionFeature ->
                val subscriptionFeatureText = buildFeatureText(subscriptionFeature)
                if (subscriptionFeatureText.isNotEmpty()) {
                    if (index != 0) subscriptionFeatureStringBuilder.append("\n")
                    subscriptionFeatureStringBuilder.append(subscriptionFeatureText)
                }

            }
            binding.tvSubscriptionFeature.text = subscriptionFeatureStringBuilder.toString().trim()*/


             binding.rvSubscriptionFeatures.apply {
                 val subscriptionFeaturesAdapter = SubscriptionFeatureAdapter()
                 val subscriptionFeaturesList = plan.subscriptionFeatures ?: ArrayList()
                 subscriptionFeaturesList.add(
                     0,
                     StationService(
                         0,
                         "${
                             itemView.context.getString(
                                 R.string.f_litres,
                                 plan.liters
                             )
                         } (${itemView.context.getString(R.string.depends_on_fuel_price)})"
                     ),
                 )
                 subscriptionFeaturesList.add(
                     1,
                     StationService(0, itemView.context.getString(R.string.fuel_s, plan.fuel.name)),
                 )
                 subscriptionFeaturesAdapter.submitList(
                     subscriptionFeaturesList
                 )
                 setHasFixedSize(true)
                 adapter = subscriptionFeaturesAdapter
             }

            itemView.setOnClickListener {
                onItemClickedInterface.onSubscribeClicked(
                    binding.buttonSubscribe.text.toString()
                        .trim() == itemView.context.getString(R.string.subscribed),
                    plan
                )
            }
            binding.buttonSubscribe.setOnClickListener {
                onItemClickedInterface.onSubscribeClicked(
                    binding.buttonSubscribe.text.toString()
                        .trim() == itemView.context.getString(R.string.subscribed),
                    plan
                )
            }
            binding.tvSubscriptionName.setOnClickListener {
                onItemClickedInterface.onSubscribeClicked(
                    binding.buttonSubscribe.text.toString()
                        .trim() == itemView.context.getString(R.string.subscribed),
                    plan
                )
            }
            binding.tvSubscriptionPrice.setOnClickListener {
                onItemClickedInterface.onSubscribeClicked(
                    binding.buttonSubscribe.text.toString()
                        .trim() == itemView.context.getString(R.string.subscribed),
                    plan
                )
            }

            binding.rvSubscriptionFeatures.rootView.setOnClickListener {
                onItemClickedInterface.onSubscribeClicked(
                    binding.buttonSubscribe.text.toString()
                        .trim() == itemView.context.getString(R.string.subscribed),
                    plan
                )
            }
        }

        private fun buildFeatureText(stationService: StationService): String {
            var serviceText = ""
            if (stationService.discount == 0) {
                serviceText = stationService.name ?: ""
            } else {
                serviceText = String.format(
                    Locale.getDefault(),
                    "%s %s %s",
                    stationService.name,
                    itemView.context.getString(
                        R.string.discount, stationService.discount
                    ),
                    "%"
                )
            }
            if (stationService.limit != null)
                serviceText =
                    String.format(
                        Locale.getDefault(),
                        "%d %s",
                        stationService.limit?.minus(stationService.used ?: 0),
                        serviceText.trim()
                    )

            return serviceText
        }

    }

    interface OnItemClickedInterface {
        fun onSubscribeClicked(isSubscribed: Boolean, selectedSubscription: PlanModel.Plan)
    }

}