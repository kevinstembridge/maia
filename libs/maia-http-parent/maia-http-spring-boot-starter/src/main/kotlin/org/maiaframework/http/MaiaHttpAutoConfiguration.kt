package org.maiaframework.http

import org.apache.hc.client5.http.DnsResolver
import org.apache.hc.client5.http.SystemDefaultDnsResolver
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.client5.http.io.HttpClientConnectionManager
import org.apache.hc.client5.http.io.ManagedHttpClientConnection
import org.apache.hc.client5.http.socket.ConnectionSocketFactory
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory
import org.apache.hc.core5.http.config.Registry
import org.apache.hc.core5.http.config.RegistryBuilder
import org.apache.hc.core5.http.io.HttpConnectionFactory
import org.apache.hc.core5.ssl.SSLContexts
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import javax.net.ssl.SSLContext


@AutoConfiguration
class MaiaHttpAutoConfiguration {


    @Bean
    fun sslContext(): SSLContext {

        return SSLContexts.createSystemDefault()

    }


    @Bean
    fun dnsResolver(): DnsResolver {

        return SystemDefaultDnsResolver()

    }


    @Bean
    fun connectionFactory(): HttpConnectionFactory<ManagedHttpClientConnection> {

        return ManagedHttpClientConnectionFactory()

    }


    @Bean
    fun socketFactoryRegistry(
            sslContext: SSLContext
    ): Registry<ConnectionSocketFactory> {

        return RegistryBuilder.create<ConnectionSocketFactory>()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                .build()

    }


    @Bean
    fun httpClientConnectionManager(
            socketFactoryRegistry: Registry<ConnectionSocketFactory>,
            connectionFactory: HttpConnectionFactory<ManagedHttpClientConnection>
    ): HttpClientConnectionManager {

        return PoolingHttpClientConnectionManager(
                socketFactoryRegistry,
                connectionFactory
        )

    }

    @Bean
    fun httpClient(
            connectionManager: HttpClientConnectionManager
    ): CloseableHttpClient {

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build()

    }


}
