package xd.yolo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import xd.yolo.model._

object Server extends App {

  private val run: Array[Class[_]] = Array(classOf[MainConfig])
  SpringApplication.run(run, args)

}

@EnableAutoConfiguration
@Configuration
@ComponentScan
class MainConfig {

  @Bean
  def mapper: ObjectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

  @Bean def mongoClient: MongoClient = new MongoClient()

  @Bean def database: MongoDatabase = mongoClient.getDatabase("mydb")

  @Bean def topicService: TopicService = new MongoTopicService(database)

  @Bean def postService: PostService = new MongoPostService(database)

}

