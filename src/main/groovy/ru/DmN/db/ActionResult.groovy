package ru.DmN.db

enum ActionResult {
    SUCCESS,
    FAIL

    static int parse(ActionResult obj) {
        return values().findIndexOf { it == obj }
    }

    static ActionResult parse(int code) {
        return values()[code]
    }
}
