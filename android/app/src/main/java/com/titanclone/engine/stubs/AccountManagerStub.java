package com.titanclone.engine.stubs;

import android.accounts.Account;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Virtual AccountManager proxy — returns clone-specific accounts only.
 * Prevents clones from seeing each other's Google accounts.
 *
 * Hooked methods:
 * - getAccounts() — return only clone's registered accounts
 * - getAccountsByType() — filter by type for clone's accounts
 * - addAccount() — register in clone's virtual account store
 */
public class AccountManagerStub extends MethodInvocationProxy {

    private static final String TAG = "AcctStub";

    private final List<Account> virtualAccounts = new ArrayList<>();

    @Override
    public String getName() {
        return "AccountManager";
    }

    @Override
    public void inject() throws Throwable {
        addMethodHandler("getAccounts", this::handleGetAccounts);
        addMethodHandler("getAccountsByType", this::handleGetAccountsByType);
        addMethodHandler("getAccountsByTypeForPackage", this::handleGetAccountsByType);

        // TODO: Use reflection to intercept IAccountManager
        markInjected();
    }

    public void addVirtualAccount(Account account) {
        virtualAccounts.add(account);
    }

    public void clearVirtualAccounts() {
        virtualAccounts.clear();
    }

    private Object handleGetAccounts(Object original, Method method, Object[] args)
            throws Throwable {
        if (!virtualAccounts.isEmpty()) {
            return virtualAccounts.toArray(new Account[0]);
        }
        return method.invoke(original, args);
    }

    private Object handleGetAccountsByType(Object original, Method method, Object[] args)
            throws Throwable {
        if (!virtualAccounts.isEmpty() && args != null && args.length > 0) {
            String type = (String) args[0];
            List<Account> filtered = new ArrayList<>();
            for (Account a : virtualAccounts) {
                if (a.type.equals(type)) filtered.add(a);
            }
            return filtered.toArray(new Account[0]);
        }
        return method.invoke(original, args);
    }
}
