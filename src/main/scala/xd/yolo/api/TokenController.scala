package xd.yolo.api

import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._
import xd.yolo.api.TokenController.{GenerateForMailsRequest, GenerateForUsersRequest}
import xd.yolo.model.{Token, TokenService, UserId}

@RestController
class TokenController {

  @Autowired
  private var service: TokenService = _

  @PostMapping(Array("/tokens/users"))
  def generateTokens(@RequestBody request: GenerateForUsersRequest): Unit = {
    val validUntil = new DateTime(request.validUntil)
    val ids = request.userIds.map(UserId)
    val tokens = Token.generateForUsers(validUntil, request.votes, ids)
    service.insertAll(tokens)
    //TODO Send tokens
  }

  @PostMapping(Array("/tokens/mails"))
  def generateTokens(@RequestBody request: GenerateForMailsRequest): Unit = {
    val validUntil = new DateTime(request.validUntil)
    val tokens = Token.generateForMails(validUntil, request.votes, request.mails)
    service.insertAll(tokens)
    //TODO Send tokens
  }

  @GetMapping(Array("/tokens/{token}"))
  def token(@PathVariable token: String): Token = {
    service.findByToken(token).orNull
  }


}

object TokenController {

  case class GenerateForUsersRequest(userIds: List[String], validUntil: Long, votes: Int)

  case class GenerateForMailsRequest(mails: List[String], validUntil: Long, votes: Int)

}