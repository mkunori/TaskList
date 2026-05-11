package com.mkunori.tasklist.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * タスクを表すエンティティクラスです。
 *
 * このクラスは tasks テーブルの1行に対応します。
 * Entityは、JavaのオブジェクトとDBのテーブルを対応づける役割を持ちます。
 */
@Entity
@Table(name = "tasks")
public class Task {

    /**
     * タスクIDです。
     *
     * 主キーとして使われ、自動採番されます。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * タスクのタイトルです。
     *
     * nullを許可しない設定にしています。
     */
    @Column(nullable = false)
    private String title;

    /**
     * タスクが完了しているかどうかを表します。
     *
     * true の場合は完了、false の場合は未完了です。
     */
    @Column(nullable = false)
    private boolean done;

    /**
     * タスクの期限日です。
     *
     * 期限が設定されていない場合は null になります。
     */
    private LocalDate dueDate;

    /**
     * タスクの優先度です。
     *
     * EnumType.STRINGを指定することで、DBには LOW、MEDIUM、HIGH の文字列として保存されます。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    /**
     * JPAが利用する引数なしコンストラクタです。
     */
    public Task() {
    }

    /**
     * タイトルと期限日を指定してタスクを作成します。
     *
     * 新規作成時は、未完了かつ通常優先度のタスクとして作成します。
     *
     * @param title タスクのタイトル
     * @param dueDate タスクの期限日
     */
    public Task(String title, LocalDate dueDate) {
        this(title, dueDate, Priority.MEDIUM);
    }

    /**
     * タイトル、期限日、優先度を指定してタスクを作成します。
     *
     * 新規作成時は、未完了のタスクとして作成します。
     *
     * @param title タスクのタイトル
     * @param dueDate タスクの期限日
     * @param priority タスクの優先度
     */
    public Task(String title, LocalDate dueDate, Priority priority) {
        this.title = title;
        this.dueDate = dueDate;
        this.priority = priority;
        this.done = false;
    }

    /**
     * タスクIDを返します。
     *
     * @return タスクID
     */
    public Long getId() {
        return id;
    }

    /**
     * タスクのタイトルを返します。
     *
     * @return タスクのタイトル
     */
    public String getTitle() {
        return title;
    }

    /**
     * タスクのタイトルを設定します。
     *
     * @param title 新しいタイトル
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * タスクが完了しているかどうかを返します。
     *
     * @return 完了している場合はtrue、未完了の場合はfalse
     */
    public boolean isDone() {
        return done;
    }

    /**
     * タスクの完了状態を設定します。
     *
     * @param done 完了している場合はtrue、未完了の場合はfalse
     */
    public void setDone(boolean done) {
        this.done = done;
    }

    /**
     * タスクの期限日を返します。
     *
     * @return タスクの期限日。未設定の場合は null
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * タスクの期限日を設定します。
     *
     * @param dueDate タスクの期限日。未設定の場合は null
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * タスクの優先度を返します。
     *
     * @return タスクの優先度
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * タスクの優先度を設定します。
     *
     * @param priority タスクの優先度
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    /**
     * タスクの完了状態を反転します。
     *
     * 未完了なら完了に、完了なら未完了に切り替えます。
     */
    public void toggleDone() {
        this.done = !this.done;
    }

    /**
     * 期限日の表示用文字列を返します。
     *
     * 期限が未設定の場合は「期限なし」を返します。
     * 期限が今日より前の場合は「期限: yyyy-MM-dd（期限切れ）」を返します。
     * 期限が今日の場合は「期限: yyyy-MM-dd（今日）」を返します。
     * それ以外の場合は「期限: yyyy-MM-dd」を返します。
     *
     * @return 期限日の表示用文字列
     */
    public String getDueDateLabel() {
        if (dueDate == null) {
            return "期限なし";
        }

        LocalDate today = LocalDate.now();

        if (dueDate.isBefore(today)) {
            return "期限: " + dueDate + "（期限切れ）";
        }

        if (dueDate.isEqual(today)) {
            return "期限: " + dueDate + "（今日）";
        }

        return "期限: " + dueDate;
    }
}