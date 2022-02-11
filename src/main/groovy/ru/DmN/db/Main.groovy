package ru.DmN.db

import java.util.concurrent.CompletableFuture

class Main {
    static void main(String[] args) {
        {
            def table = new Table("Users",
                    new Column<Integer>("Id", Column.Attribute.AUTO_INCREMENT as Column.Attribute[], Integer.class, 0, new ArrayList<>()),
                    new Column<String>("Name", Column.Attribute.NOT_NULL as Column.Attribute[], String.class, null, new ArrayList<>()),
                    new Column<String>("Password", Column.Attribute.NOT_NULL as Column.Attribute[], String.class, null, new ArrayList<>())
            )

            table.insert("Ivan", "123456789", "Vasya", "abc12345", "NN228337", "#21#223@\$")

            table.remove(1)

            try (def db = new DataBase(228, "test.dmn.db")) {
                db.tables.add table

                CompletableFuture.runAsync {
                    db.accept().run()
                }

                try (var connection = new DataBaseClient("localhost", 228)) {
                    def is = connection.is
                    def os = connection.os

                    if (connection.aSelectTable("Users") == ActionResult.FAIL)
                        throw new RuntimeException("SELECT TABLE ERROR")
                    else println "[OK] SELECT TABLE"

                    if (connection.aSelectColumn("Password") == ActionResult.FAIL)
                        throw new RuntimeException("SELECT COLUMN ERROR")
                    else println "[OK] SELECT COLUMN"

                    if (connection.aSetValue(1, DataType.STRING, "p2356word767") == ActionResult.FAIL)
                        throw new RuntimeException("SET VALUE ERROR")
                    else println "[OK] SET VALUE"

                    def result = connection.<String>aGetValue(0, DataType.STRING)
                    assert result.first == "123456789"
                    if (result.second == ActionResult.FAIL)
                        throw new RuntimeException("GET VALUE ERROR")
                    else println "[OK] GET VALUE"

                    if (connection.aCreateTable("Online Table", new DataBaseClient.ColumnData[]{["Name", new Column.Attribute[]{Column.Attribute.NOT_NULL}, DataType.STRING, "[NO NAME]"], ["Id", new Column.Attribute[]{Column.Attribute.AUTO_INCREMENT}, DataType.INT, "0"]}) == ActionResult.FAIL)
                        throw new RuntimeException("CREATE TABLE ERROR")
                    else println "[OK] CREATE TABLE"
                }
            }
        }

        {
            try (def db = new DataBase(228, "test.dmn.db")) {
                db.load()
                db.tables.forEach { it.print(System.out) }
            }
        }
    }
}
