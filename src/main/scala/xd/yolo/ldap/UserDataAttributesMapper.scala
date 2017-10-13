package xd.yolo.ldap

import javax.naming.directory.{Attribute, Attributes}

import org.springframework.ldap.core.AttributesMapper

class UserDataAttributesMapper extends AttributesMapper[UserData] {
  override def mapFromAttributes(attributes: Attributes): UserData = {
    UserData(attributes.get("ipaUniqueID").get().asInstanceOf[String],
      attributes.get("uid").get().asInstanceOf[String],
      safetyHandleAttribute(attributes.get("givenName")),
      safetyHandleAttribute(attributes.get("sn")),
      safetyHandleAttribute(attributes.get("mail")))
  }

  private def safetyHandleAttribute(attr: Attribute): Option[String] = {
    Option(attr).map(_.get()).collect { case s: String => s }
  }
}
