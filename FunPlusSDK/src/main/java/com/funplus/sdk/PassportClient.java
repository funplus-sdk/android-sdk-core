package com.funplus.sdk;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The <code>PassportClient</code> class communicates with the Passport Server. It is responsible
 * for sending request to the Passport Server and validate and parse the response.
 */
class PassportClient {
    private static final String CURRENT_FPID_SAVED_KEY = "com.funplus.sdk.CurrentFPID";

    private static final String SANDBOX_ENDPOINT = "https://passport-dev.funplusgame.com/client_api.php";
    private static final String PRODUCTION_ENDPOINT = "https://passport.funplusgame.com/client_api.php";

    private static final int API_VERSION = 4;

    @NonNull private final FunPlusConfig funPlusConfig;
    @NonNull private String currentFPID;

    /**
     * Constructor.
     *
     * @param funPlusConfig     The config object.
     */
    PassportClient(@NonNull FunPlusConfig funPlusConfig) {
        this.funPlusConfig = funPlusConfig;

        // Try to restore FPID from local.
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(funPlusConfig.context);
        currentFPID = pref.getString(CURRENT_FPID_SAVED_KEY, "");

        // If empty, use android ID instead.
        if (currentFPID.isEmpty()) {
            currentFPID = FunPlusFactory.getDeviceInfo(funPlusConfig.context).androidId;
        }
    }

    /**
     * Get current FPID.
     *
     * @return  Current FPID.
     */
    @NonNull String getCurrentFPID() {
        return currentFPID;
    }

    /**
     * Get (retrieve or create) the FPID associated with given external user ID.
     *
     * @param externalID        The external user ID.
     * @param externalIDType    Type of the external user ID.
     * @param completion        The completion callback.
     */
    void get(@NonNull String externalID,
             @NonNull FunPlusID.ExternalIDType externalIDType,
             @NonNull FunPlusID.FunPlusIDHandler completion) {
        Map<String, String> params = new HashMap<>();
        params.put("method", "get");
        params.put("game_id", funPlusConfig.appId);
        params.put("guid", externalID);
        params.put("guid_type", externalIDType.rawValue);

        request(params, completion);
    }

    /**
     * Bind the given external user ID to given FPID.
     *
     * @param fpid              The FPID.
     * @param externalID        The external user ID.
     * @param externalIDType    Type of the external user ID.
     * @param completion        The completion callback.
     */
    void bind(@NonNull String fpid,
              @NonNull String externalID,
              @NonNull FunPlusID.ExternalIDType externalIDType,
              @NonNull FunPlusID.FunPlusIDHandler completion) {
        Map<String, String> params = new HashMap<>();
        params.put("method", "bind");
        params.put("game_id", funPlusConfig.appId);
        params.put("fpid", fpid);
        params.put("guid", externalID);
        params.put("guid_type", externalIDType.rawValue);

        request(params, completion);
    }

    private void request(@NonNull Map<String, String> params,
                         @NonNull final FunPlusID.FunPlusIDHandler completion) {
        String endpoint = funPlusConfig.environment.equals(SDKEnvironment.Sandbox) ? SANDBOX_ENDPOINT : PRODUCTION_ENDPOINT;
        String url = String.format(Locale.US, "%s?ver=%d", endpoint, API_VERSION);
        String sig = makeSignature(params);

        if (sig == null) {
            getLogger().e("Signature must not be null");
            completion.onFailure(FunPlusID.Error.SigError);
            return;
        }

        PassportRequest request = new PassportRequest(url, params, sig, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject data = response.getJSONObject("data");
                    String fpid = data.getString("fpid");

                    if (!fpid.equals(currentFPID)) {
                        updateFPID(fpid);

                        boolean isNew;

                        try {
                            isNew = data.getBoolean("is_new");
                        } catch (JSONException e) {
                            isNew = false;
                        }

                        if (isNew) {
                            FunPlusFactory.getFunPlusData(funPlusConfig).traceNewUser();
                        }
                    }

                    String sessionKey = data.getString("session_key");
                    if (sessionKey == null) {
                        sessionKey = "";
                    }

                    long expireIn = data.getLong("session_expire_in");

                    completion.onSuccess(fpid, sessionKey, expireIn);
                } catch (JSONException e) {
                    completion.onFailure(FunPlusID.Error.ParseError);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                completion.onFailure(FunPlusID.Error.NetworkError);
            }
        });

        FunPlusFactory.getRequestQueue(funPlusConfig.context).add(request);
    }

    private void updateFPID(@NonNull String newFPID) {
        currentFPID = newFPID;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(funPlusConfig.context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(CURRENT_FPID_SAVED_KEY, currentFPID);
        editor.apply();

        FunPlusFactory.getSessionManager(funPlusConfig).onUserIdChanged(currentFPID);
    }

    @NonNull private ILogger getLogger() {
        return FunPlusFactory.getLogger(funPlusConfig);
    }

    @Nullable private String makeSignature(@NonNull Map<String, String> params) {
        StringBuilder plain = new StringBuilder();
        for (ConcurrentHashMap.Entry<String, String> entry : params.entrySet()) {
            if (plain.length() > 0)
                plain.append("&");

            plain.append(entry.getKey()).append("=").append(entry.getValue());
        }

        String cipher = DeviceInfo.encryptHMACSha256(plain.toString(), funPlusConfig.appKey);
        if (cipher != null) {
            return String.format("%s%s:%s", "FP ", funPlusConfig.appId, cipher);
        } else {
            return null;
        }
    }

    private class PassportRequest extends Request<JSONObject> {

        @NonNull private final Response.Listener<JSONObject> listener;
        @NonNull private final Map<String, String> parameters;
        @NonNull private final String auth;

        PassportRequest(@NonNull String url,
                        @NonNull Map<String, String> parameters,
                        @NonNull String auth,
                        @NonNull Response.Listener<JSONObject> listener,
                        @NonNull Response.ErrorListener errorListener) {
            super(Method.POST, url, errorListener);
            this.listener = listener;
            this.parameters = parameters;
            this.auth = auth;
        }

        @Override
        @NonNull protected Map<String, String> getParams() throws AuthFailureError {
            return parameters;
        }

        @Override
        @Nullable public byte[] getBody() throws AuthFailureError {
            StringBuilder sb = new StringBuilder();
            try {
                for (ConcurrentHashMap.Entry<String, String> entry : parameters.entrySet()) {
                    if (sb.length() > 0) {
                        sb.append('&');
                    }
                    sb.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append('=').append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                return sb.toString().getBytes();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        @NonNull public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", auth);
            return headers;
        }

        @Override
        protected void deliverResponse(@NonNull JSONObject response) {
            listener.onResponse(response);
        }

        @Override
        @NonNull protected Response<JSONObject> parseNetworkResponse(@NonNull NetworkResponse response) {
            try {
                String resp = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                return Response.success(new JSONObject(resp), HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException | JSONException e) {
                return Response.error(new ParseError(e));
            }
        }
    }
}
