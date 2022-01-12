package com.video.testconnectsdk

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.ArraySet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.device.ConnectableDeviceListener
import com.connectsdk.discovery.CapabilityFilter
import com.connectsdk.discovery.DiscoveryManager
import com.connectsdk.discovery.DiscoveryManagerListener
import com.connectsdk.service.DeviceService
import com.connectsdk.service.WebOSTVService
import com.connectsdk.service.capability.MouseControl
import com.connectsdk.service.command.ServiceCommandError
import com.video.testconnectsdk.databinding.ActivityMainBinding
import android.widget.RelativeLayout
import java.lang.Exception


class MainActivity : AppCompatActivity(), ConnectableDeviceListener {
    var castDeviceSet = ArraySet<ConnectableDevice>()
    private var connectableDevice: ConnectableDevice? = null
    private var mAdapter: RvDeviceAdapter? = null
    private lateinit var mBinding: ActivityMainBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mAdapter = RvDeviceAdapter(object : RvDeviceAdapter.ClickListener {
            override fun onClick(data: ConnectableDevice) {
                data.addListener(this@MainActivity)
                data.connect()
                connectableDevice = data
            }
        })
        mBinding.rvDevices.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.rvDevices.adapter = mAdapter
        val discoveryManager = DiscoveryManager.getInstance()
        discoveryManager.registerDefaultDeviceTypes()
        val filter = CapabilityFilter(
            MouseControl.Move
        )
        discoveryManager.setCapabilityFilters(filter)
        discoveryManager.setPairingLevel(DiscoveryManager.PairingLevel.ON)

        discoveryManager.start()


        discoveryManager.addListener(object : DiscoveryManagerListener {
            override fun onDeviceAdded(manager: DiscoveryManager?, device: ConnectableDevice?) {
                castDeviceSet.add(device)
                mAdapter?.setData(castDeviceSet)
            }

            override fun onDeviceUpdated(manager: DiscoveryManager?, device: ConnectableDevice?) {
            }

            override fun onDeviceRemoved(manager: DiscoveryManager?, device: ConnectableDevice?) {
            }

            override fun onDiscoveryFailed(manager: DiscoveryManager?, error: ServiceCommandError?) {
            }
        })


        var xDefault = 0.0
        var yDefault = 0.0
        var xReal = 0.0
        var yReal = 0.0
        var swipe = ""

        val MIN_DISTANCE = 20
        var downX = 0F
        var downY = 0f
        var upX: Float
        var upY: Float

        mBinding.touchPad.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.getX()
                        downY = event.getY()
                        xDefault = event.x.toDouble()
                        yDefault = event.y.toDouble()
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        xDefault = 0.0
                        yDefault = 0.0
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {

                        xReal = event.x.toDouble() - xDefault
                        yReal = event.y.toDouble() - yDefault


                        upX = event.x;
                        upY = event.y;

                        val deltaX = upX - downX;
                        val deltaY = upY - downY;

                        //HORIZONTAL SCROLL
                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            if (Math.abs(deltaX) > MIN_DISTANCE) {
                                // left or right
                                if (deltaX < 0) {
                                    //left
                                    swipe = "left"
                                    if (xReal > 0) {
                                        xReal *= (-1)
                                    }
                                    connectableDevice?.getCapability(MouseControl::class.java)?.move(xReal, yReal)

                                    Log.d("DDD", "left x: $xReal, y: $yReal")
                                    return true;
                                }
                                if (deltaX > 0) {
                                    //right
                                    swipe = "right"
                                    if (xReal < 0) {
                                        xReal *= (-1)
                                    }
                                    connectableDevice?.getCapability(MouseControl::class.java)?.move(xReal, yReal)
                                    Log.d("DDD", "right")
                                    Log.d("DDD", "left x: $xReal, y: $yReal")
                                    return true;
                                }
                            } else {
                                //not long enough swipe...
                                return false;
                            }
                        }
                        //VERTICAL SCROLL
                        else {
                            if (Math.abs(deltaY) > MIN_DISTANCE) {
                                // top or down
                                if (deltaY < 0) {
                                    //top
                                    swipe = "top"
                                    if (yReal < 0) {
                                        yReal *= (-1)
                                    }
                                    connectableDevice?.getCapability(MouseControl::class.java)?.move(xReal, yReal)
                                    Log.d("DDD", "top")
                                    Log.d("DDD", "left x: $xReal, y: $yReal")
                                    return true;
                                }
                                if (deltaY > 0) {
                                    //bottom
                                    swipe = "bottom"
                                    if (yReal > 0) {
                                        yReal *= (-1)
                                    }
                                    connectableDevice?.getCapability(MouseControl::class.java)?.move(xReal, yReal)
                                    Log.d("DDD", "bottom")
                                    Log.d("DDD", "left x: $xReal, y: $yReal")
                                    return true;
                                }
                            } else {
                                //not long enough swipe...
                                return false;
                            }
                        }


                    }

                }

                return true
            }
        })

    }


    override fun onDeviceReady(device: ConnectableDevice?) {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
        device?.getCapability(MouseControl::class.java)?.connectMouse()
        connectableDevice = device
        connectableDevice?.getCapability(MouseControl::class.java)?.move(0.0, 0.0)

    }

    override fun onDeviceDisconnected(device: ConnectableDevice?) {
    }

    override fun onPairingRequired(device: ConnectableDevice?, service: DeviceService?, pairingType: DeviceService.PairingType?) {
        Log.d("AAA", "${pairingType?.name}")
    }

    override fun onCapabilityUpdated(device: ConnectableDevice?, added: MutableList<String>?, removed: MutableList<String>?) {
    }

    override fun onConnectionFailed(device: ConnectableDevice?, error: ServiceCommandError?) {
    }
}