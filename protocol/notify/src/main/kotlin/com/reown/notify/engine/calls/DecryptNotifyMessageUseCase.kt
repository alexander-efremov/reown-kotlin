@file:JvmSynthetic

package com.reown.notify.engine.calls

import com.reown.android.Core
import com.reown.android.internal.common.crypto.codec.Codec
import com.reown.android.internal.common.crypto.sha256
import com.reown.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.reown.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.reown.android.internal.common.model.AppMetaData
import com.reown.android.internal.common.model.AppMetaDataType
import com.reown.android.internal.common.model.params.CoreNotifyParams
import com.reown.android.internal.common.model.sync.ClientJsonRpc
import com.reown.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.reown.android.internal.common.storage.rpc.JsonRpcHistory
import com.reown.android.push.notifications.DecryptMessageUseCaseInterface
import com.reown.foundation.common.model.Topic
import com.reown.foundation.util.Logger
import com.reown.notify.common.model.Notification
import com.reown.notify.common.model.NotificationMessage
import com.reown.notify.common.model.toCore
import com.reown.notify.data.jwt.message.MessageRequestJwtClaim
import com.reown.notify.data.storage.NotificationsRepository
import kotlinx.coroutines.supervisorScope
import org.bouncycastle.util.encoders.Base64
import kotlin.reflect.safeCast

internal class DecryptNotifyMessageUseCase(
    private val codec: Codec,
    private val serializer: JsonRpcSerializer,
    private val jsonRpcHistory: JsonRpcHistory,
    private val notificationsRepository: NotificationsRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val logger: Logger
) : DecryptMessageUseCaseInterface {

    override suspend fun decryptNotification(topic: String, message: String, onSuccess: (Core.Model.Message) -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        try {
            val decryptedMessageString = codec.decrypt(Topic(topic), Base64.decode(message))
            val messageHash = sha256(decryptedMessageString.toByteArray())

            if (messageHash !in jsonRpcHistory.getListOfPendingRecords().map { sha256(it.body.toByteArray()) }) {
                val clientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString)
                    ?: return@supervisorScope onFailure(IllegalArgumentException("The decrypted message does not match the Message format: $decryptedMessageString"))
                val notifyMessageJwt = CoreNotifyParams.MessageParams::class.safeCast(serializer.deserialize(clientJsonRpc.method, decryptedMessageString))
                    ?: return@supervisorScope onFailure(IllegalArgumentException("The decrypted message does not match WalletConnect Notify Message format"))
                val messageRequestJwt = extractVerifiedDidJwtClaims<MessageRequestJwtClaim>(notifyMessageJwt.messageAuth).getOrElse {
                    return@supervisorScope onFailure(IllegalArgumentException("The decrypted message does not match WalletConnect Notify Message format"))
                }

                val metadata: AppMetaData = metadataStorageRepository.getByTopicAndType(Topic(topic), AppMetaDataType.PEER)
                    ?: return@supervisorScope onFailure(IllegalArgumentException("The decrypted message does not match WalletConnect Notify Message format"))

                with(messageRequestJwt.serverNotification) {
                    if (!notificationsRepository.doesNotificationsExistsByNotificationId(id)) {

                        val notification = Notification(
                            id = id, topic = topic, sentAt = sentAt, metadata = metadata, notificationMessage = NotificationMessage(title = title, body = body, icon = icon, url = url, type = type)
                        )

                        notificationsRepository.insertOrReplaceNotification(notification)
                        onSuccess(notification.toCore())
                    } else {
                        logger.log("DecryptNotifyMessageUseCase - notification already exists $id")
                    }
                }

            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}