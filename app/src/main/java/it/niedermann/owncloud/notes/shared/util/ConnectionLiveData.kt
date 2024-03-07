/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Öztürk <alper_ozturk@proton.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData

/**
 * LiveData subclass that provides network connection status updates.
 * It observes changes in network connectivity and posts updates to its observers.
 *
 * @property context The application context used to access system services.
 */
class ConnectionLiveData(val context: Context) : LiveData<ConnectionLiveData.ConnectionType>() {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkRequest = NetworkRequest.Builder().build()

    /**
     * Enum representing different types of network connections.
     */
    enum class ConnectionType {
        Lost, WiFi, Ethernet, MobileData, Other
    }

    init {
        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    val connectivityManager =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

                    if (networkCapabilities != null) {
                        when {
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                                postValue(ConnectionType.WiFi)
                            }

                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                                postValue(ConnectionType.Ethernet)
                            }

                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                                postValue(ConnectionType.MobileData)
                            }

                            else -> {
                                postValue(ConnectionType.Other)
                            }
                        }
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    postValue(ConnectionType.Lost)
                }
            }
        )
    }
}
