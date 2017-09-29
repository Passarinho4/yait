package xd.yolo.model

import com.avsystem.commons.jiop.JavaInterop._
import com.avsystem.commons.mongo.BsonRef.Creator
import com.avsystem.commons.mongo._
import com.avsystem.commons.mongo.core.ops.Filtering
import com.avsystem.commons.mongo.sync.GenCodecCollection
import com.avsystem.commons.serialization.GenCodec
import com.mongodb.client.MongoDatabase
import org.bson.codecs.DocumentCodec
import org.bson.types.ObjectId
import org.joda.time.DateTime
import xd.yolo.model.State.{Created, Open}


trait TopicService {
  def getAll: Seq[Topic]
  def getAllActive: Seq[Topic]
  def getById(id: ObjectId): Topic
  def save(topic: Topic): Unit
}

class MongoTopicService(db: MongoDatabase) extends TopicService with Creator[Topic] {
  import com.avsystem.commons.mongo.core.ops.Filtering._
  import BsonGenCodecs._

  implicit val jDateTimeCodec: GenCodec[DateTime] = GenCodec.create[DateTime](
    input => new DateTime(input.readLong()),
    (output, obj) => output.writeLong(obj.getMillis))

  implicit val codec: GenCodec[Topic] = GenCodec.materializeRecursively[Topic]
  private val collection = GenCodecCollection.create[Topic](db, "topic")

  private val idRef = ref(_.id)
  private val stateRef = ref(_.state)

  override def getAll: Seq[Topic] = collection.find().iterator().asScala.toSeq

  override def getAllActive: Seq[Topic] = {
    collection.find(stateRef in (Created, Open))
      .iterator().asScala.toSeq
  }

  override def getById(id: ObjectId): Topic = {
    collection.find(idRef equal id).first()
  }

  override def save(topic: Topic): Unit = {
    collection.insertOne(topic)
  }
}
