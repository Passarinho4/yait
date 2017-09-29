package xd.yolo.model

import com.avsystem.commons.misc.{NamedEnum, NamedEnumCompanion}
import org.bson.types.ObjectId
import org.joda.time.DateTime
import xd.yolo.model.State.Created

case class UserId(id: String)

case class VotingToken(token: String)

case class Post(id: ObjectId, topicId: ObjectId, authorId: UserId, content: String)

object Post {
  def apply(topicId: ObjectId, authorId: UserId, content: String): Post =
    new Post(new ObjectId(), topicId, authorId, content)
}

sealed abstract class State(override val name: String) extends NamedEnum
object State extends NamedEnumCompanion[State] {

  case object Created extends State("Created")
  case object Open extends State("Open")
  case object Closed extends State("Closed")

  override val values: List[State] = caseObjects
}

case class Topic(id: ObjectId,
                 title: String,
                 state: State,
                 description: String,
                 authorId: UserId,
                 votes: List[VotingToken],
                 commentsAllowed: Boolean,
                 creationDate: DateTime)
object Topic {
  def apply(title: String, description: String, authorId: UserId): Topic = {
    Topic(new ObjectId(), title, Created, description, authorId, List(), commentsAllowed = false, new DateTime())
  }
}
