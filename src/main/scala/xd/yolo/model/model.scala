package xd.yolo.model

import java.util.UUID

import com.avsystem.commons.misc.{NamedEnum, NamedEnumCompanion}
import org.bson.types.ObjectId
import org.joda.time.DateTime
import xd.yolo.model.State.Opened

case class UserId(id: String)


case class Post(id: ObjectId, topicId: ObjectId, authorId: UserId, content: String, creationDate: DateTime)

object Post {
  def apply(topicId: ObjectId, authorId: UserId, content: String): Post =
    new Post(new ObjectId(), topicId, authorId, content, new DateTime())
}

sealed abstract class State(override val name: String) extends NamedEnum
object State extends NamedEnumCompanion[State] {

  case object Opened extends State("Opened")

  case object WorkInProgress extends State("WorkInProgress")

  case object WontFix extends State("WontFix")
  case object Closed extends State("Closed")

  override val values: List[State] = caseObjects
}

case class Topic(id: ObjectId,
                 title: String,
                 state: State,
                 description: String,
                 authorId: UserId,
                 votes: List[ObjectId],
                 commentsAllowed: Boolean,
                 creationDate: DateTime)
object Topic {
  def apply(title: String, description: String, authorId: UserId): Topic = {
    Topic(new ObjectId(), title, Opened, description, authorId, List(), commentsAllowed = false, new DateTime())
  }
}

case class Token(id: ObjectId, token: String, userId: Option[UserId],
                 creationDate: DateTime, validUntil: DateTime, votesLeft: Int) {

  def voteFor(topic: Topic): (Token, Topic) = {
    if (!canVote()) throw new IllegalStateException(s"Token $token is outdated or doesn't have enough points.")
    if (topic.state != Opened) throw new IllegalArgumentException(s"$Topic {topic.id} isn't open for voting.")

    val newTopic = topic.copy(votes = id :: topic.votes)
    val newToken = copy(votesLeft = votesLeft - 1)
    (newToken, newTopic)
  }

  def canVote(): Boolean = {
    validUntil.isAfterNow && votesLeft > 0
  }

}

object Token {

  def apply(validUntil: DateTime, votes: Int, userId: UserId): Token =
    new Token(new ObjectId(), UUID.randomUUID().toString, Some(userId), new DateTime(), validUntil, votes)

  def generateForUsers(validUntil: DateTime, votes: Int, users: Seq[(UserId, String)]): Map[String, Token] =
    users.map(e => (e._2, Token(validUntil, votes, e._1))).toMap

  def apply(validUntil: DateTime, votes: Int): Token =
    new Token(new ObjectId(), UUID.randomUUID().toString, None, new DateTime(), validUntil, votes)

  def generateForMails(validUntil: DateTime, votes: Int, mails: Seq[String]): Map[String, Token] =
    mails.map(mail => (mail, Token(validUntil, votes))).toMap
}