package xd.yolo.ldap

import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UserDataCacheUpdater @Autowired()(cache: UserDataCache) extends LazyLogging {

  @Scheduled(fixedRate = 60000)
  def updateCache(): Unit = {
    cache.refresh()
    logger.info("Cache successfully refreshed.")
  }

}
