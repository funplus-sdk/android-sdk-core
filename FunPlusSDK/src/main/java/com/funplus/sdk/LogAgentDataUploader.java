package com.funplus.sdk;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

class LogAgentDataUploader {
    private static final String LOG_TAG = "LogAgentDataUploader";
    private static final int MAX_BATCH_SIZE = 100;

    interface CompletionHandler {
        /**
         * This method will be called when uploading finishes.
         *
         * @param status    The status of this uploading process.
         * @param total     The total count of logs.
         * @param uploaded  The count of logs uploaded. If status is <code>true</code>,
         *                  this value should equal to the <code>total</code> value.
         */
        void onComplete(boolean status, int total, int uploaded);
    }

    @NonNull private final FunPlusConfig funPlusConfig;
    @NonNull private final String endpoint;
    @NonNull private final String tag;
    @NonNull private final String key;

    LogAgentDataUploader(@NonNull FunPlusConfig funPlusConfig,
                         @NonNull String endpoint,
                         @NonNull String tag,
                         @NonNull String key) {
        this.funPlusConfig = funPlusConfig;
        this.endpoint = endpoint;
        this.tag = tag;
        this.key = key;
    }

    void upload(@NonNull List<String> data,
                @NonNull CompletionHandler completionHandler) {
        uploadInternal(data, completionHandler, data.size(), 0);
    }

    private void uploadInternal(@NonNull final List<String> data,
                                @NonNull final CompletionHandler completionHandler,
                                final int total,
                                final int uploaded) {
        // Upload completes? Return.
        if (total <= uploaded) {
            completionHandler.onComplete(total == uploaded, total, uploaded);
            return;
        }

        // Batch size must not exceed MAX_BATCH_SIZE.
        final int batchSize = (total - uploaded > MAX_BATCH_SIZE) ? MAX_BATCH_SIZE : total - uploaded;
        List<String> batch = data.subList(0, batchSize);

        long timestamp = System.currentTimeMillis();
        String sig = DeviceInfo.MD5(String.format(Locale.US, "%s:%d:%s", tag, timestamp, key));

        if (sig == null) {
            // Should never reach here.
            Log.e(LOG_TAG, "Upload failed: signature should not be null");
            completionHandler.onComplete(false, total, uploaded);
            return;
        }

        String url = String.format(Locale.US, "%s?tag=%s&timestamp=%d&num=%d&signature=%s", endpoint, tag, timestamp, batchSize, sig);
        String requestBody = TextUtils.join("\n", batch);
        
        DataUploadRequest request = new DataUploadRequest(url, requestBody, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                List<String> remainingData = data.subList(batchSize, data.size());
                // Recursion.
                uploadInternal(remainingData, completionHandler, total, uploaded + batchSize);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Break the recursion.
                completionHandler.onComplete(false, total, uploaded);
            }
        });

        FunPlusFactory.getRequestQueue(funPlusConfig.context).add(request);
    }

    private class DataUploadRequest extends Request<String> {

        @NonNull private final String requestBody;
        @NonNull private final Response.Listener<String> listener;

        DataUploadRequest(@NonNull String url,
                          @NonNull String requestBody,
                          @NonNull Response.Listener<String> listener,
                          @NonNull Response.ErrorListener errorListener) {
            super(Method.POST, url, errorListener);
            this.requestBody = requestBody;
            this.listener = listener;

            this.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }

        @Override
        @NonNull public byte[] getBody() throws AuthFailureError {
            return requestBody.getBytes();
        }

        @Override
        protected void deliverResponse(@NonNull String response) {
            listener.onResponse(response);
        }

        @Override
        @NonNull protected Response<String> parseNetworkResponse(@NonNull NetworkResponse response) {
            if (response.statusCode != 200) {
                return Response.error(new VolleyError(response));
            }

            try {
                String responseBody = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                if (responseBody.equals("OK")) {
                    return Response.success(responseBody, HttpHeaderParser.parseCacheHeaders(response));
                } else {
                    return Response.error(new VolleyError(response));
                }
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(response));
            }
        }
    }
}
