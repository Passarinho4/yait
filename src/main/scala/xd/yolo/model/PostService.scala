package xd.yolo.model

import com.avsystem.commons.jiop.JavaInterop._
import com.avsystem.commons.mongo.BsonRef.Creator
import com.avsystem.commons.mongo.sync.GenCodecCollection
import com.avsystem.commons.serialization.GenCodec
import com.mongodb.client.MongoDatabase
import org.bson.types.ObjectId

trait PostService {

  def getAllForTopic(topicId: ObjectId): Seq[Post]
  def getById(id: ObjectId): Post
  def save(post: Post): ObjectId

}

class MongoPostService(db: MongoDatabase) extends PostService with Creator[Post] with BasicCodecs {
  import com.avsystem.commons.mongo.core.ops.Filtering._

  implicit val codec: GenCodec[Post] = GenCodec.materializeRecursively[Post]
  private val collection = GenCodecCollection.create[Post](db, "post")

  private val topicIdRef = ref(_.topicId)
  private val idRef = ref(_.id)

  override def getAllForTopic(topicId: ObjectId): Seq[Post] = {
    collection.find(topicIdRef equal topicId).iterator().asScala.toSeq
  }

  override def getById(id: ObjectId): Post = {
    collection.find(idRef equal id).first()
  }

  override def save(post: Post): ObjectId = {
    collection.insertOne(post)
    post.id
  }
}