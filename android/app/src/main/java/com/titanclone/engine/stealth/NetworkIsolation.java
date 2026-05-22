package com.titanclone.engine.stealth;

import android.util.Log;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-clone network identity isolation.
 *
 * Routes each clone's network traffic through a different SOCKS5 proxy
 * so that each clone has a unique public IP address. This prevents
 * Google from correlating multiple Play Store clones to the same device.
 *
 * Features:
 * - Per-clone SOCKS5 proxy configuration
 * - Per-clone DNS resolver
 * - Proxy health monitoring
 * - Socket-level proxy injection (hooks java.net.Socket.connect())
 */
public class NetworkIsolation {

    private static final String TAG = "NetworkIsolation";

    // Map: cloneId -> ProxyConfig
    private final Map<String, ProxyConfig> proxyConfigs = new HashMap<>();

    /**
     * Configure a SOCKS5 proxy for a clone.
     */
    public void setProxyForClone(String cloneId, String host, int port,
                                  String username, String password) {
        ProxyConfig config = new ProxyConfig(host, port, username, password);
        proxyConfigs.put(cloneId, config);
        Log.i(TAG, "Proxy set for " + cloneId + ": " + host + ":" + port);
    }

    /**
     * Configure DNS server for a clone.
     */
    public void setDnsForClone(String cloneId, String dnsServer) {
        ProxyConfig config = proxyConfigs.get(cloneId);
        if (config != null) {
            config.dnsServer = dnsServer;
        }
    }

    /**
     * Get the Java Proxy object for a clone.
     */
    public Proxy getProxyForClone(String cloneId) {
        ProxyConfig config = proxyConfigs.get(cloneId);
        if (config == null) return Proxy.NO_PROXY;

        return new Proxy(Proxy.Type.SOCKS,
                new InetSocketAddress(config.host, config.port));
    }

    /**
     * Get proxy config for socket-level interception.
     */
    public ProxyConfig getProxyConfig(String cloneId) {
        return proxyConfigs.get(cloneId);
    }

    /**
     * Remove proxy config when clone stops.
     */
    public void removeProxyForClone(String cloneId) {
        proxyConfigs.remove(cloneId);
    }

    /**
     * Check if a proxy is responsive.
     */
    public boolean checkProxyHealth(String cloneId) {
        ProxyConfig config = proxyConfigs.get(cloneId);
        if (config == null) return false;

        try {
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new InetSocketAddress(config.host, config.port), 5000);
            socket.close();
            config.healthy = true;
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Proxy unhealthy for " + cloneId + ": " + e.getMessage());
            config.healthy = false;
            return false;
        }
    }

    /**
     * Check all proxy health statuses.
     */
    public Map<String, Boolean> checkAllProxyHealth() {
        Map<String, Boolean> results = new HashMap<>();
        for (String cloneId : proxyConfigs.keySet()) {
            results.put(cloneId, checkProxyHealth(cloneId));
        }
        return results;
    }

    public static class ProxyConfig {
        public final String host;
        public final int port;
        public final String username;
        public final String password;
        public String dnsServer;
        public boolean healthy = true;

        ProxyConfig(String host, int port, String username, String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }
    }
}
