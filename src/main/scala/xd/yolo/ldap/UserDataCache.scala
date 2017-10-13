package xd.yolo.ldap

import java.util.concurrent.TimeUnit

import com.avsystem.commons.jiop.JavaInterop._
import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.mutable.ListBuffer

@Component
class UserDataCache @Autowired()(handler: LdapHandler) {


  private val cache: LoadingCache[String, UserData] = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build[String, UserData](
    new CacheLoader[String, UserData] {
      override def load(key: String): UserData = {
        handler.getUserDataById(key) match {
          case Some(user) => user
          case None => throw new IllegalStateException("User not found in LDAP.")
        }
      }
    }
  )

  def getUserDataById(id: String): UserData = {
    cache.get(id)
  }

  def getUsersDataById(ids: List[String]): List[UserData] = {
    val usersFromCache = new ListBuffer[UserData]
    val idsToObtainFromLdap = new ListBuffer[String]
    val cacheMap = cache.asMap().asScala
    ids.foreach(id =>
      if (cacheMap.contains(id)) {
        usersFromCache += cacheMap(id)
      } else {
        idsToObtainFromLdap += id
      }
    )
    handler.getUserDataByIds(idsToObtainFromLdap.toList) ::: usersFromCache.toList
  }

  def getUserDataByLogin(login: String): Option[UserData] = {
    cache.asMap().asScala.find { case (id, userData) => userData.login == login }.map(_._2)
  }

  def putAll(userDatas: List[UserData]): Unit = {
    cache.putAll(userDatas.map(data => (data.id, data)).toMap.asJava)
  }

  def refresh(): Unit = {
    val ids = cache.asMap().asScala.keySet.toList
    if (ids.nonEmpty) {
      cache.invalidateAll()
      cache.putAll(handler.getUserDataByIds(ids).map(userData => (userData.id, userData)).toMap.asJava)
    }
  }
}
