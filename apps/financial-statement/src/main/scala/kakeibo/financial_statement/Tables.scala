package kakeibo.financial_statement

import slick.jdbc.MySQLProfile.api._

class BSTable(tag: Tag) extends Table[BS](tag, "bs") {
  def aggId = column[Int]("agg_id", O.PrimaryKey)
  def asset = column[BigDecimal]("asset")
  def liability = column[BigDecimal]("liability")
  
  def * = (aggId, asset, liability) <> (BS.apply.tupled, BS.unapply)
}

class PLTable(tag: Tag) extends Table[PL](tag, "pl") {
  def aggId = column[Int]("agg_id", O.PrimaryKey)
  def expense = column[BigDecimal]("expense")
  def revenue = column[BigDecimal]("revenue")
  
  def * = (aggId, expense, revenue) <> (PL.apply.tupled, PL.unapply)
}

object Tables {
  val bs = TableQuery[BSTable]
  val pl = TableQuery[PLTable]
}