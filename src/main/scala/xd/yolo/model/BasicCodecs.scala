package xd.yolo.model

import com.avsystem.commons.mongo.BsonGenCodecs
import com.avsystem.commons.serialization.GenCodec
import com.mongodb.client.model.UpdateOptions
import org.joda.time.DateTime

trait BasicCodecs extends BsonGenCodecs {
  val updateOpt: UpdateOptions = new UpdateOptions().upsert(true)

  implicit def jDateTimeCodec: GenCodec[DateTime] = BasicCodecs.jDateTimeCodec
}

object BasicCodecs {
  implicit val jDateTimeCodec: GenCodec[DateTime] = GenCodec.create[DateTime](
    input => new DateTime(input.readLong()),
    (output, obj) => output.writeLong(obj.getMillis))
}

