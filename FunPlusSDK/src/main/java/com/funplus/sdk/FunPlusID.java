package com.funplus.sdk;

import android.support.annotation.NonNull;

public class FunPlusID implements IFunPlusID {

    public enum Error {
        UnknownError,
        SigError,
        ParseError,
        NetworkError,
    }

    public enum ExternalIDType {
        GUID("guid"),
        Email("email"),
        FacebookID("facebook_id"),
        InAppUserID("inapp_user_id");

        @NonNull final String rawValue;

        ExternalIDType(@NonNull String rawValue) {
            this.rawValue = rawValue;
        }

        public static ExternalIDType construct(@NonNull String rawValue) {
            switch (rawValue.toLowerCase()) {
                case "guid":
                    return GUID;
                case "email":
                    return Email;
                case "facebook_id":
                    return FacebookID;
                default:
                    return InAppUserID;
            }
        }
    }

    public interface FunPlusIDHandler {
        void onSuccess(String fpid, String sessionKey, long expireIn);
        void onFailure(Error error);
    }

    @NonNull private final PassportClient passportClient;

    /**
     * Constructor.
     *
     * @param funPlusConfig     The config object.
     */
    FunPlusID(@NonNull FunPlusConfig funPlusConfig) {
        passportClient = new PassportClient(funPlusConfig);
    }

    /**
     * Get (retrieve or create) the FPID associated with given external user ID.
     *
     * @param externalID        The external user ID.
     * @param externalIDType    Type of the external user ID.
     * @param completion        The completion callback.
     */
    @Override
    public void get(@NonNull String externalID,
                    @NonNull ExternalIDType externalIDType,
                    @NonNull FunPlusIDHandler completion) {
        passportClient.get(externalID, externalIDType, completion);
    }

    /**
     * Bind the given external user ID to given FPID.
     *
     * @param fpid              The FPID.
     * @param externalID        The external user ID.
     * @param externalIDType    Type of the external user ID.
     * @param completion        The completion callback.
     */
    @Override
    public void bind(@NonNull String fpid,
                     @NonNull String externalID,
                     @NonNull ExternalIDType externalIDType,
                     @NonNull FunPlusIDHandler completion) {
        passportClient.bind(fpid, externalID, externalIDType, completion);
    }

    /**
     * Get the current FPID.
     *
     * @return  Current FPID.
     */
    @Override
    @NonNull public String getCurrentFPID() {
        return passportClient.getCurrentFPID();
    }
}
