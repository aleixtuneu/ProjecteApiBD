package cat.itb.dam.m78.dbdemo3.model

import app.cash.sqldelight.db.SqlDriver
import cat.itb.dam.m78.dbdemo3.db.Database

expect fun createDriver(): SqlDriver
fun createDatabase(): Database {
    val driver = createDriver()
    return Database(driver)
}
val database by lazy { createDatabase() }