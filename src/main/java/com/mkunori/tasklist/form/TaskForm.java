package com.mkunori.tasklist.form;

/**
 * タスク追加フォームの入力値を受け取るクラスです。
 * 
 * Entity と Form を分けておくと、画面入力とDBの責務を分けやすくなります。
 */
public class TaskForm {

    /**
     * 入力されたタスクのタイトルです。
     */
    private String title;

    /**
     * タイトルを返します。
     * 
     * @return タイトル
     */
    public String getTitle() {
        return title;
    }

    /**
     * タイトルを設定します。
     * 
     * @param title タイトル
     */
    public void setTitle(String title) {
        this.title = title;
    }
}