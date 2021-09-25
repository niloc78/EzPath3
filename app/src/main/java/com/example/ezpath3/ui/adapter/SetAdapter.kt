package com.example.ezpath3.ui.adapter

import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ezpath3.databinding.SetItemLayoutBinding
import com.example.ezpath3.util.makeDiffUtilListener


class SetAdapter(var setItemClickListener: SetItemClickListener)
    : RecyclerView.Adapter<SetAdapter.ViewHolder>() {
    var data : ArrayList<String> = ArrayList()

    interface SetItemClickListener {
        fun deleteSet(setName : String)
        fun showLoadSetDialog(setName : String)
    }

    inner class ViewHolder(val viewBinding: SetItemLayoutBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(pos : Int) {
            //info: setName
            viewBinding.setName.text = data[pos]
            //info: attach listeners
            attachListeners()
        }
        // attach listeners
        private fun attachListeners() { // todo attach click listeners
            //info: setName textview listeners
            viewBinding.setName.apply { //todo set onclick and longclick listener
                setOnClickListener {  // todo open confirmloadsetdialog via a listener? DONE
                    setItemClickListener.showLoadSetDialog(this.text.toString())
                }
                setOnLongClickListener { // todo setContainer perform long click DONE
                    viewBinding.setItemContainer.performLongClick()
                    true
                }
            }
            // info: setContainer listeners
            viewBinding.setItemContainer.apply {  // todo set onclick and longclick listener DONE
                setOnLongClickListener {  // todo show delete buttons and highlights DONE
                    //info: show delete button
                    viewBinding.deleteButton.visibility = View.VISIBLE
                    //info: highlight everything
                    this.isSelected = true
                    viewBinding.setCard.isSelected = true
                    viewBinding.setName.isSelected = true
                    //info: disable setName function
                    viewBinding.setName.isEnabled = false
                    true
                }
                setOnClickListener { // todo hide delete buttons and highlights DONE
                    //info: hide delete button
                    viewBinding.deleteButton.visibility = View.INVISIBLE
                    //info: de-highlight everything
                    this.isSelected = false
                    viewBinding.setCard.isSelected = false
                    viewBinding.setName.isSelected = false
                    //info: enable setName function
                    viewBinding.setName.isEnabled = true
                }
            }
            //info: deleteButton listener
            viewBinding.deleteButton.setOnClickListener { // todo setContainer perform click, delete set via a listener? DONE
                viewBinding.setItemContainer.performClick()
                setItemClickListener.deleteSet(viewBinding.setName.text.toString())
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SetItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        //val view = LayoutInflater.from(parent.context).inflate(R.layout.set_item_layout, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setNewData(newData : ArrayList<String>) { //info: update setData and notify recyclerView
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
                    //println("areItemsTheSame: old: ${data[oldItemPosition]} new: ${newData[newItemPosition]}")
                    //println("areItemsTheSame result: ${data[oldItemPosition] == newData[newItemPosition]}")
                    data[oldItemPosition] == newData[newItemPosition]
                },
                areContentsTheSame = {oldItemPosition, newItemPosition ->
                    val old = data[oldItemPosition]
                    val new = newData[newItemPosition]
                    // println("areContentsTheSame: old: $old new: $new")
                    //println("areContentsTheSame result: ${old == new}")
                    old == new
                }
        )
        data.clear()
        data.addAll(newData)
        //println("setData was set to new: ${data.joinToString()}")
        result.dispatchUpdatesTo(this)
    }
}