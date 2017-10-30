package xd.yolo.ldap

import com.avsystem.commons.misc.{NamedEnum, NamedEnumCompanion}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Service
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

  def getUserId(login: String): Try[String] = Try {
    ldapFacade.getUserDataByLogin(login).map(_.id).get
  }

  def getUserTypeForUser(login: String): UserType = {
    if (ldapFacade.isAdmin(login)) {
      UserType.Admin
    } else {
      UserType.Member
    }
  }

  def getGroups() = ldapFacade.getGroups()
}

object UserService {

  class CantAuthenticateException(message: String) extends RuntimeException(message)

  sealed abstract class UserType(val name: String) extends NamedEnum

  object UserType extends NamedEnumCompanion[UserType] {

    case object Member extends UserType("Member")

    case object Admin extends UserType("Admin")

    override val values: List[UserType] = caseObjects
  }

}
