package com.example.tasklist.entity;

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
     * null を許可しない設定にしています。
     */
    @Column(nullable = false)
    private String title;

    /**
     * JPAが利用する引数なしコンストラクタです。
     */
    public Task() {
    }

    /**
     * タイトルを指定してタスクを作成します。
     * 
     * @param title タスクのタイトル
     */
    public Task(String title) {
        this.title = title;
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
}