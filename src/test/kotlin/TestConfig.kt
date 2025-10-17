package es.unizar.webeng.lab2

import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory
import org.apache.hc.core5.http.config.RegistryBuilder
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.function.Supplier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Configuration
class TestConfig {
    @Bean
    fun testRestTemplate(): TestRestTemplate {
        val trustAllCerts =
            arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String,
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String,
                    ) {
                    }
                },
            )

        val sslContext =
            SSLContext.getInstance("TLS").apply {
                init(null, trustAllCerts, SecureRandom())
            }

        val sslSocketFactory =
            SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE,
            )

        val socketFactoryRegistry =
            RegistryBuilder
                .create<org.apache.hc.client5.http.socket.ConnectionSocketFactory>()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build()

        val connectionManager = PoolingHttpClientConnectionManager(socketFactoryRegistry)
        val httpClient =
            HttpClients
                .custom()
                .setConnectionManager(connectionManager)
                .build()

        val factory = HttpComponentsClientHttpRequestFactory(httpClient)

        return TestRestTemplate(
            RestTemplateBuilder().requestFactory(Supplier { factory }),
        )
    }
}
