package com.funplus.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class DeviceInfo {
    private static final String PLAY_AD_ID_SAVED_KEY = "com.funplus.sdk.PlayAdId";

    @NonNull final String androidId;
    @NonNull final String appVersion;
    @NonNull final String deviceType;
    @NonNull final String deviceName;
    @NonNull final String deviceManufacturer;
    @NonNull final String osName;
    @NonNull final String osVersion;
    @NonNull final String apiLevel;
    @NonNull final String language;
    @NonNull final String country;
    @NonNull final String hardwareName;
    @Nullable String playAdId;

    /**
     * Constructor.
     *
     * @param context       The current context.
     */
    DeviceInfo(@NonNull Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        int screenLayout = configuration.screenLayout;
        Locale locale = Locale.getDefault();

        androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        appVersion = getAppVersion(context);
        deviceType = getDeviceType(screenLayout);
        deviceName = Build.MODEL;
        deviceManufacturer = Build.MANUFACTURER;
        osName = "android";
        osVersion = Build.VERSION.RELEASE;
        apiLevel = "" + Build.VERSION.SDK_INT;
        language = locale.getLanguage();
        country = locale.getDisplayCountry();
        hardwareName = Build.DISPLAY;
        playAdId = getPlayAdId(context);
    }

    /**
     * Get app's version.
     *
     * @param context       The current context.
     * @return              App's version.
     */
    @NonNull private String getAppVersion(@NonNull Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            String name = context.getPackageName();
            PackageInfo info = pm.getPackageInfo(name, 0);
            if (info.versionName != null) {
                return info.versionName + "." + info.versionCode;
            } else {
                return "unknown";
            }
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }

    /**
     * Get device type.
     *
     * @param screenLayout      The screen layout.
     * @return                  Device type.
     */
    @NonNull private String getDeviceType(int screenLayout) {
        int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "phone";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return "tablet";
            default:
                return "unknown";
        }
    }

    /**
     * Get Google Play Advertising ID.
     *
     * @param context       The current context.
     * @return              The Google Play Advertising ID.
     */
    @Nullable private static String getPlayAdId(@NonNull Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String playAdId = preferences.getString(PLAY_AD_ID_SAVED_KEY, null);

        if (playAdId == null) {
            PlayServicesHelper.getPlayAdId(context, new PlayServicesHelper.PlayAdIdReadListener() {
                @Override
                public void onPlayAdIdRead(String playAdId) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(PLAY_AD_ID_SAVED_KEY, playAdId);
                    editor.apply();
                }
            });
        }

        return playAdId;
    }

    /**
     * Calculate the MD5 hash for given text.
     *
     * @param text      The given text.
     * @return          The MD5 hash.
     */
    @Nullable static String MD5(@NonNull String text) {
        try {
            byte[] bytes = text.getBytes("UTF-8");
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(bytes, 0, bytes.length);

            byte[] hash = messageDigest.digest();
            BigInteger bigInt = new BigInteger(1, hash);
            String formatString = "%0" + (hash.length << 1) + "x";

            return String.format(Locale.US, formatString, bigInt);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Encrypt given text using HMACSha256.
     *
     * @param plain     The plain text.
     * @param key       The key.
     * @return          The cipher text.
     */
    @Nullable static String encryptHMACSha256(@NonNull String plain,
                                              @NonNull String key) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            hmacSha256.init(secretKeySpec);
            byte[] cipherBytes = hmacSha256.doFinal(plain.getBytes());

            final BigInteger bigInt = new BigInteger(1, cipherBytes);
            final String formatString = "%0" + (cipherBytes.length << 1) + "x";
            String cipherStr = String.format(formatString, bigInt);

            return Base64.encodeToString(cipherStr.getBytes(), Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }
}
