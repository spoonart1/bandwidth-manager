

import android.os.Handler
import android.os.HandlerThread
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * Created by Lafran on 12/8/17.
 */
class BandwidthManager(val listener: BandWithListener?) {

    private val TAG = "BandwithManager"
    private val CONNECT_TIMEOUT = 10000L

    private val testUrl = "http://siab.faceoffice.co.id/images/siab/asset-orang.png"
    private var startTime = 0L
    private var endTime = 0L
    private var fileSize = 0L

    private val POOR_BANDWIDTH = 150
    private val AVERAGE_BANDWIDTH = 550
    private val GOOD_BANDWIDTH = 2000

    private var client: OkHttpClient? = null
    private var request: Request? = null
    private var downloadSpeed = 0.0

    private var thread = HandlerThread("bandwith-manager")

    init {
        setup()
    }

    private fun setup() {
        client = OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .build()

        request = Request.Builder()
                .url(testUrl)
                .build()

        fetchRequest()
    }

    private fun fetchRequest() {
        startTime = System.currentTimeMillis()
        client!!.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                listener?.onNetworkFailed()
                e?.printStackTrace()
            }

            override fun onResponse(call: Call?, response: Response?) {

                if (response == null) {
                    return
                }

                if (!response!!.isSuccessful) {
                    throw IOException("Unexpected code " + response);
                }
                thread.start()
                thread.run {
                    println("$TAG Thread Start")
                    val responseHeaders = response!!.headers()
                    var i = 0
                    val size = responseHeaders.size()
                    while (i < size) {
                        println("${responseHeaders.name(i)} : ${responseHeaders.value(i)}")
                        i++
                    }

                    val input = response.body()!!.byteStream()

                    try {
                        val bos = ByteArrayOutputStream()
                        val buffer = ByteArray(1024)

                        while (input.read(buffer) !== -1) {
                            bos.write(buffer)
                        }
                        val docBuffer = bos.toByteArray()
                        fileSize = bos.size().toLong()

                    } finally {
                        input.close()
                    }

                    endTime = System.currentTimeMillis()
                    val timeInSeconds = getDifferenceInSeconds()
                    val kilobytePerSeconds = Math.round(1024 / timeInSeconds).toInt()

                    //UI Thread
                    Handler(this.looper).post {
                        if (kilobytePerSeconds <= POOR_BANDWIDTH) {
                            listener?.onNetworkPoorDetected()
                        } else if (kilobytePerSeconds > POOR_BANDWIDTH && kilobytePerSeconds <= AVERAGE_BANDWIDTH) {
                            listener?.onNetworkStable()
                        } else if (kilobytePerSeconds >= GOOD_BANDWIDTH) {
                            listener?.onNetworkStable()
                        }
                    }

                    downloadSpeed = fileSize / getTimeTakenMillis()

                    println("$TAG time taken in secs: $timeInSeconds")
                    println("$TAG kilobyte per secs: $kilobytePerSeconds")
                    println("$TAG download speed: $downloadSpeed")
                    println("$TAG file size: ${fileSize / 1024}kb")
                }
            }
        })
    }

    private fun getDifferenceInSeconds(): Double {
        val timeTakenMillis = getTimeTakenMillis()
        val timeTakenSeconds = timeTakenMillis / 1000
        return timeTakenSeconds
    }

    private fun getTimeTakenMillis(): Double {
        val timeTakenMillis = Math.floor(endTime.toDouble() - startTime.toDouble())
        return timeTakenMillis
    }

    fun getDownloadSpeed(): Double {
        return downloadSpeed
    }

    interface BandWithListener {
        fun onNetworkPoorDetected()
        fun onNetworkStable() {}
        fun onNetworkFailed() {}
    }
}