package cat.itb.dam.m78.dbdemo3.db

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import cat.itb.dam.m78.dbdemo3.db.composeApp.newInstance
import cat.itb.dam.m78.dbdemo3.db.composeApp.schema
import kotlin.Unit

public interface Database : Transacter {
  public val myTableQueries: MyTableQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = Database::class.schema

    public operator fun invoke(driver: SqlDriver): Database = Database::class.newInstance(driver)
  }
}
