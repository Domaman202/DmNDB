package ru.DmN.db

enum DataType {
    CHAR(Character.class),
    INT(Integer.class),
    LONG(Long.class),
    DOUBLE(Double.class),
    STRING(String.class),
    ARRAY(Object[].class),
    REFERENCE(Table.class)

    Class<?> clazz

    DataType(Class<?> clazz) {
        this.clazz = clazz
    }

    static int parse(DataType obj) {
        return values().findIndexOf { it == obj }
    }

    static DataType parse(int code) {
        return values()[code]
    }

    static Object parseOf(DataType type, String str, ArrayList<Table> tables) {
        switch(type) {
            case CHAR: return str.toCharacter()
            case INT: return str.toInteger()
            case LONG: return str.toLong()
            case DOUBLE: return str.toDouble()
            case STRING: return str
            case ARRAY: return new Object[str.toInteger()]
            case REFERENCE: return tables.find { it.name == str }
        }
    }
}