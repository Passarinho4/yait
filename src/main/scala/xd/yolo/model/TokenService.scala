package xd.yolo.model

import com.avsystem.commons.jiop.JavaInterop._
import com.avsystem.commons.mongo.BsonRef.Creator
import com.avsystem.commons.mongo.sync.GenCodecCollection
import com.avsystem.commons.serialization.GenCodec
import com.mongodb.client.{MongoCollection, MongoDatabase}
import org.joda.time.DateTime

trait TokenService {
  def findByToken(token: String): Option[Token]

  def findActive(): Seq[Token]

  def save(newToken: Token)

  def insertAll(tokens: Seq[Token])

}

class MongoTokenService(db: MongoDatabase) extends TokenService with Creator[Token] with BasicCodecs {

  import com.avsystem.commons.mongo.core.ops.Filtering._

  implicit private val codec: GenCodec[Token] = GenCodec.materializeRecursively[Token]
  private val collection: MongoCollection[Token] = GenCodecCollection.create[Token](db, "token")

  private val idRef = ref(_.id)
  private val tokenRef = ref(_.token)
  private val validRef = ref(_.validUntil)
  private val votesLeftRef = ref(_.votesLeft)

  override def findByToken(token: String): Option[Token] = {
    Option(collection.find(tokenRef equal token).first())
  }

  override def findActive(): Seq[Token] = {
    collection.find(validRef gte new DateTime()).iterator().asScala.toSeq
  }

  override def insertAll(tokens: Seq[Token]): Unit = {
    collection.insertMany(tokens.asJava)
  }

  override def save(newToken: Token): Unit = {
    collection.replaceOne(idRef equal newToken.id, newToken, updateOpt)
  }
}
