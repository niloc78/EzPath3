package com.example.ezpath3.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ezpath3.R
import com.example.ezpath3.databinding.ErrandFooterBinding
import com.example.ezpath3.databinding.ErrandItemLayoutBinding
import com.example.ezpath3.util.makeDiffUtilListener


class ErrandAdapter
constructor(var errandItemClickListener: ErrandItemClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var data : ArrayList<LinkedHashMap<String, String>> = ArrayList() // errandName, storeName, address


    interface ErrandItemClickListener {
        fun check(checked : Boolean, errandName : String)
        fun add(maxed : Boolean)
    }



    inner class ErrandViewHolder(val viewBinding : ErrandItemLayoutBinding) : RecyclerView.ViewHolder(viewBinding.root) {

        fun bind(errandName : String, storeName : String, address : String) {
            //info: set errand name
            viewBinding.errandName.text = errandName
            //info: set storename
            viewBinding.storeName.text = storeName
            //info: set address
            viewBinding.address.text = address
            //info: attach listeners
            attachListeners()
        }

        fun attachListeners() {
            viewBinding.errandCheckBox.setOnCheckedChangeListener { _, isChecked ->
                viewBinding.errandCard.isChecked = isChecked
                errandItemClickListener.check(checked = isChecked, errandName = viewBinding.errandName.text.toString()) //info: uncheck or check this errand. changing marker color
            }
        }

    }

    inner class FooterViewHolder(val viewBinding : ErrandFooterBinding) : RecyclerView.ViewHolder(viewBinding.root) {


        fun bind() {
            attachListeners()
        }
        fun attachListeners() {
            viewBinding.addErrandButton.setOnClickListener {
                //prevent further adding if max of 10 was reached, otherwise open dialog todo in parent decide whether to show toast or open dialog DONE
                errandItemClickListener.add(maxed = data.size >= 10)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.errand_item_layout -> {
                val binding = ErrandItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                //val view = LayoutInflater.from(parent.context).inflate(R.layout.errand_item_layout, parent, false)
                ErrandViewHolder(binding)
            }
            R.layout.errand_footer -> {
                val binding = ErrandFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                //val view = LayoutInflater.from(parent.context).inflate(R.layout.errand_footer, parent, false)
                FooterViewHolder(binding)
            }
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            R.layout.errand_item_layout -> (holder as ErrandViewHolder).bind(data[position]["errandName"]!!, data[position]["storeName"]!!,
                    data[position]["address"]!!)
            R.layout.errand_footer -> (holder as FooterViewHolder).bind()
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            data.size -> R.layout.errand_footer
            else -> R.layout.errand_item_layout
        }
    }

    override fun getItemCount(): Int {
        return data.size + 1
    }

    fun setNewData(newData : ArrayList<LinkedHashMap<String, String>>) { //info: update errandData and notify recyclerView
        val result = makeDiffUtilListener(
                getOldListSize = {
                    //println("oldListSize: ${data.size}")
                    data.size
                 },
                getNewListSize = {
                    //println("newListSize: ${newData.size}")
                    newData.size
                 },
                areItemsTheSame = {oldItemPosition, newItemPosition ->
                    // println("areItemsTheSame: old: ${data[oldItemPosition]} new: ${newData[newItemPosition]}")
                    //println("areItemsTheSame result: ${data[oldItemPosition] == newData[newItemPosition]}")
                    data[oldItemPosition] == newData[newItemPosition]
                },
                areContentsTheSame = {oldItemPosition, newItemPosition ->
                    val old = data[oldItemPosition]
                    val new = newData[newItemPosition]
                    old == new
                            && old["errandName"] == new["errandName"]
                            && old["storeName"] == new["storeName"]
                            && old["address"] == new["address"]
                }
        )

        data = newData
        result.dispatchUpdatesTo(this)
    }


}