package com.aura.wallpapers.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface BillingManager {
    val isPremiumUser: StateFlow<Boolean>
    fun startBillingConnection()
    fun launchPurchaseFlow(activity: Activity, skuId: String)
    fun endConnection()
}

@Singleton
class GooglePlayBillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : BillingManager, PurchasesUpdatedListener {

    private val _isPremiumUser = MutableStateFlow(false)
    override val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    private var billingClient: BillingClient? = null

    override fun startBillingConnection() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Reconnect on next user attempt
            }
        })
    }

    private fun queryPurchases() {
        // Query active one-time unlocks or subscriptions
        // If present, _isPremiumUser.value = true
    }

    override fun launchPurchaseFlow(activity: Activity, skuId: String) {
        // Safe Google Play billing flow trigger
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<com.android.billingclient.api.Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            _isPremiumUser.value = true
        }
    }

    override fun endConnection() {
        billingClient?.endConnection()
        billingClient = null
    }
}
