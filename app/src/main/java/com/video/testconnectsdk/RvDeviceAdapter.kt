package com.video.testconnectsdk

import android.util.ArraySet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.connectsdk.device.ConnectableDevice
import com.video.testconnectsdk.databinding.DeviceItemBinding

class RvDeviceAdapter ( val listener: ClickListener) : RecyclerView.Adapter<RvDeviceAdapter.ViewHolder>() {
    private var mData: ArraySet<ConnectableDevice> = ArraySet()

    fun setData(data: ArraySet<ConnectableDevice>) {
        mData.clear()
        mData.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.mBinding.root.setOnClickListener {
            listener.onClick(mData.valueAt(position))
        }
        holder.mBinding.tvFriendlyName.text = mData.valueAt(position).friendlyName
        holder.mBinding.tvDeviceType.text = mData.valueAt(position).connectedServiceNames
    }

    override fun getItemCount() = mData.size

    class ViewHolder(val mBinding: DeviceItemBinding) : RecyclerView.ViewHolder(mBinding.root)

    interface ClickListener {
        fun onClick(data: ConnectableDevice)
    }
}