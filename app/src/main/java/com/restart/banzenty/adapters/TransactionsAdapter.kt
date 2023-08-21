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
import com.restart.banzenty.data.models.PaymentModel
import com.restart.banzenty.data.models.PlanModel
import com.restart.banzenty.data.models.StationService
import com.restart.banzenty.databinding.SubscriptionItemBinding
import com.restart.banzenty.databinding.WalletTransactionItemBinding
import java.util.Locale

class TransactionsAdapter(
    val requestManager: RequestManager,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var activeSubscriptionId: Int = -1
    private var pendingSubscriptionId: Int = -1
    private val TAG = "TransactionsAdapter"

    private val DIFF_CALLBACK =
        object : DiffUtil.ItemCallback<PaymentModel.Payment>() {

            override fun areContentsTheSame(
                oldItem: PaymentModel.Payment,
                newItem: PaymentModel.Payment
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(
                oldItem: PaymentModel.Payment,
                newItem: PaymentModel.Payment
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }

    private val differ = AsyncListDiffer(
        CustomRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )

    inner class CustomRecyclerChangeCallback(private val adapter: TransactionsAdapter) :
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
            WalletTransactionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ), requestManager,
            activeSubscriptionId,
            pendingSubscriptionId
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

    fun submitList(list: List<PaymentModel.Payment>) {
        differ.submitList(list)
    }

    class CustomItemViewHolder(
        private val binding: WalletTransactionItemBinding,
        private val requestManager: RequestManager,
        private val activeSubscriptionId: Int,
        private val pendingSubscriptionId: Int
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: PaymentModel.Payment) = with(binding.root) {
            binding.tvTransactionItemAmount.text = resources.getString(R.string.wallet_amount, payment.paid_amount)
            binding.tvTransactionItemDate.text = payment.created_at.toString()
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

}