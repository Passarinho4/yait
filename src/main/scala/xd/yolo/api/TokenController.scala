package xd.yolo.api

import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.mail.{MailSender, SimpleMailMessage}
import org.springframework.web.bind.annotation._
import xd.yolo.api.TokenController.{GenerateForGroupsRequest, GenerateForMailsRequest, GenerateForUsersRequest, TokenResponse}
import xd.yolo.ldap.LdapFacade
import xd.yolo.model.{Token, TokenService, UserId}

@RestController
class TokenController {

  @Value("${yait.address}")
  private var address: String = _
  @Autowired
  private var service: TokenService = _
  @Autowired
  private var mailSender: MailSender = _
  @Autowired
  private var ldapFacade: LdapFacade = _

  def createMail(mail: String, token: Token): SimpleMailMessage = {
    val message = new SimpleMailMessage()
    message.setTo(mail)
    message.setSubject("YAIT Token")
    message.setText(s"Now you can vote here: $address/token/${token.token}/topicList")
    message
  }

  @PostMapping(Array("/tokens/users"))
  def generateTokens(@RequestBody request: GenerateForUsersRequest): Unit = {
    val validUntil = new DateTime(request.validUntil)
    val ids = ldapFacade.getUserDataByIds(request.userIds)
      .filter(_.mail.isDefined)
      .map(e => (UserId(e.id), e.mail.get))
    val tokensMap = Token.generateForUsers(validUntil, request.votes, ids)
    service.insertAll(tokensMap.values.toSeq)
    tokensMap.foreach(token => {
      val message = createMail(token._1, token._2)
      mailSender.send(message)
    })
  }

  @PostMapping(Array("/tokens/groups"))
  def generateTokens(@RequestBody request: GenerateForGroupsRequest): Unit = {
    val validUntil = new DateTime(request.validUntil)
    val ids = request.groups.flatMap(g => ldapFacade.getUserDataByUserGroup(g))
      .filter(_.mail.isDefined)
      .map(e => (UserId(e.id), e.mail.get))
    if (ids.nonEmpty) {
      val tokensMap = Token.generateForUsers(validUntil, request.votes, ids)
      ids.foreach(pair => println(s"USER: ${pair._1.id}"))
      service.insertAll(tokensMap.values.toSeq)
      tokensMap.foreach(token => {
        mailSender.send(createMail(token._1, token._2))
      })
    }
  }

  @PostMapping(Array("/tokens/mails"))
  def generateTokens(@RequestBody request: GenerateForMailsRequest): Unit = {
    val validUntil = new DateTime(request.validUntil)
    val tokensMap = Token.generateForMails(validUntil, request.votes, request.mails)
    service.insertAll(tokensMap.values.toSeq)
    tokensMap.foreach(token => {
      val message = createMail(token._1, token._2)
      mailSender.send(message)
    })
  }

  @GetMapping(Array("/tokens/{token}"))
  def token(@PathVariable token: String): TokenResponse = {
    service.findByToken(token).map(TokenResponse.fromToken).orNull
  }


}

object TokenController {

  case class TokenResponse(id: String, token: String, userId: Option[UserId],
                           creationDate: DateTime, validUntil: DateTime, votesLeft: Int)

  object TokenResponse {
    def fromToken(token: Token): TokenResponse = {
      TokenResponse(token.id.toHexString, token.token, token.userId, token.creationDate, token.validUntil, token.votesLeft)
    }
  }

  case class GenerateForUsersRequest(userIds: List[String], validUntil: Long, votes: Int)

  case class GenerateForMailsRequest(mails: List[String], validUntil: Long, votes: Int)

  case class GenerateForGroupsRequest(groups: List[String], validUntil: Long, votes: Int)

}