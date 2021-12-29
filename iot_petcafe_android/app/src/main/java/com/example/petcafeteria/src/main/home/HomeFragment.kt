package com.example.petcafeteria.src.main.home

import android.os.Bundle
import android.util.Log
import android.view.View
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.amazonaws.regions.Regions
import com.example.petcafeteria.R
import com.example.petcafeteria.config.BaseFragment
import com.example.petcafeteria.databinding.FragmentHomeBinding
import java.io.UnsupportedEncodingException
import java.util.*

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home){

    var mqttManager: AWSIotMqttManager? = null
    var clientId: String? = null
    var credentialsProvider: CognitoCachingCredentialsProvider? = null
    companion object {

        // --- Constants to modify per your configuration ---
        // Customer specific IoT endpoint
        // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com,
        private const val CUSTOMER_SPECIFIC_ENDPOINT = "CUSTOMER_SPECIFIC_ENDPOINT"

        // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
        // AWS IoT permissions.
        private const val COGNITO_POOL_ID = "COGNITO_POOL_ID"

        // Region of AWS IoT
        private val MY_REGION = Regions.AP_NORTHEAST_2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clientId = UUID.randomUUID().toString()

        credentialsProvider = CognitoCachingCredentialsProvider(
                context,  // context
                COGNITO_POOL_ID,  // Identity Pool ID
                MY_REGION // Region
        )

        // MQTT Client
        mqttManager = AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT)

        Thread { runOnUiThread { binding.homeWaterToPetBtn!!.isEnabled = true } }.start()

        mqttconnect()
        mqttsubscribe1()
        mqttsubscribe2()

        binding.homeWaterToPetBtn.setOnClickListener {
            mqttpublish_water()
            showCustomToast("반려동물에게 물을 주었습니다.")
        }
        binding.homeFeedBtn.setOnClickListener {
            mqttpublish_feed()
            showCustomToast("반려동물에게 사료를 주었습니다.")
        }
    }


    fun mqttconnect(){
        Log.d("LOG_TAG", "clientId = $clientId")
        try {
            mqttManager!!.connect(
                    credentialsProvider
            ) { status, throwable ->
                Log.d("LOG_TAG", "Status = $status")
                runOnUiThread {
                    if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connecting) {

                    } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {

                    } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Reconnecting) {
                        if (throwable != null) {
                            Log.e(
                                    "LOG_TAG",
                                    "Connection error.",
                                    throwable
                            )
                        }

                    } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost) {
                        if (throwable != null) {
                            Log.e(
                                    "LOG_TAG",
                                    "Connection error.",
                                    throwable
                            )
                            throwable.printStackTrace()
                        }

                    } else {

                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LOG_TAG", "Connection error.", e)

        }
    }

    fun mqttsubscribe1() {
        val topic =  "$" + "aws/things/Provider/shadow/update/accepted"
        Log.d("LOG_TAG", "topic = $topic")

        try {
            mqttManager!!.subscribeToTopic(
                    topic, AWSIotMqttQos.QOS0
            ) { topic, data ->
                runOnUiThread {
                    try {
                        Log.e("LOG_TAG", "Subscribe S")
                    } catch (e: UnsupportedEncodingException) {
                        Log.e(
                                "LOG_TAG",
                                "Message encoding error.",
                                e
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LOG_TAG", "Subscription error.", e)
        }
    }

    fun mqttsubscribe2() {
        val topic3 =  "$" + "aws/things/Provider/shadow/get/accepted"
        Log.d("LOG_TAG", "topic = $topic3")

        try {
            mqttManager!!.subscribeToTopic(
                    topic3, AWSIotMqttQos.QOS0
            ) { topic, data ->
                runOnUiThread {
                    try {
                        Log.e("LOG_TAG", "Subscribe S")
                    } catch (e: UnsupportedEncodingException) {
                        Log.e(
                                "LOG_TAG",
                                "Message encoding error.",
                                e
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LOG_TAG", "Subscription error.", e)
        }
    }

    fun mqttpublish_water() {
        val topic1 = "$" + "aws/things/Provider/shadow/update"
        var msg: String = "{\"state\":{\"desired\":{\"pump\":1}}}"
        Log.d("message", msg)
        try {
            mqttManager!!.publishString(msg, topic1, AWSIotMqttQos.QOS0)
            Log.e("LOG_TAG", "Publish Water S")
        } catch (e: Exception) {
            Log.e("LOG_TAG", "Publish error.", e)
        }

    }

    fun mqttpublish_feed() {
        val topic1 = "$" + "aws/things/Provider/shadow/update"
        var msg: String = "{\"state\":{\"desired\":{\"motor\":1}}}"
        Log.d("message", msg)
        try {
            mqttManager!!.publishString(msg, topic1, AWSIotMqttQos.QOS0)
            Log.e("LOG_TAG", "Publish Feed S")
        } catch (e: Exception) {
            Log.e("LOG_TAG", "Publish error.", e)
        }

    }

}