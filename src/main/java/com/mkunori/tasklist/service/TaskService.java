package com.mkunori.tasklist.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mkunori.tasklist.entity.Task;
import com.mkunori.tasklist.form.TaskUpdateForm;
import com.mkunori.tasklist.repository.TaskRepository;

/**
 * タスクに関する処理を担当するサービスクラスです。
 *
 * Serviceは、ControllerとRepositoryの間に入るクラスです。
 * Controllerから依頼を受けて、アプリケーションの処理を実行します。
 *
 * このクラスでは、タスク一覧の取得、追加、削除、完了状態の切り替え、更新を担当します。
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

    /**
     * 編集画面に表示するためのフォームを作成します。
     *
     * 指定されたIDのタスクをDBから取得し、
     * 画面表示用のTaskUpdateFormへ詰め替えます。
     *
     * @param id 編集対象のタスクID
     * @return 編集画面用フォーム。タスクが見つからない場合は空のOptional
     */
    public Optional<TaskUpdateForm> findUpdateFormById(Long id) {
        // IDを使ってDBからタスクを探す
        Optional<Task> optionalTask = taskRepository.findById(id);

        // タスクが見つからなければ空のOptionalを返す
        if (optionalTask.isEmpty()) {
            return Optional.empty();
        }

        // Entityからフォームへ値を詰め替える
        Task task = optionalTask.get();
        TaskUpdateForm form = new TaskUpdateForm();
        form.setId(task.getId());
        form.setTitle(task.getTitle());

        return Optional.of(form);
    }

    /**
     * 指定されたタスクのタイトルを更新します。
     *
     * 更新対象のタスクをDBから取得し、フォームのタイトルで上書きして保存します。
     *
     * @param taskUpdateForm 更新フォーム
     * @return 更新できた場合はtrue、対象タスクが見つからなかった場合はfalse
     */
    public boolean updateTask(TaskUpdateForm taskUpdateForm) {
        // フォームに入っているIDを使って、更新対象のタスクを探す
        Optional<Task> optionalTask = taskRepository.findById(taskUpdateForm.getId());

        // 対象タスクが存在しない場合は更新できない
        if (optionalTask.isEmpty()) {
            return false;
        }

        // DBから取得したEntityのタイトルを、フォームの値で更新する
        Task task = optionalTask.get();
        task.setTitle(taskUpdateForm.getTitle());

        // 変更したEntityをDBへ保存する
        taskRepository.save(task);

        return true;
    }
}