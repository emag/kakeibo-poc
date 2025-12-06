package kakeibo.financial_statement

import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

import javax.sql.DataSource

object QuillContext {
  val dataSource: ZLayer[Any, Throwable, javax.sql.DataSource] =
    Quill.DataSource.fromPrefix("db_ctx")

  val mysql: ZLayer[DataSource, Nothing, Quill.Mysql[SnakeCase]] =
    Quill.Mysql.fromNamingStrategy(SnakeCase)

  val mysqlFromConfig: ZLayer[Any, Throwable, Quill.Mysql[SnakeCase]] =
    dataSource >>> mysql
}
