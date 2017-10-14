package xd.yolo.ldap

import javax.naming.directory.SearchControls

import com.avsystem.commons.jiop.JavaInterop._
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.ldap.core.support.LdapContextSource
import org.springframework.ldap.core.{AttributesMapper, LdapTemplate}
import org.springframework.ldap.filter.{AndFilter, EqualsFilter, LikeFilter, OrFilter}
import org.springframework.stereotype.Component

import scala.util.Try

@Component
class LdapHandler @Autowired()(template: LdapTemplate,
                               @Qualifier("usersContext") users: LdapContextSource,
                               @Qualifier("groupsContext") groups: LdapContextSource) {

  def auth(user: String, passwd: String): Try[Boolean] = {
    template.setContextSource(users)
    Try {
      val andFilter = new AndFilter()
      andFilter.and(new EqualsFilter("objectclass", "posixAccount"))
      andFilter.and(new EqualsFilter("uid", user))
      template.authenticate("", andFilter.encode(), passwd)
    }
  }

  def getUserData(ids: List[String], field: String): List[UserData] = {
    template.setContextSource(users)
    val andFilter = new AndFilter()
    val orFilter = new OrFilter()

    ids.foreach(id => orFilter.or(new EqualsFilter(field, id)))
    andFilter.and(new EqualsFilter("objectclass", "posixAccount"))
    andFilter.and(orFilter)
    template.search("", andFilter.encode(), SearchControls.ONELEVEL_SCOPE, new UserDataAttributesMapper()).asScala.toList
  }

  def getUserDataByIds(ids: List[String]): List[UserData] = getUserData(ids, "ipaUniqueID")

  def getUserDataById(userId: String): Option[UserData] = {
    getUserDataByIds(List(userId)).headOption
  }

  def getUserDataByLogins(logins: List[String]): List[UserData] = getUserData(logins, "uid")

  def getUserDataByLogin(login: String): Option[UserData] = getUserDataByLogins(List(login)).headOption

  def getUserDataByGroupName(name: String): List[UserData] = {
    template.setContextSource(users)
    val andFilter = new AndFilter()
    andFilter.and(new EqualsFilter("objectclass", "posixAccount"))
    andFilter.and(new LikeFilter("memberof", s"*$name*"))
    template.search("", andFilter.encode(), SearchControls.ONELEVEL_SCOPE, new UserDataAttributesMapper()).asScala.toList
  }

  def getGroups(): List[String] = {
    template.setContextSource(groups)
    val likeFilter = new LikeFilter("objectclass", "*groupOfNames*")
    val mapper: AttributesMapper[String] = attributes => attributes.get("cn").get().asInstanceOf[String]
    template.search("", likeFilter.encode(), mapper).asScala.toList

  }
}
