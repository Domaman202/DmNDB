package ru.DmN.db

interface DBProvider {
    ActionResult aCreateTable(String name, ColumnData[] cds);

    ActionResult aDeleteTable();

    ActionResult aSelectTable(String name);

    ActionResult aSelectColumn(String name);

    ActionResult aSetValue(int id, DataType type, String value);

    def <T> Tuple2<T, ActionResult> aGetValue(int id, DataType type)

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