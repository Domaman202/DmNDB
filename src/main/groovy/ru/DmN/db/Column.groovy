package ru.DmN.db

class Column <T> {
    String name
    String type
    Attribute[] attributes
    T dv
    ArrayList<T> data

    Column(String name, Attribute[] attributes, Class<T> type, T dv, ArrayList<T> data) {
        this.name = name
        this.attributes = attributes
        this.type = type.name
        this.dv = dv
        this.data = data
    }

    void insert() {
        this.data.add this.dv
    }

    void insert(T value) {
        this.data.add value
    }

    void remove() {
        this.data.removeLast()
    }

    void remove(T value) {
        this.data.remove value
    }

    static enum Attribute {
        AUTO_INCREMENT(0),
        NOT_NULL(1)

        int code

        Attribute(int code) {
            this.code = code
        }

        static Attribute parse(int code) {
            return values().find { it.code == code }
        }
    }
}
