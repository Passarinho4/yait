package xd.yolo.ldap

import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.util.Try

@Component
class LdapFacade @Autowired()(ldapHandler: LdapHandler, userDataCache: UserDataCache) extends LazyLogging {

  def auth(login: String, passwd: String): Try[Boolean] = {
    ldapHandler.auth(login, passwd)
  }

  def isAdmin(login: String): Boolean = {
    ldapHandler.isAdmin(login)
  }

  def getUserDataById(id: String): Option[UserData] = {
    Option(userDataCache.getUserDataById(id))
  }

  def getUserDataByIds(ids: List[String]): List[UserData] = {
    userDataCache.getUsersDataById(ids)
  }

  def getUserDataByUserGroup(groupName: String): List[UserData] = {
    val userDatas = ldapHandler.getUserDataByGroupName(groupName)
    userDataCache.putAll(userDatas)
    println(s"USERS BY GROUP: $userDatas")
    userDatas
  }

  def getGroups(): List[String] = {
    ldapHandler.getGroupsNames()
  }

  def getUserDataByLogin(login: String): Option[UserData] = {
    userDataCache.getUserDataByLogin(login).orElse(ldapHandler.getUserDataByLogin(login))
  }


}
