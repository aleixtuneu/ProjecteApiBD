package cat.itb.dam.m78.dbdemo3.db.composeApp

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import cat.itb.dam.m78.dbdemo3.db.Database
import cat.itb.dam.m78.dbdemo3.db.MyTableQueries
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<Database>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = DatabaseImpl.Schema

internal fun KClass<Database>.newInstance(driver: SqlDriver): Database = DatabaseImpl(driver)

private class DatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), Database {
  override val myTableQueries: MyTableQueries = MyTableQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE myTable (
          |  id INTEGER PRIMARY KEY NOT NULL,
          |  text TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, "CREATE INDEX myTable_full_name ON myTable(text)", 0)
      driver.execute(null, "DELETE FROM myTable", 0)
      driver.execute(null, "INSERT INTO myTable(id,text) VALUES (1, \"bla\")", 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
