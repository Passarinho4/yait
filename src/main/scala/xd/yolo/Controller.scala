package xd.yolo

import org.mongodb.scala.{Completed, MongoCollection, Observer}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{GetMapping, RestController}

@RestController
class Controller {

  @Autowired
  var mongoCollection: MongoCollection[Person] = _

  @GetMapping(Array("/hello"))
  def helloWorld() = {
    "Hello world!"
  }

  @GetMapping(Array("/caseClassTest"))
  def caseClassTest() = {
    Test("Lol", 12, List("L1", "L2", "L3"))
    mongoCollection.insertOne(Person("Szymek", List("d1", "d2"))).subscribe(new Observer[Completed] {
      override def onError(e: Throwable): Unit = {}

      override def onComplete(): Unit = {
        println("dupa")
      }

      override def onNext(result: Completed): Unit = {}
    })
  }

}


case class Test(s: String, l: Long, list: List[String])