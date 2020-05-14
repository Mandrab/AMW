package asl.action

import asl.action.util.GoogleMailSender
import com.google.api.services.gmail.Gmail
import com.google.common.io.Resources
import jason.asSemantics.DefaultInternalAction
import jason.asSemantics.TransitionSystem
import jason.asSemantics.Unifier
import jason.asSyntax.ListTerm
import jason.asSyntax.NumberTerm
import jason.asSyntax.Term
import java.io.File
import java.util.*
import java.util.function.BiFunction
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class send_feedback : DefaultInternalAction() {
	override fun execute(ts: TransitionSystem?, un: Unifier?, args: Array<Term>): Any {
		val mailTo = args[0].toString()
		val mailSubject: String
		val mailMsg: String

		TODO("https://blog.mailtrap.io/send-emails-with-gmail-api/")

		/*when ((args[1] as NumberTerm).solve().toInt()) {
			200 -> {
				mailSubject = ORDER_READY
				mailMsg = ORDER_INFO.apply(
					args[2].toString(),
					(args[3] as ListTerm).map { obj: Term -> obj.toString() }
				)
			}
			202 -> {
				mailSubject = ORDER_CONFIRM
				mailMsg = ORDER_INFO.apply(
					args[2].toString(),
					(args[3] as ListTerm).map { obj: Term -> obj.toString() }
				)
			}
			404 -> {
				mailSubject = ORDER_FAIL
				mailMsg = ITEM_NOT_FOUND
			}
			409 -> {
				mailSubject = ORDER_FAIL
				mailMsg = ITEM_RESERVED
			}
			else -> { mailSubject = ""; mailMsg = "" }
		}
		val reader = File(Resources.getResource("amw_mail_login").file).readLines()
		val properties = Properties()

		// Setup mail server
		properties.setProperty("mail.host", "smtp.gmail.com")
		properties.setProperty("mail.smtp.port", "587")
		properties.setProperty("mail.smtp.auth", "true")
		properties.setProperty("mail.smtp.starttls.enable", "true")
		//properties.setProperty( "mail.smtp.timeout", "5000" );

		val mailFrom = reader[0]
		val pass = reader[1]
		val session = Session.getInstance(properties, object : Authenticator() {
				override fun getPasswordAuthentication() = PasswordAuthentication(mailFrom, pass)
			})
		//session.setDebug( true );
		try {
			val message = MimeMessage(session)						// Create a default MimeMessage object
			message.setFrom(InternetAddress(mailFrom))				// SetFrom: header field of the header
			message.addRecipient(Message.RecipientType.TO, InternetAddress(mailTo))	// SetTo: header field of the header
			message.subject = mailSubject							// SetSubject: header field
			message.setText(mailMsg)								// Now set the actual message
			Transport.send(message)									// Send message
		} catch (mex: MessagingException) {
			mex.printStackTrace()
			return false
		}
		return true*/
	}

	companion object {
		private const val ORDER_CONFIRM = "Order confirm"
		private const val ORDER_READY = "Order ready"
		private const val ORDER_FAIL = "Order fail"
		private const val ITEM_NOT_FOUND = "At least an item of your order has not been found"
		private const val ITEM_RESERVED = "Due to another buy, at least one item of your order is not present in " +
				"sufficient quantity"
		private val ORDER_INFO = BiFunction { orderId: String, items: List<String>? ->
				"""Your order with id: $orderId including the following items: ${java.lang.String.join("\n",
					items)} has been confirmed"""
			}
	}
}