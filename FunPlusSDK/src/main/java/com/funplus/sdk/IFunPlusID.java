package com.funplus.sdk;

public interface IFunPlusID {
    String getCurrentFPID();
    void get(String externalID,
             FunPlusID.ExternalIDType externalIDType,
             FunPlusID.FunPlusIDHandler completion);
    void bind(String fpid,
              String externalID,
              FunPlusID.ExternalIDType externalIDType,
              FunPlusID.FunPlusIDHandler completion);
}
