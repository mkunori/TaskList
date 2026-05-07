package com.mkunori.tasklist.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mkunori.tasklist.entity.Task;
import com.mkunori.tasklist.repository.TaskRepository;

/**
 * タスクに関する処理を担当するサービスクラスです。
 *
 * Serviceは、ControllerとRepositoryの間に入るクラスです。
 * Controllerから依頼を受けて、アプリケーションの処理を実行します。
 *
 * このクラスでは、タスク一覧の取得、追加、削除、完了状態の切り替えを担当します。
 */
@Service
public class TaskService {

    /**
     * タスクをDBから読み書きするためのリポジトリです。
     */
    private final TaskRepository taskRepository;

    /**
     * コンストラクタです。
     *
     * SpringがTaskRepositoryを自動で渡してくれます。
     *
     * @param taskRepository タスクリポジトリ
     */
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * すべてのタスクを取得します。
     *
     * ControllerはDBの詳しい取得方法を知らなくても、
     * このメソッドを呼ぶだけでタスク一覧を取得できます。
     *
     * @return すべてのタスク一覧
     */
    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * 新しいタスクを追加します。
     *
     * 画面から受け取ったタイトルを使ってTaskエンティティを作成し、
     * DBへ保存します。
     *
     * @param title タスクのタイトル
     */
    public void addTask(String title) {
        // フォーム入力値から、DB保存用のEntityを作成する
        Task task = new Task(title);

        // Repositoryを使ってDBへ保存する
        taskRepository.save(task);
    }

    /**
     * 指定されたIDのタスクを削除します。
     *
     * @param id 削除するタスクのID
     */
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    /**
     * 指定されたIDのタスクの完了状態を切り替えます。
     *
     * 未完了のタスクなら完了にし、完了済みのタスクなら未完了に戻します。
     *
     * @param id 完了状態を切り替えるタスクのID
     */
    public void toggleTaskDone(Long id) {
        // IDを使ってDBからタスクを1件探す
        Optional<Task> optionalTask = taskRepository.findById(id);

        // タスクが見つからなかった場合は、何もしない
        if (optionalTask.isEmpty()) {
            return;
        }

        // OptionalからTaskを取り出す
        Task task = optionalTask.get();

        // Entityに用意したメソッドで、true/falseを反転する
        task.toggleDone();

        // 変更したEntityをDBへ保存する
        taskRepository.save(task);
    }
}