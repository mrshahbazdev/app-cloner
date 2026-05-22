package com.titanclone.engine.stubs;

import android.net.Uri;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Virtual ContentResolver proxy — routes content queries through
 * clone-specific ContentProviders.
 *
 * Hooked methods:
 * - query() — redirect authority to clone's provider
 * - insert() — redirect to clone's provider
 * - update() — redirect to clone's provider
 * - delete() — redirect to clone's provider
 * - call() — intercept cross-provider calls
 */
public class ContentResolverStub extends MethodInvocationProxy {

    private static final String TAG = "CRStub";

    // Map of original authority -> redirected authority per clone
    private final Map<String, String> authorityRedirects = new HashMap<>();

    @Override
    public String getName() {
        return "ContentResolver";
    }

    @Override
    public void inject() throws Throwable {
        addMethodHandler("query", this::handleQuery);
        addMethodHandler("insert", this::handleInsert);
        addMethodHandler("update", this::handleUpdate);
        addMethodHandler("delete", this::handleDelete);
        addMethodHandler("call", this::handleCall);

        // TODO: Intercept IContentProvider via ActivityThread
        markInjected();
    }

    public void addAuthorityRedirect(String original, String redirected) {
        authorityRedirects.put(original, redirected);
    }

    private Object handleQuery(Object original, Method method, Object[] args)
            throws Throwable {
        if (args != null && args.length > 0 && args[0] instanceof Uri) {
            args[0] = redirectUri((Uri) args[0]);
        }
        return method.invoke(original, args);
    }

    private Object handleInsert(Object original, Method method, Object[] args)
            throws Throwable {
        if (args != null && args.length > 0 && args[0] instanceof Uri) {
            args[0] = redirectUri((Uri) args[0]);
        }
        return method.invoke(original, args);
    }

    private Object handleUpdate(Object original, Method method, Object[] args)
            throws Throwable {
        if (args != null && args.length > 0 && args[0] instanceof Uri) {
            args[0] = redirectUri((Uri) args[0]);
        }
        return method.invoke(original, args);
    }

    private Object handleDelete(Object original, Method method, Object[] args)
            throws Throwable {
        if (args != null && args.length > 0 && args[0] instanceof Uri) {
            args[0] = redirectUri((Uri) args[0]);
        }
        return method.invoke(original, args);
    }

    private Object handleCall(Object original, Method method, Object[] args)
            throws Throwable {
        // TODO: Redirect cross-provider calls
        return method.invoke(original, args);
    }

    private Uri redirectUri(Uri originalUri) {
        String authority = originalUri.getAuthority();
        if (authority != null && authorityRedirects.containsKey(authority)) {
            String newAuthority = authorityRedirects.get(authority);
            Log.d(TAG, "Redirecting authority: " + authority + " -> " + newAuthority);
            return originalUri.buildUpon().authority(newAuthority).build();
        }
        return originalUri;
    }
}
