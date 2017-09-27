package xd.yolo.model

import org.bson.types.ObjectId
import org.joda.time.DateTime

case class UserId(id: String)

case class VotingToken(token: String)

case class Post(topicId: ObjectId, authorId: UserId, content: String)

case class Topic(id: ObjectId,
                 title: String,
                 state: String,
                 description: String,
                 authorId: UserId,
                 votes: List[VotingToken],
                 commentsAllowed: Boolean,
                 creationDate: DateTime)
object Topic {
  def apply(title: String, description: String, authorId: UserId): Topic = {
    Topic(new ObjectId(), title, "Created", description, authorId, List(), commentsAllowed = false, new DateTime())
  }
}
