package xd.yolo.api

import org.bson.types
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._
import xd.yolo.api.PostController.{PostRequest, PostResponse}
import xd.yolo.model.{Post, PostService, UserId}

@RestController
class PostController {

  @Autowired
  var service: PostService = _

  @GetMapping(Array("/topics/{topicId}/posts"))
  def posts(@PathVariable topicId: String): Seq[PostResponse] = {
    service.getAllForTopic(new ObjectId(topicId)).map(PostResponse.fromPost)
  }

  @PostMapping(Array("/topics/{topicId}/posts"))
  def topic(@PathVariable topicId: String, @RequestBody postRequest: PostRequest): String = {
    service.save(Post(new types.ObjectId(topicId), UserId(postRequest.authorId), postRequest.content)).toHexString
  }

}

object PostController {
  case class PostRequest(authorId: String, content: String)

  case class PostResponse(id: String, topicId: String, authorId: String, content: String, creationDate: Long)

  object PostResponse {
    def fromPost(post: Post): PostResponse = {
      PostResponse(post.id.toHexString, post.topicId.toHexString,
        post.authorId.id, post.content, post.creationDate.getMillis)
    }
  }

}
