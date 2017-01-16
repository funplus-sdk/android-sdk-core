package com.funplus.sdk;

import java.lang.reflect.Method;

/**
 * The <code>Reflection</code> class is used to make reflections callings.
 */
class Reflection {

    /**
     * Invoke a static method by name.
     *
     * @param className     The target class name.
     * @param methodName    The target method name.
     * @param cArgs         Types of method arguments.
     * @param args          Method arguments.
     * @return              An object representing the returning value of the method calling.
     * @throws Exception
     */
    static Object invokeStaticMethod(String className, String methodName, Class[] cArgs, Object... args) throws Exception {
        Class classObject = Class.forName(className);
        return invokeMethod(classObject, methodName, null, cArgs, args);
    }

    /**
     * Invoke an instance method by name.
     *
     * @param instance      The target instance.
     * @param methodName    The target method name.
     * @param cArgs         Types of method arguments.
     * @param args          Method arguments.
     * @return              An object representing the returning value of the method calling.
     * @throws Exception
     */
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
