package xd.yolo.security

import com.avsystem.commons.jiop.JavaInterop._
import org.springframework.security.core.{Authentication, GrantedAuthority}

case class User(username: String, privileges: List[String])

class UserAuthentication(user: User) extends Authentication {

  private var authenticated = true

  override def setAuthenticated(isAuthenticated: Boolean): Unit = authenticated = isAuthenticated

  override def getCredentials = null

  override def getDetails = null

  override def isAuthenticated = authenticated

  override def getAuthorities = user.privileges.map(p => new GrantedAuthority {
    override def getAuthority: String = p
  }).asJava

  override def getPrincipal = user

  override def getName = user.username
}
