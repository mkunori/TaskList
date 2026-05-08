package com.mkunori.tasklist.form;

import java.time.LocalDate;

import com.mkunori.tasklist.entity.Priority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * タスク更新フォームの入力値を受け取るクラスです。
 *
 * 編集画面から送信された値を受け取ります。
 * 新規追加用のTaskFormとは分けて、更新に必要なidも持たせています。
 */
public class TaskUpdateForm {

    /**
     * 更新対象のタスクIDです。
     */
    @NotNull(message = "更新対象のタスクIDがありません")
    private Long id;

    /**
     * 更新後のタスクタイトルです。
     */
    @NotBlank(message = "タイトルを入力してください")
    @Size(max = 255, message = "タイトルは255文字以内で入力してください")
    private String title;

    /**
     * 更新後の期限日です。
     *
     * 期限なしも許可するため、未入力の場合は null になります。
     */
    private LocalDate dueDate;

    /**
     * 更新後の優先度です。
     */
    @NotNull(message = "優先度を選択してください")
    private Priority priority = Priority.MEDIUM;

    /**
     * タスクIDを返します。
     *
     * @return タスクID
     */
    public Long getId() {
        return id;
    }

    /**
     * タスクIDを設定します。
     *
     * @param id タスクID
     */
    public void setId(Long id) {
        this.id = id;
    }

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

    /**
     * 期限日を返します。
     *
     * @return 期限日。未入力の場合は null
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * 期限日を設定します。
     * 
     * Springがフォームの日付入力を LocalDate としてセットします。
     *
     * @param dueDate 期限日。未入力の場合は null
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * 優先度を返します。
     *
     * @return 優先度
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * 優先度を設定します。
     *
     * @param priority 優先度
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}