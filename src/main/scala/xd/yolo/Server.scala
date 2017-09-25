package xd.yolo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}

object Server extends App {

  private val run: Array[Class[_]] = Array(classOf[MainConfig])
  SpringApplication.run(run, args)


}

@EnableAutoConfiguration
@Configuration
@ComponentScan
class MainConfig {

  val codecRegistry = fromRegistries(fromProviders(classOf[Person]), DEFAULT_CODEC_REGISTRY )

  @Bean
  def mapper: ObjectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

  @Bean def mongoClient: MongoClient = MongoClient()

  @Bean def database: MongoDatabase = mongoClient.getDatabase("mydb").withCodecRegistry(codecRegistry)

  @Bean def collection: MongoCollection[Person] = database.getCollection("test")

}

case class Person(name:String, cos:List[String])