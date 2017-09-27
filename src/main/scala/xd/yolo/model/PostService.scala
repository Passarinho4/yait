package xd.yolo.model

import com.mongodb.client.MongoCollection


trait PostService {

}

class MongoPostService(collection: MongoCollection[Post]) extends PostService {

}
