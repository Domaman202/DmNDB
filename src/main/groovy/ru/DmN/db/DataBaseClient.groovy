package ru.DmN.db

class DataBaseClient implements Closeable {
    Socket socket
    InputStreamReader is
    OutputStreamWriter os

    DataBaseClient(Socket socket) throws IOException {
        this.socket = socket
        this.is = new InputStreamReader(this.socket.getInputStream())
        this.os = new OutputStreamWriter(this.socket.getOutputStream())
    }

    DataBaseClient(String ip, int port) throws IOException {
        this.socket = new Socket(ip, port)
        this.is = new InputStreamReader(this.socket.getInputStream())
        this.os = new OutputStreamWriter(this.socket.getOutputStream())
    }

    ActionResult aCreateTable(String name, ColumnData[] cds) {
        this.os.write Action.parse(Action.CREATE_TABLE)
        this.os.write "$name\n"
        this. os.write cds.size()

        for (def cd : cds) {
            this.os.write "$cd.name\n"
            this.os.write cd.attributes.size()
            for (def attr : cd.attributes)
                this.os.write Column.Attribute.parse(attr)
            this.os.write DataType.parse(cd.type)
            this.os.write "$cd.defaultValue\n"
        }

        this.os.flush()

        return ActionResult.parse(this.is.read())
    }

    ActionResult aSelectTable(String name) {
        this.os.write Action.parse(Action.SELECT_TABLE)
        this.os.write "$name\n"
        this.os.flush()

        return ActionResult.parse(this.is.read())
    }

    ActionResult aSelectColumn(String name) {
        this.os.write Action.parse(Action.SELECT_COLUMN)
        this.os.write "$name\n"
        this.os.flush()

        return ActionResult.parse(this.is.read())
    }

    ActionResult aSetValue(int id, DataType type, String value) {
        this.os.write Action.parse(Action.SET_VALUE)
        this.os.write id
        this.os.write DataType.parse(type)
        this.os.write "$value\n"
        this.os.flush()

        return ActionResult.parse(this.is.read())
    }

    def <T> Tuple2<T, ActionResult> aGetValue(int id, DataType type) {
        this.os.write Action.parse(Action.GET_VALUE)
        this.os.write id
        this.os.flush()

        return new Tuple2<>((T) DataType.parseOf(type, this.is.readLine(), null), ActionResult.parse(this.is.read()))
    }

    @Override
    void close() throws IOException {
        if (this.socket.isConnected()) {
            this.socket.close()
            this.is.close()
            this.os.close()
        }
    }

    static class ColumnData {
        String name
        Column.Attribute[] attributes
        DataType type
        String defaultValue

        ColumnData(String name, Column.Attribute[] attributes, DataType type, String defaultValue) {
            this.name = name
            this.attributes = attributes
            this.type = type
            this.defaultValue = defaultValue
        }
    }
}
