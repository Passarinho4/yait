package xd.yolo.model

import com.avsystem.commons.mongo.{BsonCodec, Doc, DocumentCodec, Filter}
import com.avsystem.commons.mongo.sync.MongoOps
import com.mongodb.client.{MongoCollection, MongoDatabase}
import org.bson.types.ObjectId
import com.avsystem.commons.jiop.JavaInterop._

trait PostService {

  def getAllForTopic(topicId: ObjectId): Seq[Post]
  def getById(id: ObjectId): Post
  def save(post: Post): Unit

}

class MongoPostService(collection: MongoCollection[Post]) extends PostService {

  import MongoPostService._

  override def getAllForTopic(topicId: ObjectId): Seq[Post] = {
    collection.find(Filter.equal(topicIdKey, topicId)).iterator().asScala.toSeq
  }

  override def getById(id: ObjectId): Post = {
    collection.find(Filter.equal(idKey, id)).first()
  }

  override def save(post: Post): Unit = {
    collection.insertOne(post)
  }
}
object MongoPostService extends MongoOps {

  private val idKey = BsonCodec.objectId.key("_id")
  private val topicIdKey = BsonCodec.objectId.key("topicId")
  private val authorKey = BsonCodec.string.key("authorId")
  private val contentKey = BsonCodec.string.key("content")

  def getCollection(db: MongoDatabase): MongoCollection[Post] = {
    dbOps(db).getCollection[Post]("post", codec.bsonCodec)
  }

  private val codec = new DocumentCodec[Post] {
    override def toDocument(t: Post): Doc = Doc()
      .put(idKey, t.id)
      .put(topicIdKey, t.topicId)
      .put(authorKey, t.authorId.id)
      .put(contentKey, t.content)

    override def fromDocument(doc: Doc): Post = Post(
      doc.require(idKey),
      doc.require(topicIdKey),
      UserId(doc.require(authorKey)),
      doc.require(contentKey))
  }
}