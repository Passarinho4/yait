package xd.yolo.security

import javax.servlet.http.HttpServletRequest

import com.avsystem.commons.jiop.JavaInterop._
import io.jsonwebtoken.Jwts
import org.springframework.security.crypto.codec.Base64

class TokenAuthenticationService(private val secret: String) {

  import TokenAuthenticationService._

  def getAuthentication(httpRequest: HttpServletRequest) =
    Option(httpRequest.getHeader(TokenHeader))
      .map(parseUser)
      .map(new UserAuthentication(_))
      .orNull

  def parseUser(token: String): User = {
    val body = Jwts.parser()
      .setSigningKey(Base64.encode(secret.getBytes))
      .parseClaimsJws(token)
      .getBody

    val username = body.get("username", classOf[String])
    val privileges = body.get("privileges", classOf[java.util.List[String]]).asScala.toList
    User(username, privileges)
  }

}

object TokenAuthenticationService {
  val TokenHeader = "X-AUTH-TOKEN"
}