package com.funplus.sdk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

class ObjectReaderWriter {
    private static final String LOG_TAG = "ObjectReaderWriter";

    @Nullable static <T> T readObject(@NonNull FunPlusConfig funPlusConfig,
                                      @NonNull String filename,
                                      @NonNull String objectName,
                                      @NonNull Class<T> type) {
        Closeable closable = null;
        T object = null;
        try {
            FileInputStream inputStream = funPlusConfig.context.openFileInput(filename);
            closable = inputStream;

            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            closable = bufferedStream;

            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);
            closable = objectStream;

            try {
                object = type.cast(objectStream.readObject());
                Log.i(LOG_TAG, String.format(Locale.US, "Read %s", objectName));
            } catch (ClassNotFoundException e) {
                Log.w(LOG_TAG, String.format(Locale.US, "Failed to find %s class (%s)", objectName, e.getMessage()));
            } catch (ClassCastException e) {
                Log.w(LOG_TAG, String.format(Locale.US, "Failed to cast %s object (%s)", objectName, e.getMessage()));
            } catch (Exception e) {
                Log.w(LOG_TAG, String.format(Locale.US, "Failed to read %s object (%s)", objectName, e.getMessage()));
            }
        } catch (FileNotFoundException e) {
            Log.w(LOG_TAG, String.format(Locale.US, "%s file not found", objectName));
        } catch (Exception e) {
            Log.w(LOG_TAG, String.format(Locale.US, "Failed to open %s file for reading (%s)", objectName, e));
        }
        try {
            if (closable != null) {
                closable.close();
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, String.format(Locale.US, "Failed to close %s file for reading (%s)", objectName, e));
        }

        return object;
    }

    static <T> void writeObject(@NonNull T object,
                                @NonNull FunPlusConfig funPlusConfig,
                                @NonNull String filename,
                                @NonNull String objectName) {
        Closeable closable = null;
        try {
            FileOutputStream outputStream = funPlusConfig.context.openFileOutput(filename, Context.MODE_PRIVATE);
            closable = outputStream;

            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            closable = bufferedStream;

            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);
            closable = objectStream;

            try {
                objectStream.writeObject(object);

                Log.w(LOG_TAG, String.format(Locale.US, "Wrote %s", objectName));
            } catch (NotSerializableException e) {
                Log.w(LOG_TAG, String.format(Locale.US, "Failed to serialize %s", objectName));
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, String.format(Locale.US, "Failed to open %s for writing (%s)", objectName, e));
        }
        try {
            if (closable != null) {
                closable.close();
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, String.format(Locale.US, "Failed to close %s file for writing (%s)", objectName, e));
        }
    }
}