package com.mkunori.tasklist.service;

/**
 * タスク一覧の絞り込み条件を表す列挙型です。
 *
 * 画面から送られてきた表示条件を、このenumで扱います。
 * 文字列をそのまま使うよりも、使える値を限定できるため安全です。
 */
public enum TaskFilterType {

    /**
     * すべてのタスクを表示します。
     */
    ALL("すべて"),

    /**
     * 未完了のタスクだけを表示します。
     */
    ACTIVE("未完了"),

    /**
     * 完了済みのタスクだけを表示します。
     */
    DONE("完了済み");

    /**
     * 画面に表示する名前です。
     */
    private final String displayName;

    /**
     * 絞り込み条件を作成します。
     *
     * @param displayName 画面に表示する名前
     */
    TaskFilterType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 画面に表示する名前を返します。
     *
     * @return 画面表示用の名前
     */
    public String getDisplayName() {
        return displayName;
    }
}