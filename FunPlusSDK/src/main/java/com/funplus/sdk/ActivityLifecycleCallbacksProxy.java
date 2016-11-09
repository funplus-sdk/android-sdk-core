package com.funplus.sdk;

import android.app.Application.ActivityLifecycleCallbacks;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

class ActivityLifecycleCallbacksProxy {
    @NonNull private final List<ActivityLifecycleCallbacks> instancesToRegister = new ArrayList<>();

    void add(@NonNull ActivityLifecycleCallbacks instance) {
        instancesToRegister.add(instance);
    }

    @NonNull List<ActivityLifecycleCallbacks> getInstancesToRegister() {
        return instancesToRegister;
    }
}
