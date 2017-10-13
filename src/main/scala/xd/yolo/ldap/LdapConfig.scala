package xd.yolo.ldap

import com.avsystem.commons.jiop.JavaInterop._
import org.springframework.beans.factory.annotation.{Qualifier, Value}
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.LdapContextSource

@Configuration
class LdapConfig {

  @Value("${ldap.url}") private var ldapUrl: String = _
  @Value("${ldap.usersDn}") private var ldapUsersDn: String = _
  @Value("${ldap.groupsDn}") private var ldapGroupsDn: String = _
  @Value("${ldap.user}") private var ldapUser: String = _
  @Value("${ldap.password}") private var ldapPassword: String = _
  @Value("${ldap.timeout}") private var ldapTimeout: String = _

  @Bean
  @Qualifier
  def usersContextSource(): LdapContextSource = getContextSourceWithBase(ldapUsersDn)

  @Bean
  @Qualifier
  def groupsContextSource(): LdapContextSource = getContextSourceWithBase(ldapGroupsDn)

  @Bean
  def ldapTemplate(): LdapTemplate = new LdapTemplate(usersContextSource())

  def baseEnvironmentProperties(): JMap[String, AnyRef] = {
    val map = new JHashMap[String, AnyRef]()
    map.put("com.sun.jndi.ldap.connect.timeout", ldapTimeout)
    map
  }

  private def getContextSourceWithBase(base: String) = {
    val source = new LdapContextSource
    source.setUrl(ldapUrl)
    source.setBase(base)
    source.setUserDn(ldapUser)
    source.setPassword(ldapPassword)
    source.setBaseEnvironmentProperties(baseEnvironmentProperties())
    source
  }
}
