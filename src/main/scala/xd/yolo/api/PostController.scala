package xd.yolo.api

import org.bson.types
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._
import xd.yolo.api.PostController.{PostRequest, PostResponse}
import xd.yolo.ldap.LdapFacade
import xd.yolo.model.{Post, PostService, UserId}

@RestController
class PostController {

  @Autowired
  var service: PostService = _
  @Autowired
  var ldapFacade: LdapFacade = _

  @GetMapping(Array("/topics/{topicId}/posts"))
  def posts(@PathVariable topicId: String): Seq[PostResponse] = {
    val topicPosts = service.getAllForTopic(new ObjectId(topicId))
    val idToLogin = ldapFacade.getUserDataByIds(topicPosts.map(_.authorId.id).toSet.toList)
      .map(userData => (userData.id, userData.login))
      .toMap
    topicPosts
      .map(post => PostResponse.fromPost(post,
        idToLogin.getOrElse(post.authorId.id, "Unknown")))
  }

  @PostMapping(Array("/topics/{topicId}/posts"))
  def topic(@PathVariable topicId: String, @RequestBody postRequest: PostRequest): String = {
    service.save(Post(new types.ObjectId(topicId), UserId(postRequest.authorId), postRequest.content)).toHexString
  }

}

object PostController {
  case class PostRequest(authorId: String, content: String)

  case class PostResponse(id: String, topicId: String, authorId: String, authorLogin: String,
                          content: String, creationDate: Long)

  object PostResponse {
    def fromPost(post: Post, login: String): PostResponse = {
      PostResponse(post.id.toHexString, post.topicId.toHexString,
        post.authorId.id, login, post.content, post.creationDate.getMillis)
    }
  }

}
