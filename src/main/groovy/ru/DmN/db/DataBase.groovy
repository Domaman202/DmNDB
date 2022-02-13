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

    class DBProviderImpl implements DBProvider {
        Table selectedt
        Column selectedc

        @Override
        ActionResult aCreateTable(String name, ColumnData[] cds) {
            def columns = new Column[cds.size()]
            for (int i = 0; i < cds.size(); i++)
                cds[i].with {
                    columns[i] = new Column(it.name, it.attributes, it.type.clazz, DataType.parseOf(it.type, it.defaultValue, DataBase.this.tables), new ArrayList<>())
                }
            def table = new Table(name, columns)
            def res = DataBase.this.tables.contains(table) ? ActionResult.FAIL : ActionResult.SUCCESS
            DataBase.this.tables.add table
            return res
        }

        @Override
        ActionResult aDeleteTable() {
            return DataBase.this.tables.remove(selectedt) ? ActionResult.SUCCESS : ActionResult.FAIL
        }

        @Override
        ActionResult aSelectTable(String name) {
            this.selectedt = DataBase.this.tables.find { it.name == name }
            return this.selectedt == null ? ActionResult.FAIL : ActionResult.SUCCESS
        }

        @Override
        ActionResult aSelectColumn(String name) {
            this.selectedc = this.selectedt.columns.find { it.name == name }
            return this.selectedc == null ? ActionResult.FAIL : ActionResult.SUCCESS
        }

        @Override
        ActionResult aSetValue(int id, DataType type, String value) {
            if (this.selectedc.data.size() > id) {
                this.selectedc.data[id] = DataType.parseOf(type, value, DataBase.this.tables)
                return ActionResult.SUCCESS
            }
            return ActionResult.FAIL
        }

        @Override
        <T> Tuple2<T, ActionResult> aGetValue(int id, DataType type) {
            if (this.selectedc.data.size() > id)
                return new Tuple2<>(this.selectedc.data[id], ActionResult.SUCCESS)
            return new Tuple2<>(null, ActionResult.FAIL)
        }
    }

    class Connection extends DBProviderImpl implements Runnable, Closeable {
        Socket socket
        InputStreamReader is
        OutputStreamWriter os

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
                        }

                        DataBase.this.tables.add new Table(name, columns)

                        this.os.write ActionResult.parse(ActionResult.SUCCESS)
                        this.os.flush()
                        break
                    case Action.DELETE_TABLE:
                        this.os.write ActionResult.parse(this.aDeleteTable())
                        this.os.flush()
                        break
                    case Action.SELECT_TABLE:
                        this.os.write ActionResult.parse(this.aSelectTable(this.is.readLine()))
                        this.os.flush()
                        break
                    case Action.SELECT_COLUMN:
                        this.os.write ActionResult.parse(this.aSelectColumn(this.is.readLine()))
                        this.os.flush()
                        break
                    case Action.SET_VALUE:
                        this.os.write ActionResult.parse(this.aSetValue(this.is.read(), DataType.parse(this.is.read()), this.is.readLine()))
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
