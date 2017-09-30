package xd.yolo.api

import com.typesafe.scalalogging.LazyLogging
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation._
import xd.yolo.api.TopicController.{TopicRequest, VoteRequest}
import xd.yolo.model._

import scala.language.postfixOps


@RestController
class TopicController extends LazyLogging {

  @Autowired var service: TopicService = _

  @Autowired var tokenService: TokenService = _

  @GetMapping(Array("topics"))
  def topics() = {
    service.getAll
  }

  @GetMapping(Array("topics/{id}"))
  def topic(@PathVariable id: String): Topic = {
    service.getById(new ObjectId(id)).orNull
  }

  @GetMapping(Array("topics/active"))
  def activeTopics(): Seq[Topic] = {
    service.getAllActive
  }

  @PostMapping(path = Array("topics"), produces = Array(MediaType.APPLICATION_JSON_VALUE))
  def topic(@RequestBody topicRequest: TopicRequest) = {
    service.save(Topic(topicRequest.title, topicRequest.description, UserId(topicRequest.authorId)))
  }

  @PostMapping(Array("topics/{id}"))
  def voteForTopic(@RequestBody voteRequest: VoteRequest, @PathVariable id: String) = {
    for {
      topic <- service.getById(new ObjectId(id))
      token <- tokenService.findByToken(voteRequest.token)
    } {
      val (newToken, newTopic) = token.voteFor(topic)
      service.save(newTopic)
      tokenService.save(newToken)
    }
  }
}

object TopicController {

  case class TopicRequest(title: String, description: String, authorId: String)

  case class VoteRequest(token: String)
}
