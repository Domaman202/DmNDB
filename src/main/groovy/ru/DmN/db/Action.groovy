package ru.DmN.db;

enum Action {
    CREATE_TABLE,
    SELECT_TABLE,
    DELETE_TABLE,
    //
    SELECT_COLUMN,
    //
    SET_VALUE,
    GET_VALUE

    static int parse(Action obj) {
        return values().findIndexOf { it == obj }
    }

    static Action parse(int code) {
        return values()[code]
    }
}
