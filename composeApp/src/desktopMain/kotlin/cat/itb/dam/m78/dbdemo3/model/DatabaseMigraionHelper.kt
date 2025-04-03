package cat.itb.dam.m78.dbdemo3.model

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import cat.itb.dam.m78.dbdemo3.db.Database
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

actual fun createDriver(): SqlDriver {

    val currentDir = System.getProperty("user.dir")
    println("Current directory: $currentDir")

    val userHome = System.getProperty("user.home")
    println("userHome: $userHome")

    //val file = Path(userHome, "myDatabase.db")
    val file = Path(currentDir, "myDatabase.db")
    val driver = JdbcSqliteDriver("jdbc:sqlite:${file.absolutePathString()}")

    println(file.absolutePathString())
    if (Database.Schema == null) {
        Database.Schema.create(driver)
    }
    return driver
}

