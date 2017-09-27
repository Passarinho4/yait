package xd.yolo.model

import com.avsystem.commons.jiop.JavaInterop._
import com.avsystem.commons.mongo.sync.MongoOps
import com.avsystem.commons.mongo.{BsonCodec, Doc, DocumentCodec, Filter}
import com.mongodb.client.{MongoCollection, MongoDatabase}
import org.bson.types.ObjectId
import org.joda.time.DateTime


trait TopicService {
  def getAll: Seq[Topic]
  def getById(id: ObjectId): Topic
  def save(topic: Topic): Unit
}

class MongoTopicService(collection: MongoCollection[Topic]) extends TopicService {

  import MongoTopicService._

  override def getAll: Seq[Topic] = collection.find().iterator().asScala.toSeq

  override def getById(id: ObjectId): Topic = {
    collection.find(Filter.equal(idKey, id)).first()
  }

  override def save(topic: Topic): Unit = {
    collection.insertOne(topic)
  }
}
object MongoTopicService extends MongoOps {
  def getCollection(database: MongoDatabase): MongoCollection[Topic] = {
    dbOps(database).getCollection[Topic]("topic", MongoTopicService.codec.bsonCodec)
  }

  private val idKey = BsonCodec.objectId.key("_id")
  private val titleKey = BsonCodec.string.key("title")
  private val stateKey = BsonCodec.string.key("state")
  private val descriptionKey = BsonCodec.string.key("description")
  private val authorIdKey = BsonCodec.string.key("authorId")
  private val votesKey = BsonCodec.string.collection[List].key("votes")
  private val commentsAllowedKey = BsonCodec.boolean.key("commentsAllowed")
  private val creationDateKey = BsonCodec.int64.key("creationDate")

  val codec: DocumentCodec[Topic] = new DocumentCodec[Topic] {
    override def toDocument(t: Topic): Doc = Doc()
      .put(idKey, t.id)
      .put(titleKey, t.title)
      .put(stateKey, t.state)
      .put(descriptionKey, t.description)
      .put(authorIdKey, t.authorId.id)
      .put(votesKey, t.votes.map(_.token))
      .put(commentsAllowedKey, t.commentsAllowed)
      .put(creationDateKey, t.creationDate.getMillis)

    override def fromDocument(doc: Doc): Topic = Topic(
      doc.require(idKey),
      doc.require(titleKey),
      doc.require(stateKey),
      doc.require(descriptionKey),
      UserId(doc.require(authorIdKey)),
      doc.require(votesKey).map(VotingToken),
      doc.require(commentsAllowedKey),
      new DateTime(doc.require(creationDateKey)))
  }


}