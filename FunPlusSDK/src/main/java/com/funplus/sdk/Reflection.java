package com.funplus.sdk;

import java.lang.reflect.Method;

class Reflection {

    static Object invokeStaticMethod(String className, String methodName, Class[] cArgs, Object... args) throws Exception {
        Class classObject = Class.forName(className);
        return invokeMethod(classObject, methodName, null, cArgs, args);
    }

    static Object invokeInstanceMethod(Object instance, String methodName, Class[] cArgs, Object... args) throws Exception {
        Class classObject = instance.getClass();
        return invokeMethod(classObject, methodName, instance, cArgs, args);
    }

    private static Object invokeMethod(Class classObject, String methodName, Object instance, Class[] cArgs, Object... args) throws Exception {
        @SuppressWarnings("unchecked")
        Method methodObject = classObject.getMethod(methodName, cArgs);

        return methodObject.invoke(instance, args);
    }
}
