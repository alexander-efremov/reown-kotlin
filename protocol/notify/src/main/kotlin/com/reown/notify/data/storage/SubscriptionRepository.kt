@file:JvmSynthetic

package com.reown.notify.data.storage

import com.reown.android.internal.common.model.AccountId
import com.reown.android.internal.common.model.Expiry
import com.reown.android.internal.common.model.RelayProtocolOptions
import com.reown.foundation.common.model.PublicKey
import com.reown.foundation.common.model.Topic
import com.reown.notify.common.model.Scope
import com.reown.notify.common.model.Subscription
import com.reown.notify.common.storage.data.dao.ActiveSubscriptionsQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class SubscriptionRepository(private val activeSubscriptionsQueries: ActiveSubscriptionsQueries) {

    suspend fun setActiveSubscriptions(account: String, subscriptions: List<Subscription.Active>) = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.transaction {
            activeSubscriptionsQueries.deleteByAccount(account)
            subscriptions.forEach { subscription ->
                with(subscription) {

                    activeSubscriptionsQueries.insertOrAbortActiveSubscribtion(
                        account,
                        authenticationPublicKey.keyAsHex,
                        expiry.seconds,
                        relay.protocol,
                        relay.data,
                        mapOfScope.mapValues { scope -> Triple(scope.value.name, scope.value.description, scope.value.isSelected) },
                        topic.value,
                        requestedSubscriptionId
                    )
                }
            }
        }
    }

    suspend fun insertOrAbortSubscription(account: String, subscription: Subscription.Active) = withContext(Dispatchers.IO) {
        with(subscription) {
            activeSubscriptionsQueries.insertOrAbortActiveSubscribtion(
                account,
                authenticationPublicKey.keyAsHex,
                expiry.seconds,
                relay.protocol,
                relay.data,
                mapOfScope.mapValues { scope -> Triple(scope.value.name, scope.value.description, scope.value.isSelected) },
                topic.value,
                requestedSubscriptionId
            )
        }
    }


    suspend fun updateActiveSubscriptionWithLastNotificationId(lastNotificationId: String?, topic: String) = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.transaction {
            activeSubscriptionsQueries.updateActiveSubscriptionWithLastNotificationId(lastNotificationId, topic)
            activeSubscriptionsQueries.flagActiveSubscriptionAsReachedTheEndOfHistory(topic)
        }
    }

    suspend fun getActiveSubscriptionByNotifyTopic(notifyTopic: String): Subscription.Active? = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.getActiveSubscriptionByNotifyTopic(notifyTopic, ::toActiveSubscriptionWithoutMetadata).executeAsOneOrNull()
    }

    suspend fun getAllActiveSubscriptions(): List<Subscription.Active> = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.getAllActiveSubscriptions(::toActiveSubscriptionWithoutMetadata).executeAsList()
    }

    suspend fun getAccountActiveSubscriptions(accountId: AccountId): List<Subscription.Active> = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.getActiveSubscriptionsByAccount(accountId.value, ::toActiveSubscriptionWithoutMetadata).executeAsList()
    }

    suspend fun deleteSubscriptionByNotifyTopic(notifyTopic: String) = withContext(Dispatchers.IO) {
        activeSubscriptionsQueries.deleteByNotifyTopic(notifyTopic)
    }

    @Suppress("LocalVariableName")
    private fun toActiveSubscriptionWithoutMetadata(
        account: String,
        authentication_public_key: String,
        expiry: Long,
        relay_protocol: String,
        relay_data: String?,
        map_of_scope: Map<String, Triple<String, String, Boolean>>,
        notify_topic: String,
        requested_subscription_id: Long?,
        last_notification_id: String?,
        reached_end_of_history: Boolean,
    ): Subscription.Active = Subscription.Active(
        account = AccountId(account),
        authenticationPublicKey = PublicKey(authentication_public_key),
        mapOfScope = map_of_scope.map { entry ->
            entry.key to Scope.Cached(
                id = entry.key,
                name = entry.value.first,
                description = entry.value.second,
                isSelected = entry.value.third
            )
        }.toMap(),
        expiry = Expiry(expiry),
        relay = RelayProtocolOptions(relay_protocol, relay_data),
        topic = Topic(notify_topic),
        dappMetaData = null,
        requestedSubscriptionId = requested_subscription_id,
        lastNotificationId = last_notification_id,
        reachedEndOfHistory = reached_end_of_history
    )
}