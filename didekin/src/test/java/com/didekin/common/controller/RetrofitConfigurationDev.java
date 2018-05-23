package com.didekin.common.controller;

import com.didekinlib.http.HttpHandler;
import com.didekinlib.http.JksInClient;
import com.didekinlib.http.exception.ErrorBean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekinlib.http.GsonUtil.getGsonConverterTokenKey;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm;
import static javax.net.ssl.TrustManagerFactory.getInstance;
import static okhttp3.logging.HttpLoggingInterceptor.Level.BODY;

/**
 * User: pedro@didekin
 * Date: 20/04/16
 * Time: 16:33
 */
@Profile(NGINX_JETTY_LOCAL)
@Configuration
public class RetrofitConfigurationDev {

    private static final String jetty_local_URL = "https://didekinspring.pagekite.me/";
    private static final String local_jks_appclient = "/Users/pedro/keystores/didekin_web_local/didekin_web_local.pkcs12";
    private static final String local_jks_appclient_pswd = "octubre_5_2016_dev_jks";
    private static final int http_timeOut = 120;

    @Bean
    public HttpHandler retrofitHandler()
    {
        return new HttpHandler.HttpHandlerBuilder(jetty_local_URL)
                .okHttpClient(http_timeOut, new JksInAppClient(local_jks_appclient, local_jks_appclient_pswd))
                .build();
    }

    @Bean
    public HttpHandlerMock retrofitHandlerMock()
    {
        return new HttpHandlerMock(jetty_local_URL, new JksInAppClient(local_jks_appclient, local_jks_appclient_pswd), http_timeOut);
    }

    public static class HttpHandlerMock {

        private final Retrofit retrofit;

        public HttpHandlerMock(final String hostPort, int timeOut)
        {
            retrofit = new Retrofit.Builder()
                    .baseUrl(hostPort)
                    .client(getOkHttpClient(null, timeOut))
                    .addConverterFactory(getGsonConverterTokenKey())
                    .build();
        }

        public HttpHandlerMock(final String hostPort, final JksInClient jksInAppClient, int timeOut)
        {
            retrofit = new Retrofit.Builder()
                    .baseUrl(hostPort)
                    .client(getOkHttpClient(jksInAppClient, timeOut))
                    .addConverterFactory(getGsonConverterTokenKey())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }

        public <T> T getService(Class<T> endPointInterface)
        {
            return retrofit.create(endPointInterface);
        }

        public ErrorBean getErrorBean(Response<?> response) throws IOException
        {
            Converter<ResponseBody, ErrorBean> converter = retrofit.responseBodyConverter(ErrorBean.class, new Annotation[0]);
            ErrorBean errorBean = converter.convert(response.errorBody());
            if (errorBean == null || errorBean.getMessage() == null) {
                okhttp3.Response okhttpResponse = response.raw();
                errorBean = new ErrorBean(okhttpResponse.message(), okhttpResponse.code());
            }
            return errorBean;
        }

        // ====================== HELPER METHODS ========================

        private OkHttpClient getOkHttpClient(JksInClient jksInAppClient, int timeOut)
        {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .addNetworkInterceptor(doLoggingInterceptor())
                    .connectTimeout(timeOut, SECONDS)
                    .readTimeout(timeOut * 2, SECONDS);
            if (jksInAppClient == null) {
                return builder.build();
            } else {
                X509TrustManager trustManager = getTrustManager(jksInAppClient);
                return builder.sslSocketFactory(getSslSocketFactory(trustManager), trustManager)
                        .build();
            }
        }

        private Interceptor doLoggingInterceptor()
        {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(BODY);
            return loggingInterceptor;
        }

        private X509TrustManager getTrustManager(JksInClient jksInAppClient)
        {
            KeyStore keyStore;
            TrustManagerFactory tmf;

            try {
                // Configuración cliente.
                String keyStoreType = KeyStore.getDefaultType();
                keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(jksInAppClient.getInputStream(), jksInAppClient.getJksPswd().toCharArray());
                // Create a TrustManager that trusts the CAs in our JksInAppClient
                tmf = getInstance(getDefaultAlgorithm());
                tmf.init(keyStore);

                TrustManager[] trustManagers = tmf.getTrustManagers();
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                    throw new IllegalStateException("Unexpected default trust managers:"
                            + Arrays.toString(trustManagers));
                }
                return (X509TrustManager) trustManagers[0];
            } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException("TrustManager not initialized");
            }
        }

        private SSLSocketFactory getSslSocketFactory(TrustManager trustManager)
        {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{trustManager}, null);
                return sslContext.getSocketFactory();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException("SSLSocketFactory not initialized");
            }
        }
    }
}