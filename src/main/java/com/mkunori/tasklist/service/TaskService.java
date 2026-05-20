package com.mkunori.tasklist.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mkunori.tasklist.entity.Priority;
import com.mkunori.tasklist.entity.Task;
import com.mkunori.tasklist.form.TaskUpdateForm;
import com.mkunori.tasklist.repository.TaskRepository;

/**
 * タスクに関する処理を担当するサービスクラスです。
 *
 * Serviceは、ControllerとRepositoryの間に入るクラスです。
 * Controllerから依頼を受けて、タスクの追加、更新、削除、完了状態の切り替え、
 * 絞り込み、検索、並び替えを実行します。
 *
 * このアプリではログイン機能を使わず、Cookieに保存した匿名ユーザーIDを使って、
 * ブラウザごとにタスクを分けています。
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
     * 指定された匿名ユーザーID、表示条件、並び替え条件、キーワードでタスク一覧を取得します。
     *
     * DBから指定された匿名ユーザーIDに紐づくタスクだけを取得したあと、
     * Java側で絞り込み、検索、並び替えを行います。
     *
     * @param ownerId 匿名ユーザーID
     * @param filterType 表示条件
     * @param sortType 並び替え条件
     * @param keyword 検索キーワード
     * @return 絞り込み、検索、並び替えを行ったタスク一覧
     */
    public List<Task> findTasks(
            String ownerId,
            TaskFilterType filterType,
            TaskSortType sortType,
            String keyword) {

        // この匿名ユーザーIDに紐づくタスクだけを取得する
        List<Task> tasks = taskRepository.findByOwnerId(ownerId);

        // まず完了状態で絞り込む
        List<Task> filteredTasks = filterTasks(tasks, filterType);

        // 次にキーワードで絞り込む
        List<Task> searchedTasks = searchTasks(filteredTasks, keyword);

        // 最後に並び替える
        return sortTasks(searchedTasks, sortType);
    }

    /**
     * 表示条件に応じてタスク一覧を絞り込みます。
     *
     * ALLなら全件、ACTIVEなら未完了のみ、DONEなら完了済みのみを返します。
     *
     * @param tasks 絞り込み前のタスク一覧
     * @param filterType 表示条件
     * @return 絞り込み後のタスク一覧
     */
    private List<Task> filterTasks(List<Task> tasks, TaskFilterType filterType) {
        if (filterType == null) {
            return tasks;
        }

        return switch (filterType) {
            case ALL -> tasks;
            case ACTIVE -> tasks.stream()
                    .filter(task -> !task.isDone())
                    .toList();
            case DONE -> tasks.stream()
                    .filter(Task::isDone)
                    .toList();
        };
    }

    /**
     * キーワードに応じてタスク一覧を絞り込みます。
     *
     * キーワードが空の場合は、検索せずに元の一覧をそのまま返します。
     * 今回はタスクタイトルにキーワードが含まれているかを調べます。
     *
     * @param tasks 検索前のタスク一覧
     * @param keyword 検索キーワード
     * @return 検索後のタスク一覧
     */
    private List<Task> searchTasks(List<Task> tasks, String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        if (normalizedKeyword.isEmpty()) {
            return tasks;
        }

        String lowerKeyword = normalizedKeyword.toLowerCase();

        return tasks.stream()
                .filter(task -> containsKeyword(task, lowerKeyword))
                .toList();
    }

    /**
     * タスクのタイトルにキーワードが含まれているかを判定します。
     *
     * タイトルが null の場合は、検索対象外として false を返します。
     *
     * @param task 検索対象のタスク
     * @param lowerKeyword 小文字に変換済みの検索キーワード
     * @return タイトルにキーワードが含まれている場合はtrue
     */
    private boolean containsKeyword(Task task, String lowerKeyword) {
        String title = task.getTitle();

        if (title == null) {
            return false;
        }

        return title.toLowerCase().contains(lowerKeyword);
    }

    /**
     * 並び替え条件に応じてタスク一覧を並び替えます。
     *
     * @param tasks 並び替え前のタスク一覧
     * @param sortType 並び替え条件
     * @return 並び替え後のタスク一覧
     */
    private List<Task> sortTasks(List<Task> tasks, TaskSortType sortType) {
        if (sortType == null) {
            return sortByCreated(tasks);
        }

        return switch (sortType) {
            case CREATED -> sortByCreated(tasks);
            case DUE_DATE -> sortByDueDate(tasks);
            case PRIORITY -> sortByPriority(tasks);
        };
    }

    /**
     * タスク一覧を登録順で並び替えます。
     *
     * idは自動採番されるため、idの昇順にすると登録が古い順になります。
     *
     * @param tasks 並び替え前のタスク一覧
     * @return 登録順に並び替えたタスク一覧
     */
    private List<Task> sortByCreated(List<Task> tasks) {
        return tasks.stream()
                .sorted(Comparator.comparing(Task::getId))
                .toList();
    }

    /**
     * タスク一覧を期限が近い順で並び替えます。
     *
     * dueDateがnullのタスク、つまり期限なしのタスクは最後に並べます。
     *
     * @param tasks 並び替え前のタスク一覧
     * @return 期限が近い順に並び替えたタスク一覧
     */
    private List<Task> sortByDueDate(List<Task> tasks) {
        return tasks.stream()
                .sorted(Comparator.comparing(
                        Task::getDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /**
     * タスク一覧を優先度が高い順で並び替えます。
     *
     * PriorityのsortOrderを使い、HIGH、MEDIUM、LOW の順に表示します。
     * 優先度が同じタスクは、登録順で並べます。
     *
     * 優先度が未設定のタスクがあった場合は、もっとも低い優先度として扱います。
     *
     * @param tasks 並び替え前のタスク一覧
     * @return 優先度が高い順に並び替えたタスク一覧
     */
    private List<Task> sortByPriority(List<Task> tasks) {
        return tasks.stream()
                .sorted(Comparator
                        .comparing((Task task) -> getPrioritySortOrder(task))
                        .reversed()
                        .thenComparing(Task::getId))
                .toList();
    }

    /**
     * タスクの優先度並び替え用の値を返します。
     *
     * 優先度が未設定の場合は、もっとも低い値として扱います。
     *
     * @param task 対象タスク
     * @return 優先度の並び替え用の値
     */
    private int getPrioritySortOrder(Task task) {
        if (task.getPriority() == null) {
            return 0;
        }

        return task.getPriority().getSortOrder();
    }

    /**
     * 新しいタスクを追加します。
     *
     * @param title タスクのタイトル
     * @param dueDate タスクの期限日。未入力の場合は null
     * @param priority タスクの優先度
     * @param ownerId 匿名ユーザーID
     */
    public void addTask(String title, LocalDate dueDate, Priority priority, String ownerId) {
        Task task = new Task(title, dueDate, priority, ownerId);

        taskRepository.save(task);
    }

    /**
     * 指定されたIDのタスクを削除します。
     *
     * タスクIDだけでなく匿名ユーザーIDも条件にすることで、
     * 他のブラウザのタスクを削除できないようにします。
     *
     * @param id 削除するタスクのID
     * @param ownerId 匿名ユーザーID
     */
    public void deleteTask(Long id, String ownerId) {
        taskRepository.deleteByIdAndOwnerId(id, ownerId);
    }

    /**
     * 指定されたIDのタスクの完了状態を切り替えます。
     *
     * タスクIDだけでなく匿名ユーザーIDも条件にすることで、
     * 他のブラウザのタスクを操作できないようにします。
     *
     * @param id 完了状態を切り替えるタスクのID
     * @param ownerId 匿名ユーザーID
     */
    public void toggleTaskDone(Long id, String ownerId) {
        Optional<Task> optionalTask = taskRepository.findByIdAndOwnerId(id, ownerId);

        if (optionalTask.isEmpty()) {
            return;
        }

        Task task = optionalTask.get();

        task.toggleDone();

        taskRepository.save(task);
    }

    /**
     * 編集画面に表示するためのフォームを作成します。
     *
     * 指定されたタスクIDと匿名ユーザーIDに一致するタスクだけを取得し、
     * 画面表示用のTaskUpdateFormへ詰め替えます。
     *
     * @param id 編集対象のタスクID
     * @param ownerId 匿名ユーザーID
     * @return 編集画面用フォーム。タスクが見つからない場合は空のOptional
     */
    public Optional<TaskUpdateForm> findUpdateFormById(Long id, String ownerId) {
        Optional<Task> optionalTask = taskRepository.findByIdAndOwnerId(id, ownerId);

        if (optionalTask.isEmpty()) {
            return Optional.empty();
        }

        Task task = optionalTask.get();
        TaskUpdateForm form = new TaskUpdateForm();

        form.setId(task.getId());
        form.setTitle(task.getTitle());
        form.setDueDate(task.getDueDate());
        form.setPriority(task.getPriority());

        return Optional.of(form);
    }

    /**
     * 指定されたタスクのタイトル、期限日、優先度を更新します。
     *
     * タスクIDだけでなく匿名ユーザーIDも条件にすることで、
     * 他のブラウザのタスクを更新できないようにします。
     *
     * @param taskUpdateForm 更新フォーム
     * @param ownerId 匿名ユーザーID
     * @return 更新できた場合はtrue、対象タスクが見つからなかった場合はfalse
     */
    public boolean updateTask(TaskUpdateForm taskUpdateForm, String ownerId) {
        Optional<Task> optionalTask =
                taskRepository.findByIdAndOwnerId(taskUpdateForm.getId(), ownerId);

        if (optionalTask.isEmpty()) {
            return false;
        }

        Task task = optionalTask.get();

        task.setTitle(taskUpdateForm.getTitle());
        task.setDueDate(taskUpdateForm.getDueDate());
        task.setPriority(taskUpdateForm.getPriority());

        taskRepository.save(task);

        return true;
    }
}