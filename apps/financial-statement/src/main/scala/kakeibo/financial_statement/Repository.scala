package kakeibo.financial_statement

import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Await
import scala.concurrent.duration._

trait Repository {
  def insertBS(bs: BS): Unit
  def insertPL(pl: PL): Unit
}

class SlickRepository extends Repository {
  
  private val db = Database.forConfig("db_ctx")
  
  def insertBS(bs: BS): Unit = {
    val action = Tables.bs += bs
    val future = db.run(action)
    Await.result(future, 10.seconds): Unit
  }
  
  def insertPL(pl: PL): Unit = {
    val action = Tables.pl += pl
    val future = db.run(action)
    Await.result(future, 10.seconds): Unit
  }
}