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
        void onSuccess(String fpid);
        void onFailure(Error error);
    }

    @NonNull private final PassportClient passportClient;

    FunPlusID(@NonNull FunPlusConfig funPlusConfig) {
        passportClient = new PassportClient(funPlusConfig);
    }

    @Override
    public void get(@NonNull String externalID,
                    @NonNull ExternalIDType externalIDType,
                    @NonNull FunPlusIDHandler completion) {
        passportClient.get(externalID, externalIDType, completion);
    }

    @Override
    public void bind(@NonNull String fpid,
                     @NonNull String externalID,
                     @NonNull ExternalIDType externalIDType,
                     @NonNull FunPlusIDHandler completion) {
        passportClient.bind(fpid, externalID, externalIDType, completion);
    }

    @Override
    @NonNull public String getCurrentFPID() {
        return passportClient.getCurrentFPID();
    }
}
