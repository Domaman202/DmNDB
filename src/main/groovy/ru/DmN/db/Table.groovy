package ru.DmN.db

class Table {
    String name
    Column[] columns

    Table(String name, Column[] columns) {
        this.name = name
        this.columns = columns
    }

    ArrayList get(int id) {
        def list = new ArrayList(columns.size())
        for (column in columns)
            list.add column.data.last()
        return list
    }

    void insert(def ... values) {
        def i = 0
        for (value in values) {
            def column = columns[i]
            if (column.attributes.contains Column.Attribute.AUTO_INCREMENT) {
                column.insert column.data.size() > 0 ? column.data.get(column.data.size() - 1) + 1 : 0
                column = columns[++i]
            }

            if (Class.forName(column.type).isInstance value)
                column.insert value
            i++

            if (i >= columns.size())
                i = 0
        }

        if (i > 0 && i < columns.size()) {
            for (; i < columns.size(); i++) {
                if (columns[i].dv == null && columns[i].attributes.contains(Column.Attribute.NOT_NULL))
                    throw new TableInsertException()
                columns[i].insert()
            }
        }
    }

    void remove() {
        for (column in columns)
            column.remove()
    }

    void remove(int id) {
        for (column in columns) {
            column.data.remove id
            if (column.attributes.contains(Column.Attribute.AUTO_INCREMENT))
                for (i in id..column.data.size() - 1)
                    column.data[i] = column.data[i] - 1
        }
    }

    void replace(Column column) {
        for (def i = 0; i < columns.length; i++)
            if (columns[i].name == column.name) {
                columns[i] = column
                break
            }
    }

    void print(PrintStream out) {
        out.println "$name:"
        columns.each {
            out.print "${it.name}: "
            it.each { out.println it.data }
        }
    }

    static class TableInsertException extends Exception {}
}