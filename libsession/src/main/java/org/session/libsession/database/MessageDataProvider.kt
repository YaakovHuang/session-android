package org.session.libsession.database

import org.session.libsession.messaging.sending_receiving.attachments.*
import org.session.libsession.messaging.threads.Address
import org.session.libsession.messaging.utilities.DotNetAPI
import org.session.libsignal.service.api.messages.SignalServiceAttachmentPointer
import org.session.libsignal.service.api.messages.SignalServiceAttachmentStream
import java.io.InputStream

interface MessageDataProvider {

    fun getMessageID(serverID: Long): Long?
    fun deleteMessage(messageID: Long)

    fun getDatabaseAttachment(attachmentId: Long): DatabaseAttachment?

    fun getAttachmentStream(attachmentId: Long): SessionServiceAttachmentStream?
    fun getAttachmentPointer(attachmentId: Long): SessionServiceAttachmentPointer?

    fun getSignalAttachmentStream(attachmentId: Long): SignalServiceAttachmentStream?
    fun getScaledSignalAttachmentStream(attachmentId: Long): SignalServiceAttachmentStream?
    fun getSignalAttachmentPointer(attachmentId: Long): SignalServiceAttachmentPointer?

    fun setAttachmentState(attachmentState: AttachmentState, attachmentId: Long, messageID: Long)

    fun insertAttachment(messageId: Long, attachmentId: Long, stream : InputStream)

    fun isOutgoingMessage(timestamp: Long): Boolean

    fun updateAttachmentAfterUploadSucceeded(attachmentId: Long, attachmentStream: SignalServiceAttachmentStream, attachmentKey: ByteArray, uploadResult: DotNetAPI.UploadResult)
    fun updateAttachmentAfterUploadFailed(attachmentId: Long)

    // Quotes
    fun getMessageForQuote(timestamp: Long, author: Address): Long?
    fun getAttachmentsAndLinkPreviewFor(messageID: Long): List<Attachment>
    fun getMessageBodyFor(messageID: Long): String

    fun getAttachmentIDsFor(messageID: Long): List<Long>
    fun getLinkPreviewAttachmentIDFor(messageID: Long): Long?

}