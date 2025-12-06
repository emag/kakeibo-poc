package kakeibo.financial_statement

import scalasql._
import scalasql.MySqlDialect._

object Db {

  private final case class BS[T[_]](
      aggId: T[Int],
      asset: T[BigDecimal],
      liability: T[BigDecimal]
  )
  private object BS extends Table[BS]()

  private final case class PL[T[_]](
      aggId: T[Int],
      expense: T[BigDecimal],
      revenue: T[BigDecimal]
  )
  private object PL extends Table[PL]()

  private val ds = new com.mysql.cj.jdbc.MysqlDataSource
  ds.setURL(
    "jdbc:mysql://mysql:3306/financial_statement?verifyServerCertificate=false&useSSL=true"
  )
  ds.setUser("root")
  ds.setPassword("")
  private val client = new DbClient.DataSource(ds)

  def insert(aggId: Int): Unit = {
    val bsQ = BS.insert.columns(
      _.aggId := aggId,
      _.asset := BigDecimal(0),
      _.liability := BigDecimal(0)
    )
    val plQ = PL.insert.columns(
      _.aggId := aggId,
      _.expense := BigDecimal(0),
      _.revenue := BigDecimal(0)
    )
    client.transaction { implicit db =>
      val _ = db.run(bsQ)
      val _ = db.run(plQ): Unit
    }
  }

  def update(
      aggId: Int,
      assetDelta: BigDecimal,
      liabilityDelta: BigDecimal,
      expenseDelta: BigDecimal,
      revenueDelta: BigDecimal
  ): Unit = {
    val currentBsQ = BS.select.filter(_.aggId === aggId).forUpdate.single
    val currentPlQ = PL.select.filter(_.aggId === aggId).forUpdate.single
    def updateBsQ(
        currentAsset: BigDecimal,
        currentLiability: BigDecimal
    ): query.Update[BS[Column], BS[Sc]] = {
      val newAsset = currentAsset + assetDelta
      val newLiability = currentLiability + liabilityDelta
      BS.update(_.aggId === aggId)
        .set(
          _.asset := newAsset,
          _.liability := newLiability
        )
    }
    def updatePlQ(
        currentExpense: BigDecimal,
        currentRevenue: BigDecimal
    ): query.Update[PL[Column], PL[Sc]] = {
      val newExpense = currentExpense + expenseDelta
      val newRevenue = currentRevenue + revenueDelta
      PL.update(_.aggId === aggId)
        .set(
          _.expense := newExpense,
          _.revenue := newRevenue
        )
    }
    client.transaction { implicit db =>
      val currentBs = db.run(currentBsQ)
      val _ = db.run(updateBsQ(currentBs.asset, currentBs.liability))
      val currentPl = db.run(currentPlQ)
      val _ = db.run(updatePlQ(currentPl.expense, currentPl.revenue))
    }
  }
}
