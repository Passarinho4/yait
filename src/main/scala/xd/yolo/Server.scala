package xd.yolo

import com.avsystem.commons.jiop.JavaInterop._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.mongodb.client.MongoDatabase
import com.mongodb.{MongoClient, MongoCredential, ServerAddress}
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.util.StringUtils
import xd.yolo.model._

object Server extends App {

  private val run: Array[Class[_]] = Array(classOf[MainConfig])
  SpringApplication.run(run, args)

}

@EnableAutoConfiguration
@Configuration
@ComponentScan
class MainConfig {

  @Value("${mongo.username}")
  var username: String = _
  @Value("${mongo.password}")
  var password: String = _
  @Value("${mongo.host}")
  var host: String = _
  @Value("${mongo.authDatabase}")
  var authDatabase: String = _

  @Bean
  def mapper: ObjectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

  @Bean def mongoClient: MongoClient =
    if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
      new MongoClient(JList(new ServerAddress(host)),
        JList(MongoCredential.createCredential(username, authDatabase, password.toCharArray)))
    } else {
      new MongoClient()
    }

  @Bean def database: MongoDatabase = mongoClient.getDatabase("mydb")

  @Bean def topicService: TopicService = new MongoTopicService(database)

  @Bean def postService: PostService = new MongoPostService(database)

  @Bean def tokenService: TokenService = new MongoTokenService(database)
}

