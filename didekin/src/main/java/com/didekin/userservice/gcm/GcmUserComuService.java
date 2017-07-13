package com.didekin.userservice.gcm;

import com.didekin.userservice.repository.UsuarioServiceIf;
import com.didekinlib.gcm.model.common.GcmException;
import com.didekinlib.gcm.model.common.GcmMulticastRequest;
import com.didekinlib.gcm.model.common.GcmRequest;
import com.didekinlib.gcm.model.common.GcmRequestData;
import com.didekinlib.gcm.model.common.GcmResponse;
import com.didekinlib.gcm.model.common.GcmTokensHolder;
import com.didekinlib.gcm.retrofit.GcmEndPointImp;
import com.didekinlib.model.common.gcm.GcmToComunidadHelper;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import static com.didekin.ThreadPoolConstants.KEEP_ALIVE_MILLISEC;
import static com.didekin.ThreadPoolConstants.MAX_THREADS_GCM;
import static com.didekin.ThreadPoolConstants.TERMINATION_TIMEOUT;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Queues.newLinkedBlockingQueue;
import static com.google.common.util.concurrent.MoreExecutors.getExitingExecutorService;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * User: pedro
 * Date: 28/07/16
 * Time: 18:57
 */
@Service
class GcmUserComuService implements GcmUserComuServiceIf {

    private static final Logger logger = LoggerFactory.getLogger(GcmUserComuService.class.getCanonicalName());

    private String didekin_package = "com.didekindroid";
    private String didekin_firebase_project_key =
            "AAAADknoTJQ:APA91bGRihWJup9TYYtKl6LV7d01f5DZJDr5edlwh8KV4fLzq8S20OYyxnqP7Hsj2b4B4zDU0G_jzDH8bOwXGlz77XMFzcPWnEZ8EcDqTbiNTSjDHxuegT2eE8Dsn9YvozF4GIbIHFaJ";
    private String didekin_api_key_header = "key=" + didekin_firebase_project_key;

    /**
     * This service sends the messages and  processes the results.
     */
    private final ListeningExecutorService gcmSenderExec;
    private final ThreadPoolExecutor gcmSenderPool;
    /**
     * This service processes the results of a GCM message.
     */
    private final ExecutorService gcmUpdaterExec;
    private final ThreadPoolExecutor gcmUpdaterPool;


    private GcmEndPointImp gcmEndPoint;
    private UsuarioServiceIf usuarioService;

    private GcmUserComuService()
    {
        // TODO: check dimensions.
        gcmSenderPool = new ThreadPoolExecutor(MAX_THREADS_GCM, MAX_THREADS_GCM, KEEP_ALIVE_MILLISEC, MILLISECONDS, newLinkedBlockingQueue(1000));
        gcmSenderPool.allowCoreThreadTimeOut(true);
        gcmSenderPool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        gcmSenderExec = listeningDecorator(
                getExitingExecutorService(gcmSenderPool, TERMINATION_TIMEOUT, SECONDS));

        gcmUpdaterPool = (ThreadPoolExecutor) newCachedThreadPool();
        gcmUpdaterPool.allowCoreThreadTimeOut(true);
        gcmUpdaterPool.setKeepAliveTime(KEEP_ALIVE_MILLISEC, MILLISECONDS);
        gcmUpdaterExec = getExitingExecutorService(gcmUpdaterPool, TERMINATION_TIMEOUT, SECONDS);
    }

    @Autowired
    public GcmUserComuService(GcmEndPointImp gcmEndPoint, UsuarioServiceIf usuarioService)
    {
        this();
        this.gcmEndPoint = gcmEndPoint;
        this.usuarioService = usuarioService;
    }

    @Override
    public void sendGcmMessageToComunidad(final GcmToComunidadHelper tokensHelper, final GcmRequestData requestData)
    {
        logger.debug(String.format("Threads in pools:  sender = %d, updater = %d %n", gcmSenderPool.getPoolSize(), gcmUpdaterPool.getPoolSize()));

        final List<String> gcmTokens = usuarioService.getGcmTokensByComunidad(tokensHelper.getComunidadId());

        if (gcmTokens.size() <= 0) {
            return;
        }

        // TODO: Migrate to RX_JAVA.

        Callable<GcmResponse> taskGcmMsg = () -> {
            logger.debug("Sending message-notification.");
            GcmMulticastRequest request = new GcmMulticastRequest.Builder(
                    gcmTokens,
                    new GcmRequest.Builder(requestData, didekin_package).build())
                    .build();
            return gcmEndPoint.sendMulticastGzip(didekin_api_key_header, request);
        };

        try {
            // If queue is full, throws exception.
            ListenableFuture<GcmResponse> gcmSendFuture = gcmSenderExec.submit(taskGcmMsg);

            Futures.addCallback(
                    gcmSendFuture,
                    new FutureCallback<GcmResponse>() {

                        @Override
                        public void onSuccess(GcmResponse response)
                        {
                            checkArgument(response != null);
                            List<GcmTokensHolder> tokensToProcess = response.getTokensToProcess();
                            if (tokensToProcess.size() > 0) {
                                usuarioService.modifyUserGcmTokens(tokensToProcess);
                            }
                        }

                        @Override
                        public void onFailure(Throwable cause)
                        {
                            if (!gcmSendFuture.isCancelled()) {
                                gcmSendFuture.cancel(true);
                            }
                            if (cause instanceof GcmException) { // Checked exception
                                GcmException gce = (GcmException) cause;
                                logger.error(" Error:" + gce.getErrorBean().getHttpStatus() + " " + gce.getErrorBean().getMessage());
                            } else if (cause instanceof RuntimeException) {  // Unchecked exception
                                throw new RuntimeException(cause);
                            } else {  // Error
                                throw (Error) cause;
                            }
                        }
                    },
                    gcmUpdaterExec);
        } catch (RejectedExecutionException e) {
            logger.error(String.format("Threads in pool = %d; %s%n", gcmSenderPool.getPoolSize(), e.getCause()));
        }
    }

/*    ================================  TEST HELPER METHODS ============================*/

    @Override
    public ThreadPoolExecutor getGcmSenderExec()
    {
        return gcmSenderPool;
    }

    @Override
    public ThreadPoolExecutor getGcmUpdaterExec()
    {
        return gcmUpdaterPool;
    }
}