package asl.action.util

import com.google.api.client.util.Base64
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object GoogleMailSender {
	/**
	 * Create a MimeMessage using the parameters provided.
	 * Web reference: https://developers.google.com/gmail/api/guides/sending
	 *
	 * @param to email address of the receiver
	 * @param from email address of the sender, the mailbox account
	 * @param subject subject of the email
	 * @param bodyText body text of the email
	 * @return the MimeMessage to be used to send email
	 * @throws MessagingException
	 */
	@Throws(MessagingException::class)
	fun createEmail(to: String, from: String, subject: String, bodyText: String): MimeMessage {
		val props = Properties()
		val session = Session.getDefaultInstance(props, null)
		val email = MimeMessage(session)
		email.setFrom(InternetAddress(from))
		email.addRecipient(javax.mail.Message.RecipientType.TO, InternetAddress(to))
		email.subject = subject
		email.setText(bodyText)
		return email
	}

	/**
	 * Create a message from an email.
	 * Web reference: https://developers.google.com/gmail/api/guides/sending
	 *
	 * @param emailContent Email to be set to raw of message
	 * @return a message containing a base64url encoded email
	 * @throws IOException
	 * @throws MessagingException
	 */
	@Throws(MessagingException::class, IOException::class)
	fun createMessageWithEmail(emailContent: MimeMessage): Message {
		val buffer = ByteArrayOutputStream()
		emailContent.writeTo(buffer)
		val bytes: ByteArray = buffer.toByteArray()
		val encodedEmail: String = Base64.encodeBase64URLSafeString(bytes)
		val message = Message()
		message.setRaw(encodedEmail)
		return message
	}

	/**
	 * Send an email from the user's mailbox to its recipient.
	 * Web reference: https://developers.google.com/gmail/api/guides/sending
	 *
	 * @param service Authorized Gmail API instance.
	 * @param userId User's email address. The special value "me"
	 * can be used to indicate the authenticated user.
	 * @param emailContent Email to be sent.
	 * @return The sent message
	 * @throws MessagingException
	 * @throws IOException
	 */
	@Throws(MessagingException::class, IOException::class)
	fun sendMessage(service: Gmail, userId: String, emailContent: MimeMessage): Message {
		var message: Message = createMessageWithEmail(emailContent)
		message = service.users().messages().send(userId, message).execute()
		println("Message id: " + message.id)
		println(message.toPrettyString())
		return message
	}
}