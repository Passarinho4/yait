package xd.yolo.ldap

case class UserData(id: String,
                    login: String,
                    name: Option[String],
                    surname: Option[String],
                    mail: Option[String])
