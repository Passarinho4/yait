package xd.yolo.model

import com.avsystem.commons.jiop.JavaInterop._
import com.avsystem.commons.mongo.BsonRef.Creator
import com.avsystem.commons.mongo.sync.GenCodecCollection
import com.avsystem.commons.serialization.GenCodec
import com.mongodb.client.MongoDatabase
import org.bson.types.ObjectId
import xd.yolo.model.State.{Closed, Opened, WontFix}

trait TopicService {


  def getAll: Seq[Topic]

  def getAll(state: State): Seq[Topic]

  def getById(id: ObjectId): Option[Topic]
  def save(topic: Topic): ObjectId

  def openTopic(id: ObjectId): Unit

  def closeTopic(id: ObjectId): Unit

  def wontFixTopic(id: ObjectId): Unit
}

class MongoTopicService(db: MongoDatabase) extends TopicService with Creator[Topic] with BasicCodecs {
  import com.avsystem.commons.mongo.core.ops.Filtering._
  import com.avsystem.commons.mongo.core.ops.Updating._

  implicit val codec: GenCodec[Topic] = GenCodec.materializeRecursively[Topic]
  private val collection = GenCodecCollection.create[Topic](db, "topic")

  private val idRef = ref(_.id)
  private val stateRef = ref(_.state)

  override def getAll: Seq[Topic] = collection.find().iterator().asScala.toSeq

  override def getAll(state: State): Seq[Topic] = {
    collection.find(stateRef equal state)
      .iterator().asScala.toSeq
  }

  override def getById(id: ObjectId): Option[Topic] = {
    Option(collection.find(idRef equal id).first())
  }

  override def save(topic: Topic): ObjectId = {
    collection.replaceOne(idRef equal topic.id, topic, updateOpt)
    topic.id
  }

  override def openTopic(id: ObjectId): Unit = {
    collection.updateOne(idRef equal id, stateRef set Opened)
  }

  override def closeTopic(id: ObjectId): Unit = {
    collection.updateOne(idRef equal id, stateRef set Closed)
  }

  override def wontFixTopic(id: ObjectId): Unit = {
    collection.updateOne(idRef equal id, stateRef set WontFix)
  }
}
