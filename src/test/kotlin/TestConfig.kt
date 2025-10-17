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
        // Trust manager that does not validate certificate chains (for testing only)
        val trustAllCerts =
            arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String,
                    ) {}

                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String,
                    ) {}
                },
            )

        // Create SSL context using the trust-all trust manager
        val sslContext =
            SSLContext.getInstance("TLS").apply {
                init(null, trustAllCerts, SecureRandom())
            }

        // Create SSL socket factory with the SSL context
        val sslSocketFactory = SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE)

        // Register HTTP and HTTPS socket factories
        val socketFactoryRegistry =
            RegistryBuilder
                .create<org.apache.hc.client5.http.socket.ConnectionSocketFactory>()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build()

        // Create connection manager with custom socket factories
        val connectionManager = PoolingHttpClientConnectionManager(socketFactoryRegistry)

        // Create HTTP client using the connection manager
        val httpClient =
            HttpClients
                .custom()
                .setConnectionManager(connectionManager)
                .build()

        // Use the HTTP client for Spring's RestTemplate
        val requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)

        // Return TestRestTemplate for integration testing
        return TestRestTemplate(
            RestTemplateBuilder().requestFactory(Supplier { requestFactory }),
        )
    }
}
