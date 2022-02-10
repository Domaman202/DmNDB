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

    static DataType parse(String clazz) {
        switch(clazz) {
            case "java.lang.Character": return CHAR
            case "java.lang.Integer": return INT
            case "java.lang.Long": return LONG
            case "java.lang.Double": return DOUBLE
            case "java.lang.String": return STRING
            case "ru.DmN.db.Table": return REFERENCE
            default: throw new RuntimeException("") // TODO:
        }
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