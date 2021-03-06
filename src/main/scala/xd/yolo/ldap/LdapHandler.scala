package xd.yolo.ldap

import javax.naming.directory.SearchControls

import com.avsystem.commons.jiop.JavaInterop._
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.ldap.core._
import org.springframework.ldap.core.support.{AbstractContextMapper, LdapContextSource}
import org.springframework.ldap.filter._
import org.springframework.stereotype.Component

import scala.util.Try

@Component
class LdapHandler @Autowired()(@Qualifier("usersContext") users: LdapContextSource,
                               @Qualifier("groupsContext") groups: LdapContextSource) {
  @Value("${ldap.usersFilter}")
  private var usersFilter: String = _


  @Value("${ldap.groupsFilter}")
  private var groupsFilter: String = _

  @Value("${ldap.adminsGroupDn}")
  private var adminsGroupDn: String = _

  def isAdmin(login: String): Boolean = {
    val template = new LdapTemplate(users)
    val andFilter = new AndFilter()

    andFilter.and(new HardcodedFilter(usersFilter))
    andFilter.and(new EqualsFilter("uid", login))
    andFilter.and(new LikeFilter("memberof", s"$adminsGroupDn"))
    val userDatas = template.search("", andFilter.encode(), SearchControls.ONELEVEL_SCOPE, new UserDataAttributesMapper())
    !userDatas.isEmpty
  }

  def auth(user: String, passwd: String): Try[Boolean] = {
    val template = new LdapTemplate(users)
    Try {
      val andFilter = new AndFilter()
      andFilter.and(new HardcodedFilter(usersFilter))
      andFilter.and(new EqualsFilter("uid", user))
      template.authenticate("", andFilter.encode(), passwd)
    }
  }

  def getUserData(ids: List[String], field: String): List[UserData] = {
    val template = new LdapTemplate(users)
    val andFilter = new AndFilter()
    val orFilter = new OrFilter()

    ids.foreach(id => orFilter.or(new EqualsFilter(field, id)))
    andFilter.and(new HardcodedFilter(usersFilter))
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
    val template = new LdapTemplate(users)
    val andFilter = new AndFilter()

    andFilter.and(new HardcodedFilter(usersFilter))
    val groupDn: String = getGroupsNameToDn()(name)
    andFilter.and(new LikeFilter("memberof", s"$groupDn"))
    val userDatas = template.search("", andFilter.encode(), SearchControls.ONELEVEL_SCOPE, new UserDataAttributesMapper())
    userDatas.asScala.toList
  }

  private def getGroupsNameToDn(): Map[String, String] = {
    val template = new LdapTemplate(groups)
    val andFilter = new AndFilter()
    andFilter.and(new LikeFilter("objectclass", "groupOfNames"))
    andFilter.and(new HardcodedFilter(groupsFilter))
    val mapper = new AbstractContextMapper[(String, String)] {
      override def doMapFromContext(ctx: DirContextOperations) = {
        val cn = ctx.getStringAttribute("cn")
        val dn = ctx.getNameInNamespace
        (cn, dn)
      }
    }
    template.search("", andFilter.encode(), mapper).asScala.toMap
  }

  def getGroupsNames(): List[String] = {
    val template = new LdapTemplate(groups)
    val andFilter = new AndFilter()
    andFilter.and(new LikeFilter("objectclass", "groupOfNames"))
    andFilter.and(new HardcodedFilter(groupsFilter))

    val mapper: AttributesMapper[String] = attributes => attributes.get("cn").get().asInstanceOf[String]
    template.search("", andFilter.encode(), mapper).asScala.toList

  }
}
