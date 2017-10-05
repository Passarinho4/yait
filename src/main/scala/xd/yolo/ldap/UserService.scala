package xd.yolo.ldap

import com.avsystem.commons.misc.{NamedEnum, NamedEnumCompanion}
import org.springframework.stereotype.Service
import xd.yolo.api.LoginController
import xd.yolo.ldap.UserService.UserType
import xd.yolo.ldap.UserService.UserType.{Admin, Member}

import scala.util.Try

@Service
class UserService {
  def loginTry(loginPassword: LoginController.LoginPassword): Try[UserType] = {
    if (loginPassword.login == "Szymek") {
      Try(Admin)
    } else {
      Try(Member)
    }
  }

}

object UserService {

  sealed abstract class UserType(val name: String) extends NamedEnum

  object UserType extends NamedEnumCompanion[UserType] {

    case object Member extends UserType("Member")

    case object Admin extends UserType("Admin")

    override val values: List[UserType] = caseObjects
  }

}
