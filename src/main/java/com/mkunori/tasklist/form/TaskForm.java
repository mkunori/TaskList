package com.mkunori.tasklist.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * タスク追加フォームの入力値を受け取るクラスです。
 *
 * このクラスは、画面から送信された値を一時的に受け取ります。
 * Entityとは分けておくことで、画面入力のチェックを管理しやすくなります。
 */
public class TaskForm {

    /**
     * 入力されたタスクのタイトルです。
     *
     * 空文字や空白だけの入力は許可しません。
     * また、長すぎるタイトルを防ぐため、最大文字数も指定しています。
     */
    @NotBlank(message = "タイトルを入力してください")
    @Size(max = 255, message = "タイトルは255文字以内で入力してください")
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
     * Springがフォームの入力値をこのメソッドを使ってセットします。
     *
     * @param title タイトル
     */
    public void setTitle(String title) {
        this.title = title;
    }
}