package com.mkunori.tasklist.entity;

/**
 * タスクの優先度を表す列挙型です。
 *
 * enumを使うことで、優先度の値を LOW、MEDIUM、HIGH の3種類に制限できます。
 * 文字列で自由入力にするよりも、誤入力を防ぎやすくなります。
 */
public enum Priority {

    /**
     * 低い優先度です。
     */
    LOW("低"),

    /**
     * 通常の優先度です。
     */
    MEDIUM("中"),

    /**
     * 高い優先度です。
     */
    HIGH("高");

    /**
     * 画面表示用の名前です。
     */
    private final String displayName;

    /**
     * 優先度を作成します。
     *
     * @param displayName 画面表示用の名前
     */
    Priority(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 画面表示用の名前を返します。
     *
     * @return 画面表示用の名前
     */
    public String getDisplayName() {
        return displayName;
    }
}