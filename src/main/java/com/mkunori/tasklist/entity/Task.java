package com.mkunori.tasklist.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
     * JPAが利用する引数なしコンストラクタです。
     *
     * Entityでは、JPAが内部でオブジェクトを作成できるように、
     * 引数なしコンストラクタが必要です。
     */
    public Task() {
    }

    /**
     * タイトルを指定してタスクを作成します。
     *
     * 新規作成時は、未完了のタスクとして作成します。
     *
     * @param title タスクのタイトル
     */
    public Task(String title) {
        this.title = title;
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
     * boolean型のgetterは、getDoneではなくisDoneという名前にすることが多いです。
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
     * タスクの完了状態を反転します。
     *
     * 未完了なら完了に、完了なら未完了に切り替えます。
     */
    public void toggleDone() {
        this.done = !this.done;
    }
}