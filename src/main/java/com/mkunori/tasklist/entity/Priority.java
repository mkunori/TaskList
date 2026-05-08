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
    LOW("低", 1),

    /**
     * 通常の優先度です。
     */
    MEDIUM("中", 2),

    /**
     * 高い優先度です。
     */
    HIGH("高", 3);

    /**
     * 画面表示用の名前です。
     */
    private final String displayName;

    /**
     * 並び替え用の値です。
     *
     * 数字が大きいほど、優先度が高いことを表します。
     */
    private final int sortOrder;

    /**
     * 優先度を作成します。
     *
     * @param displayName 画面表示用の名前
     * @param sortOrder 並び替え用の値
     */
    Priority(String displayName, int sortOrder) {
        this.displayName = displayName;
        this.sortOrder = sortOrder;
    }

    /**
     * 画面表示用の名前を返します。
     *
     * @return 画面表示用の名前
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 並び替え用の値を返します。
     *
     * @return 並び替え用の値
     */
    public int getSortOrder() {
        return sortOrder;
    }
}