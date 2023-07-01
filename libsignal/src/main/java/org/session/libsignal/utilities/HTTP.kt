package org.session.libsignal.utilities

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.dnsoverhttps.DnsOverHttps
import okhttp3.internal.toCanonicalHost
import org.session.libsignal.BuildConfig
import java.net.InetAddress
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object HTTP {
    var isConnectedToNetwork: (() -> Boolean) = { false }


    var GUARDNODE = ""


    private val seedNodeConnection by lazy {
        val bootstrapClient = OkHttpClient.Builder().build()
        val dns =DnsOverHttps.Builder().client(bootstrapClient)
            .url("https://1.1.1.1/dns-query".toHttpUrl())
            .bootstrapDnsHosts(InetAddress.getByName("8.8.4.4"), InetAddress.getByName("8.8.8.8"))
            .build()
        bootstrapClient.newBuilder()
            .dns(dns)
            .callTimeout(timeout, TimeUnit.SECONDS)
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .build()
    }

    fun setGuardNode(guardNode: String) {
        GUARDNODE = guardNode
    }

    private val defaultConnection by lazy {

        // Snode to snode communication uses self-signed certificates but clients can safely ignore this
        val trustManager = object : X509TrustManager {

            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authorizationType: String?) { }
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authorizationType: String?) { }
            override fun getAcceptedIssuers(): Array<X509Certificate> { return arrayOf() }
        }
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf( trustManager ), SecureRandom())

        val bootstrapClient = OkHttpClient.Builder().build()
        val dns =DnsOverHttps.Builder().client(bootstrapClient)
            .url("https://1.1.1.1/dns-query".toHttpUrl())
            .bootstrapDnsHosts(InetAddress.getByName("8.8.4.4"), InetAddress.getByName("8.8.8.8"))
            .build()
        bootstrapClient.newBuilder()
            .dns(dns)
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .callTimeout(timeout, TimeUnit.SECONDS)
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .build()
    }

    private fun getDefaultConnection(timeout: Long): OkHttpClient {
        // Snode to snode communication uses self-signed certificates but clients can safely ignore this
        val trustManager = object : X509TrustManager {

            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authorizationType: String?) { }
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authorizationType: String?) { }
            override fun getAcceptedIssuers(): Array<X509Certificate> { return arrayOf() }
        }
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf( trustManager ), SecureRandom())

        val bootstrapClient = OkHttpClient.Builder().build()
        val dns =DnsOverHttps.Builder().client(bootstrapClient)
            .url("https://1.1.1.1/dns-query".toHttpUrl())
            .bootstrapDnsHosts(InetAddress.getByName("8.8.4.4"), InetAddress.getByName("8.8.8.8"))
            .build()
        return bootstrapClient.newBuilder()
            .dns(dns)
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .callTimeout(timeout, TimeUnit.SECONDS)
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .build()
    }

    private const val timeout: Long = 120

    open class HTTPRequestFailedException(
        val statusCode: Int,
        val json: Map<*, *>?,
        message: String = "HTTP request failed with status code $statusCode"
    ) : kotlin.Exception(message)
    class HTTPNoNetworkException : HTTPRequestFailedException(0, null, "No network connection")

    enum class Verb(val rawValue: String) {
        GET("GET"), PUT("PUT"), POST("POST"), DELETE("DELETE")
    }

    /**
     * Sync. Don't call from the main thread.
     */
    fun execute(verb: Verb, url: String, timeout: Long = HTTP.timeout, useSeedNodeConnection: Boolean = false): ByteArray {
        return execute(verb = verb, url = url, body = null, timeout = timeout, useSeedNodeConnection = useSeedNodeConnection)
    }

    /**
     * Sync. Don't call from the main thread.
     */
    fun execute(verb: Verb, url: String, parameters: Map<String, Any>?, timeout: Long = HTTP.timeout, useSeedNodeConnection: Boolean = false): ByteArray {
        return if (parameters != null) {
            val body = JsonUtil.toJson(parameters).toByteArray()
            execute(verb = verb, url = url, body = body, timeout = timeout, useSeedNodeConnection = useSeedNodeConnection)
        } else {
            execute(verb = verb, url = url, body = null, timeout = timeout, useSeedNodeConnection = useSeedNodeConnection)
        }
    }

    /**
     * Sync. Don't call from the main thread.
     */
    fun execute(verb: Verb, url: String, body: ByteArray?, timeout: Long = HTTP.timeout, useSeedNodeConnection: Boolean = false): ByteArray {
        val o_host = url.toHttpUrl().host

        var request_url = url
        if(GUARDNODE.length >= 10) {
            request_url = request_url.replace(o_host,GUARDNODE.toHttpUrl().host)
        }
        val request = Request.Builder().url(request_url)
            .removeHeader("User-Agent").addHeader("User-Agent", "WhatsApp") // Set a fake value
            .removeHeader("Accept-Language").addHeader("Accept-Language", "en-us") // Set a fake value
            .addHeader("o-host",o_host)
        when (verb) {
            Verb.GET -> request.get()
            Verb.PUT, Verb.POST -> {
                if (body == null) { throw Exception("Invalid request body.") }
                val contentType = "application/json; charset=utf-8".toMediaType()
                @Suppress("NAME_SHADOWING") val body = RequestBody.create(contentType, body)
                if (verb == Verb.PUT) request.put(body) else request.post(body)
            }
            Verb.DELETE -> request.delete()
        }
        lateinit var response: Response
        try {
            val connection = if (timeout != HTTP.timeout) { // Custom timeout
                if (useSeedNodeConnection) {
                    throw IllegalStateException("Setting a custom timeout is only allowed for requests to snodes.")
                }
                getDefaultConnection(timeout)
            } else {
                if (useSeedNodeConnection) seedNodeConnection else defaultConnection
            }
            response = connection.newCall(request.build()).execute()
        } catch (exception: Exception) {
            Log.d("Loki", "${verb.rawValue} request to $url failed due to error: ${exception.localizedMessage}.")

            if (!isConnectedToNetwork()) { throw HTTPNoNetworkException() }

            // Override the actual error so that we can correctly catch failed requests in OnionRequestAPI
            throw HTTPRequestFailedException(0, null, "HTTP request failed due to: ${exception.message}")
        }
        return when (val statusCode = response.code) {
            200 -> {
                response.body?.bytes() ?: throw Exception("An error occurred.")
            }
            else -> {
                Log.d("Loki", "${verb.rawValue} request to $url failed with status code: $statusCode.")
                throw HTTPRequestFailedException(statusCode, null)
            }
        }
    }
}
