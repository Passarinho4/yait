package xd.yolo.ldap

import com.avsystem.commons.misc.{NamedEnum, NamedEnumCompanion}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Service
import xd.yolo.api.LoginController
import xd.yolo.ldap.UserService.UserType

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
        throw new IllegalArgumentException(s"Can't authenticate user ${loginPassword.login}")
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

  sealed abstract class UserType(val name: String) extends NamedEnum

  object UserType extends NamedEnumCompanion[UserType] {

    case object Member extends UserType("Member")

    case object Admin extends UserType("Admin")

    override val values: List[UserType] = caseObjects
  }

}
