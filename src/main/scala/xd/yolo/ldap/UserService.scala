package xd.yolo.ldap

import com.avsystem.commons.misc.{NamedEnum, NamedEnumCompanion}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import xd.yolo.api.LoginController
import xd.yolo.ldap.UserService.{CantAuthenticateException, UserType}

import scala.util.Try

@Service
class UserService @Autowired()(ldapFacade: LdapFacade) {
  @Value("${yait.admin}")
  val adminLogin: String = null

  def loginTry(loginPassword: LoginController.LoginPassword): Try[UserType] = {
    for (result <- ldapFacade.auth(loginPassword.login, loginPassword.password)) yield {
      if (result) {
        getUserTypeForUser(loginPassword.login)
      } else {
        throw new CantAuthenticateException(s"Can't authenticate user ${loginPassword.login}")
      }
    }
  }

  def getUserTypeForUser(login: String): UserType = {
    if (login == adminLogin) {
      UserType.Admin
    } else {
      UserType.Member
    }
  }

  def getGroups() = ldapFacade.getGroups()
}

object UserService {

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  class CantAuthenticateException(message: String) extends RuntimeException(message)

  sealed abstract class UserType(val name: String) extends NamedEnum

  object UserType extends NamedEnumCompanion[UserType] {

    case object Member extends UserType("Member")

    case object Admin extends UserType("Admin")

    override val values: List[UserType] = caseObjects
  }

}
