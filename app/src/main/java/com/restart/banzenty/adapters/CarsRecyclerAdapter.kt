package com.restart.banzenty.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.restart.banzenty.data.models.UserModel
import com.restart.banzenty.databinding.DeleteCarItemBinding

class CarsRecyclerAdapter(private val onCarDeletedInterface: OnCarDeletedInterface) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = "CarRecyclerAdapter"

    private val DIFF_CALLBACK =
        object : DiffUtil.ItemCallback<UserModel.UserCarPlate>() {
            override fun areItemsTheSame(
                oldItem: UserModel.UserCarPlate,
                newItem: UserModel.UserCarPlate
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: UserModel.UserCarPlate,
                newItem: UserModel.UserCarPlate
            ): Boolean {
                return oldItem == newItem
            }
        }

    private val differ = AsyncListDiffer(
        CustomRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )

    inner class CustomRecyclerChangeCallback(private val adapter: CarsRecyclerAdapter) :
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
            DeleteCarItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ), onCarDeletedInterface
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CustomItemViewHolder -> {
                holder.bind(differ.currentList[position])
                Log.i(TAG, "onBindViewHolder: current position = $position")
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return differ.currentList[position].id
    }

    fun submitList(list: List<UserModel.UserCarPlate>) {
        differ.submitList(list)
    }

    class CustomItemViewHolder(
        private val binding: DeleteCarItemBinding,
        private val onCarDeletedInterface: OnCarDeletedInterface
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(car: UserModel.UserCarPlate) = with(binding.root) {
            binding.tvDeleteCarNumbers.text = car.plateNumberDigits
            if ((car.plateNumberCharacters?.length ?: 0) > 2) {
                binding.tvThirdChar.visibility = View.VISIBLE
                binding.tvThirdChar.text = car.plateNumberCharacters?.get(2).toString()
            } else binding.tvThirdChar.visibility = View.GONE
            if ((car.plateNumberCharacters?.length ?: 0) > 1) {
                binding.tvSecondChar.visibility = View.VISIBLE
                binding.tvSecondChar.text = car.plateNumberCharacters?.get(1).toString()
            } else binding.tvSecondChar.visibility = View.GONE
            binding.tvFirstChar.text = (car.plateNumberCharacters?.get(0) ?: "").toString()
            if (car.isDeleting) {
                binding.btnDeleteCar.visibility = View.GONE
                binding.progressBarDeleteCar.visibility = View.VISIBLE
            } else {
                binding.btnDeleteCar.visibility = View.VISIBLE
                binding.progressBarDeleteCar.visibility = View.GONE
            }
            if (adapterPosition == 0) {
                binding.btnDeleteCar.visibility = View.GONE
                binding.tvDeleteLabel.visibility = View.GONE
            }else{
                binding.btnDeleteCar.visibility = View.VISIBLE
                binding.tvDeleteLabel.visibility = View.VISIBLE
            }

            binding.btnDeleteCar.setOnClickListener {
                onCarDeletedInterface.onCarDeleted(adapterPosition)
            }
        }
    }

    interface OnCarDeletedInterface {
        fun onCarDeleted(pos: Int)
    }
}