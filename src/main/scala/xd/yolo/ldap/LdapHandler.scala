package xd.yolo.ldap

import javax.naming.directory.SearchControls

import com.avsystem.commons.jiop.JavaInterop._
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.ldap.core.support.LdapContextSource
import org.springframework.ldap.core.{AttributesMapper, ContextMapper, DirContextAdapter, LdapTemplate}
import org.springframework.ldap.filter._
import org.springframework.stereotype.Component

import scala.util.Try

@Component
class LdapHandler @Autowired()(template: LdapTemplate,
                               @Qualifier("usersContext") users: LdapContextSource,
                               @Qualifier("groupsContext") groups: LdapContextSource) {
  @Value("${ldap.usersFilter}")
  private var usersFilter: String = _


  @Value("${ldap.groupsFilter}")
  private var groupsFilter: String = _

  @Value("${ldap.adminsGroupDn}")
  private var adminsGroupDn: String = _

  def isAdmin(login: String): Boolean = {
    template.setContextSource(users)
    val andFilter = new AndFilter()

    andFilter.and(new HardcodedFilter(usersFilter))
    andFilter.and(new EqualsFilter("uid", login))
    andFilter.and(new LikeFilter("memberof", s"$adminsGroupDn"))
    !template.search("", andFilter.encode(), SearchControls.ONELEVEL_SCOPE, new UserDataAttributesMapper())
      .isEmpty
  }

  def auth(user: String, passwd: String): Try[Boolean] = {
    template.setContextSource(users)
    Try {
      val andFilter = new AndFilter()
      andFilter.and(new HardcodedFilter(usersFilter))
      andFilter.and(new EqualsFilter("uid", user))
      template.authenticate("", andFilter.encode(), passwd)
    }
  }

  def getUserData(ids: List[String], field: String): List[UserData] = {
    template.setContextSource(users)
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
    template.setContextSource(users)
    val andFilter = new AndFilter()
    andFilter.and(new HardcodedFilter(usersFilter))
    andFilter.and(new LikeFilter("memberof", s"${getGroupsNameToDn()(name)}"))
    template.search("", andFilter.encode(), SearchControls.ONELEVEL_SCOPE, new UserDataAttributesMapper()).asScala.toList
  }

  private def getGroupsNameToDn(): Map[String, String] = {
    template.setContextSource(groups)
    val andFilter = new AndFilter()
    andFilter.and(new LikeFilter("objectclass", "groupOfNames"))
    andFilter.and(new HardcodedFilter(groupsFilter))

    val mapper: ContextMapper[(String, String)] = context => {
      val ctx = context.asInstanceOf[DirContextAdapter]
      val cn = ctx.getStringAttribute("cn")
      val dn = ctx.getDn
      println(s"CN: $cn")
      println(s"DN: $dn")
      (cn, dn.toString)
    }
    template.search("", andFilter.encode(), mapper).asScala.toMap
  }

  def getGroupsNames(): List[String] = {
    template.setContextSource(groups)
    val andFilter = new AndFilter()
    andFilter.and(new LikeFilter("objectclass", "groupOfNames"))
    andFilter.and(new HardcodedFilter(groupsFilter))

    val mapper: AttributesMapper[String] = attributes => attributes.get("cn").get().asInstanceOf[String]
    template.search("", andFilter.encode(), mapper).asScala.toList

  }
}
