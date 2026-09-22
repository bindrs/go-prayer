package com.example.service

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.NetworkInterface
import java.util.Collections

/**
 * Service to detect active VPN connections in real-time.
 * Uses ConnectivityManager network capabilities alongside physical network interface inspection.
 */
class VpnDetector(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isVpnActive = MutableStateFlow(false)
    val isVpnActive: StateFlow<Boolean> = _isVpnActive.asStateFlow()

    val isRealVpnConnected: StateFlow<Boolean> get() = isVpnActive

    private val _vpnInterfaceDetails = MutableStateFlow<String?>("None")
    val vpnInterfaceDetails: StateFlow<String?> = _vpnInterfaceDetails.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var vpnNetworkCallback: ConnectivityManager.NetworkCallback? = null
    private var defaultNetworkCallback: ConnectivityManager.NetworkCallback? = null
    private var pollingJob: Job? = null

    init {
        checkCurrentVpnStatus()
        registerNetworkCallbacks()
        startPeriodicCheck()
    }

    /**
     * Checks if a real VPN is currently running through ConnectivityManager capabilities and network interfaces.
     */
    fun checkCurrentVpnStatus(): Boolean {
        var isVpn = false
        var ifaceDetail: String? = null

        try {
            // Check 1: Active Network Capabilities via ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            if (activeNetwork != null) {
                val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
                if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    isVpn = true
                    ifaceDetail = "Active VPN Transport"
                }
            }

            // Check 2: All Network Capabilities via ConnectivityManager
            if (!isVpn) {
                val allNetworks = connectivityManager.allNetworks
                for (network in allNetworks) {
                    val caps = connectivityManager.getNetworkCapabilities(network)
                    if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        isVpn = true
                        ifaceDetail = "VPN Network Interface"
                        break
                    }
                }
            }

            // Check 3: Network Interfaces (tun, ppp, p2p, tap, wg, ipsec)
            if (!isVpn) {
                val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (iface in interfaces) {
                    if (iface.isUp) {
                        val name = iface.name.lowercase()
                        if (name.startsWith("tun") || name.startsWith("ppp") || 
                            name.startsWith("tap") || name.startsWith("wg") || 
                            name.contains("vpn") || name.startsWith("ipsec")) {
                            isVpn = true
                            ifaceDetail = "Tunnel Interface (${iface.name})"
                            break
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking VPN status: ${e.message}")
        }

        _isVpnActive.value = isVpn
        _vpnInterfaceDetails.value = if (isVpn) (ifaceDetail ?: "VPN Encrypted Tunnel") else "None"
        return isVpn
    }

    /**
     * Opens system VPN settings so the user can disconnect their VPN.
     */
    fun openVpnSettings() {
        try {
            val intent = Intent(Settings.ACTION_VPN_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to wireless settings
            try {
                val fallbackIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (_: Exception) {
                val generalIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(generalIntent)
            }
        }
    }

    private fun registerNetworkCallbacks() {
        try {
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                .build()

            vpnNetworkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "VPN network became available via ConnectivityManager")
                    _isVpnActive.value = true
                    _vpnInterfaceDetails.value = "Active Encrypted Tunnel"
                }

                override fun onLost(network: Network) {
                    Log.d(TAG, "VPN network lost via ConnectivityManager")
                    checkCurrentVpnStatus()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    val hasVpn = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                    if (hasVpn) {
                        _isVpnActive.value = true
                        _vpnInterfaceDetails.value = "Active VPN Transport"
                    } else {
                        checkCurrentVpnStatus()
                    }
                }
            }

            connectivityManager.registerNetworkCallback(request, vpnNetworkCallback!!)
        } catch (e: Exception) {
            Log.w(TAG, "Could not register dedicated VPN network callback: ${e.message}")
        }

        // Also monitor default network changes
        try {
            defaultNetworkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    checkCurrentVpnStatus()
                }

                override fun onLost(network: Network) {
                    checkCurrentVpnStatus()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    checkCurrentVpnStatus()
                }
            }
            connectivityManager.registerDefaultNetworkCallback(defaultNetworkCallback!!)
        } catch (e: Exception) {
            Log.w(TAG, "Could not register default network callback: ${e.message}")
        }
    }

    private fun startPeriodicCheck() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                delay(3000)
                checkCurrentVpnStatus()
            }
        }
    }

    fun unregister() {
        pollingJob?.cancel()
        vpnNetworkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (_: Exception) {}
        }
        defaultNetworkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (_: Exception) {}
        }
    }

    companion object {
        private const val TAG = "VpnDetector"
    }
}

