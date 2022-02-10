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

                try (var socket = new Socket("localhost", 228)) {
                    def is = new InputStreamReader(socket.inputStream)
                    def os = new OutputStreamWriter(socket.outputStream)

                    os.write Action.parse(Action.SELECT_TABLE)
                    os.write "Users\n"
                    os.flush()

                    if (ActionResult.parse(is.read()) == ActionResult.FAIL)
                        throw new RuntimeException("SELECT TABLE ERROR")
                    else println "[OK] SELECT TABLE"

                    os.write Action.parse(Action.SELECT_COLUMN)
                    os.write "Password\n"
                    os.flush()

                    if (ActionResult.parse(is.read()) == ActionResult.FAIL)
                        throw new RuntimeException("SELECT COLUMN ERROR")
                    else println "[OK] SELECT COLUMN"

                    os.write Action.parse(Action.SET_VALUE)
                    os.write 1
                    os.write DataType.parse(DataType.STRING)
                    os.write "p2356word767\n"
                    os.flush()

                    if (ActionResult.parse(is.read()) == ActionResult.FAIL)
                        throw new RuntimeException("SET VALUE ERROR")
                    else println "[OK] SET VALUE"

                    os.write Action.parse(Action.GET_VALUE)
                    os.write 0
                    os.flush()

                    assert is.readLine() == "123456789"

                    if (ActionResult.parse(is.read()) == ActionResult.FAIL)
                        throw new RuntimeException("GET VALUE ERROR")
                    else println "[OK] GET VALUE"

                    os.write Action.parse(Action.CREATE_TABLE)
                    os.write "Online Table\n"
                    os.write 2

                    os.write "Id\n"
                    os.write 1
                    os.write Column.Attribute.parse(Column.Attribute.AUTO_INCREMENT)
                    os.write DataType.parse(DataType.INT)
                    os.write "0\n"

                    os.write "Name\n"
                    os.write 1
                    os.write Column.Attribute.parse(Column.Attribute.NOT_NULL)
                    os.write DataType.parse(DataType.STRING)
                    os.write "[NO NAME]\n"

                    os.flush()

                    if (ActionResult.parse(is.read()) == ActionResult.FAIL)
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
