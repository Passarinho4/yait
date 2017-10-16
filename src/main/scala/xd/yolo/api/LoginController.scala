package xd.yolo.api

import com.avsystem.commons.jiop.JavaInterop._
import io.jsonwebtoken.{JwtBuilder, Jwts, SignatureAlgorithm}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.security.crypto.codec.Base64
import org.springframework.util.Base64Utils
import org.springframework.web.bind.annotation.{GetMapping, RequestHeader, RestController}
import xd.yolo.ldap.UserService
import xd.yolo.ldap.UserService.UserType

import scala.util.Try

@RestController
class LoginController {

  import LoginController._

  @Autowired
  val service: UserService = null
  @Value("${jwt.security}")
  private val secret: String = null

  @GetMapping(Array("/login/credentials"))
  def login(@RequestHeader("Authorization") header: String): Token = {
    (for {loginPassword <- decodeUserFromCredentials(header)
          userType <- service.loginTry(loginPassword)
          token <- generate(loginPassword.login, userType, secret)
    } yield token).get
  }

  @GetMapping(Array("/healthCheck"))
  def healthCheck(): String = {
    "OK"
  }

  @GetMapping(Array("/groups"))
  def getGroups(): GroupsResponse = {
    GroupsResponse(service.getGroups())
  }
}

object LoginController {

  case class Token(token: String)

  case class LoginPassword(login: String, password: String)

  case class GroupsResponse(groups: List[String])

  def decodeUserFromCredentials(header: String): Try[LoginPassword] = {
    for {
      decodedCredentials <- Try(new String(Base64Utils.decodeFromString(header)))
      slittedCredentials = decodedCredentials.split(":")
      login <- Try(slittedCredentials(0))
      password <- Try(slittedCredentials(1))
    } yield LoginPassword(login, password)
  }

  def generate(login: String, userType: UserType, secret: String): Try[Token] = {
    Try {
      val claims = new JHashMap[String, Object]()
      claims.put("username", login)
      claims.put("privileges", JList(userType.name))

      val jwtBuilder: JwtBuilder = Jwts.builder
        .setSubject(login)
        .setClaims(claims)
        .setHeaderParam("typ", "JWT")
        .signWith(SignatureAlgorithm.HS256, Base64.encode(secret.getBytes))

      Token(jwtBuilder.compact)
    }
  }
}
