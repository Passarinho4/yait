package xd.yolo.api

import com.typesafe.scalalogging.LazyLogging
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation._
import xd.yolo.api.TopicController.{TopicRequest, TopicResponse, VoteRequest}
import xd.yolo.model.State.{Closed, Opened, WontFix}
import xd.yolo.model._

import scala.language.postfixOps


@RestController
class TopicController extends LazyLogging {

  @Autowired var service: TopicService = _

  @Autowired var tokenService: TokenService = _

  @GetMapping(Array("topics"))
  def topics(): Seq[TopicResponse] = {
    service.getAll.map(TopicResponse.fromTopic)
  }

  @GetMapping(Array("topics/{id}"))
  def topic(@PathVariable id: String): TopicResponse = {
    service.getById(new ObjectId(id)).map(TopicResponse.fromTopic).orNull
  }

  @GetMapping(Array("topics/active"))
  def activeTopics(): Seq[TopicResponse] = {
    service.getAll(Opened).map(TopicResponse.fromTopic)
  }

  @GetMapping(Array("topics/opened"))
  def openedTopics(): Seq[TopicResponse] = {
    service.getAll(Opened).map(TopicResponse.fromTopic)
  }

  @GetMapping(Array("topics/wontfix"))
  def wontfixTopics(): Seq[TopicResponse] = {
    service.getAll(WontFix).map(TopicResponse.fromTopic)
  }

  @GetMapping(Array("topics/closed"))
  def closedTopics(): Seq[TopicResponse] = {
    service.getAll(Closed).map(TopicResponse.fromTopic)
  }


  @PostMapping(path = Array("topics"), produces = Array(MediaType.TEXT_PLAIN_VALUE))
  def topic(@RequestBody topicRequest: TopicRequest): String = {
    service.save(Topic(topicRequest.title, topicRequest.description, UserId(topicRequest.authorId))).toHexString
  }

  @PostMapping(Array("topics/{id}"))
  def voteForTopic(@RequestBody voteRequest: VoteRequest, @PathVariable id: String) = {
    for {
      topic <- service.getById(new ObjectId(id))
      token <- tokenService.findByToken(voteRequest.token) //Should handle invalid tokens and returns error not 200.
    } {
      val (newToken, newTopic) = token.voteFor(topic)
      service.save(newTopic)
      tokenService.save(newToken)
    }
  }

  @PostMapping(Array("topics/{id}/open"))
  def openTopic(@PathVariable id: String): Unit = {
    service.openTopic(new ObjectId(id))
  }

  @PostMapping(Array("topics/{id}/wontfix"))
  def wontFixTopic(@PathVariable id: String): Unit = {
    service.wontFixTopic(new ObjectId(id))
  }

  @PostMapping(Array("topics/{id}/close"))
  def closeTopic(@PathVariable id: String): Unit = {
    service.closeTopic(new ObjectId(id))
  }

}

object TopicController {

  case class TopicRequest(title: String, description: String, authorId: String)
  case class VoteRequest(token: String)

  case class TopicResponse(id: String, title: String, description: String, authorId: String, votes: List[String])

  object TopicResponse {
    def fromTopic(topic: Topic): TopicResponse = {
      TopicResponse(topic.id.toHexString, topic.title, topic.description, topic.authorId.id, topic.votes.map(_.toHexString))
    }
  }

}
