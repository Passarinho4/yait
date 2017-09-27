package xd.yolo.api

import org.bson.types
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._
import xd.yolo.api.PostController.PostRequest
import xd.yolo.model.{Post, PostService, UserId}

@RestController
class PostController {

  @Autowired
  var service: PostService = _

  @GetMapping(Array("/topics/{topicId}/posts"))
  def posts(@PathVariable topicId: String): Seq[Post] = {
    service.getAllForTopic(new ObjectId(topicId))
  }

  @PostMapping(Array("/topics/{topicId}/posts"))
  def topic(@PathVariable topicId: String, @RequestBody postRequest: PostRequest): Unit = {
    service.save(Post(new types.ObjectId(topicId), UserId(postRequest.authorId), postRequest.content))
  }

}

object PostController {
  private case class PostRequest(authorId: String, content: String)
}
