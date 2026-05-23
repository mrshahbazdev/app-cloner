package com.titanclone.engine.stubs;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for system service proxies. Provides the dynamic Proxy
 * framework for intercepting Android system service calls.
 *
 * Each subclass:
 * 1. Gets the real system service Binder
 * 2. Creates a dynamic Proxy wrapping the real service
 * 3. Registers method-specific handlers for interception
 * 4. Replaces the cached service singleton via reflection
 *
 * Thread-safe: method handlers are registered before injection.
 */
public abstract class MethodInvocationProxy implements InvocationHandler {

    private static final String TAG = "MethodInvocationProxy";

    protected Object originalService;
    private final Map<String, MethodHandler> methodHandlers = new HashMap<>();
    private boolean injected = false;

    /**
     * Subclasses implement this to set up method handlers and inject the proxy.
     */
    public abstract void inject() throws Throwable;

    /**
     * Get the name of this proxy (for logging).
     */
    public abstract String getName();

    /**
     * Register a handler for a specific method name.
     */
    protected void addMethodHandler(String methodName, MethodHandler handler) {
        methodHandlers.put(methodName, handler);
    }

    /**
     * Create a dynamic proxy wrapping the given interface.
     */
    protected Object createProxy(ClassLoader classLoader, Class<?>... interfaces) {
        return Proxy.newProxyInstance(classLoader, interfaces, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodHandler handler = methodHandlers.get(method.getName());
        if (handler != null) {
            try {
                return handler.handle(originalService, method, args);
            } catch (Exception e) {
                Log.w(TAG, getName() + "." + method.getName() + " handler error", e);
            }
        }
        // Fall through to real implementation
        return method.invoke(originalService, args);
    }

    public boolean isInjected() {
        return injected;
    }

    protected void markInjected() {
        this.injected = true;
        Log.i(TAG, getName() + " proxy injected");
    }

    /**
     * Handler interface for individual method interceptions.
     */
    public interface MethodHandler {
        Object handle(Object original, Method method, Object[] args) throws Throwable;
    }
}
