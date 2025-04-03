package cat.itb.dam.m78.dbdemo3.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class MyTableQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAll(mapper: (id: Long, text: String) -> T): Query<T> =
      Query(931_686_568, arrayOf("myTable"), driver, "myTable.sq", "selectAll",
      "SELECT id, text FROM myTable") { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!
    )
  }

  public fun selectAll(): Query<MyTable> = selectAll { id, text ->
    MyTable(
      id,
      text
    )
  }

  public fun insert(text: String) {
    driver.execute(354_734_070, """INSERT INTO myTable(text) VALUES (?)""", 1) {
          bindString(0, text)
        }
    notifyQueries(354_734_070) { emit ->
      emit("myTable")
    }
  }

  public fun delete(id: Long) {
    driver.execute(203_068_136, """DELETE FROM myTable WHERE id = ?""", 1) {
          bindLong(0, id)
        }
    notifyQueries(203_068_136) { emit ->
      emit("myTable")
    }
  }
}
