package com.mkunori.tasklist.service;

/**
 * タスク一覧の並び替え条件を表す列挙型です。
 *
 * 画面から送られてきた並び替え条件を、このenumで扱います。
 * 文字列をそのまま使うよりも、使える値を限定できるため安全です。
 */
public enum TaskSortType {

    /**
     * 登録順で表示します。
     *
     * idの昇順で並べることで、先に登録されたタスクを先に表示します。
     */
    CREATED("登録順"),

    /**
     * 期限が近い順で表示します。
     *
     * 期限が未設定のタスクは最後に表示します。
     */
    DUE_DATE("期限が近い順"),

    /**
     * 優先度が高い順で表示します。
     *
     * HIGH、MEDIUM、LOW の順に表示します。
     */
    PRIORITY("優先度が高い順");

    /**
     * 画面に表示する名前です。
     */
    private final String displayName;

    /**
     * 並び替え条件を作成します。
     *
     * @param displayName 画面に表示する名前
     */
    TaskSortType(String displayName) {
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