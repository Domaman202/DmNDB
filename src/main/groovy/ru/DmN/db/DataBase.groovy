package ru.DmN.db

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import groovy.json.JsonOutput

class DataBase implements Closeable {
    def tables = new ArrayList<Table>()
    ServerSocket socket
    File saveFile

    DataBase(ServerSocket socket, File saveFile, boolean load) {
        this.socket = socket
        this.saveFile = saveFile
        if (load)
            load()
    }

    DataBase(int port, String saveFileName) throws IOException {
        this.socket = new ServerSocket(port)
        this.saveFile = new File(saveFileName)
    }

    void load() throws IOException, JsonSyntaxException {
        def list = new Gson().fromJson saveFile.getText(), ArrayList.class
        for (String str in list)
            tables.add(new Gson().fromJson(str, Table.class))
    }

    void save() throws IOException {
        def list = new ArrayList<String>()
        for (table in tables)
            list.add JsonOutput.toJson(table)
        this.saveFile.write JsonOutput.toJson(list)
    }

    Connection accept() throws IOException {
        return new Connection(socket.accept())
    }

    Connection accept(boolean runInANewThread, Closure closure) throws IOException {
        return new Connection(socket.accept(runInANewThread, closure))
    }

    @Override
    void close() throws IOException {
        this.socket.close()
        save()
    }

    class Connection implements Runnable, Closeable {
        Socket socket
        InputStreamReader is
        OutputStreamWriter os
        Table selectedt
        Column selectedc

        Connection(Socket socket) {
            this.socket = socket
            this.is = new InputStreamReader(socket.inputStream)
            this.os = new OutputStreamWriter(socket.outputStream)
        }

        @Override
        void run() {
            while (this.socket.isConnected()) {
                def act = Action.parse this.is.read()
                switch (act) {
                    case Action.CREATE_TABLE:
                        def name = this.is.readLine()

                        def i = this.is.read()
                        def columns = new Column[i]
                        for (; i != 0; i--) {
                            def cname = this.is.readLine()

                            def j = this.is.read()
                            def attributes = new Column.Attribute[j]
                            for (; j != 0; j--)
                                attributes[j - 1] = Column.Attribute.parse(this.is.read())

                            def type = DataType.parse(this.is.read())

                            columns[i - 1] = new Column(cname, attributes, type.clazz, DataType.parseOf(type, this.is.readLine(), DataBase.this.tables), new ArrayList<>())
                            println "NIGGER!"
                        }

                        DataBase.this.tables.add new Table(name, columns)

                        this.os.write ActionResult.parse(ActionResult.SUCCESS)
                        this.os.flush()
                        break
                    case Action.SELECT_TABLE:
                        def name = this.is.readLine()
                        this.selectedt = DataBase.this.tables.find { it.name == name }

                        this.os.write ActionResult.parse(ActionResult.SUCCESS)
                        this.os.flush()
                        break
                    case Action.DELETE_TABLE:
                        DataBase.this.tables.remove this.selectedt

                        this.os.write ActionResult.parse(ActionResult.SUCCESS)
                        this.os.flush()
                        break
                    case Action.SELECT_COLUMN:
                        def name = this.is.readLine()
                        this.selectedc = this.selectedt.columns.find { it.name == name }

                        this.os.write ActionResult.parse(ActionResult.SUCCESS)
                        this.os.flush()
                        break
                    case Action.SET_VALUE:
                        def i = this.is.read()
                        def type = this.is.read()
                        def value = this.is.readLine()

                        this.selectedc.data[i] = DataType.parseOf DataType.parse(type), value, DataBase.this.tables

                        this.os.write ActionResult.parse(ActionResult.SUCCESS)
                        this.os.flush()
                        break
                    case Action.GET_VALUE:
                        this.os.write this.selectedc.data[this.is.read()].toString() + '\n'

                        this.os.write ActionResult.parse(ActionResult.SUCCESS)
                        this.os.flush()
                        break
                    default:
                        this.os.write ActionResult.parse(ActionResult.FAIL)
                        this.os.write "PARSE ACTION ERROR\n"
                        this.os.flush()
                        break
                }
            }

            close()
        }

        @Override
        void close() throws IOException {
            if (this.socket.isConnected()) {
                this.socket.close()
                this.is.close()
                this.os.close()
            }
        }
    }
}
