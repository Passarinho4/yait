package xd.yolo.mail

import java.util.Properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.mail.MailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
class MailConfig {

  @Value("{mail.host}")
  private var host: String = _
  @Value("{mail.password}")
  private var password: String = _
  @Value("{mail.encoding}")
  private var encoding: String = _
  @Value("{mail.username}")
  private var username: String = _
  @Value("{mail.protocol}")
  private var protocol: String = _
  @Value("{mail.port}")
  private var port: String = _


  @Bean
  def mailSender: MailSender = {
    val sender = new JavaMailSenderImpl()
    val properties = properties
    sender.setHost(host)
    sender.setPassword(password)
    sender.setDefaultEncoding(encoding)
    sender.setUsername(username)
    sender.setProtocol(protocol)
    sender.setPort(port.toInt)
    val props = new Properties()
    props.setProperty("mail.smtps.auth", "true")
    props.setProperty("mail.smtp.ssl.enable", "true")
    props.setProperty("mail.transport.protocol", "true")
    props.setProperty("mail.smtp.starttls.enable", "true")
    sender.setJavaMailProperties(props)
    sender
  }

}
