package com.mkunori.tasklist.service;

import java.time.LocalDate;
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
     * 完了状態による絞り込み、キーワード検索、並び替えはRepository側で行います。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @param sortType   並び替え条件
     * @param keyword    検索キーワード
     * @return 条件に一致するタスク一覧
     */
    public List<Task> findTasks(String ownerId, TaskFilterType filterType, TaskSortType sortType, String keyword) {

        TaskSortType actualSortType = sortType == null ? TaskSortType.CREATED : sortType;
        String normalizedKeyword = normalizeKeyword(keyword);

        return switch (actualSortType) {
            case CREATED -> findTasksOrderByCreated(ownerId, filterType, normalizedKeyword);
            case DUE_DATE -> findTasksOrderByDueDate(ownerId, filterType, normalizedKeyword);
            case PRIORITY -> findTasksOrderByPriority(ownerId, filterType, normalizedKeyword);
        };
    }

    /**
     * 検索キーワードを扱いやすい形に整えます。
     *
     * nullの場合は空文字に変換し、前後の空白を取り除きます。
     *
     * @param keyword 検索キーワード
     * @return 整形後の検索キーワード
     */
    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }

        return keyword.trim();
    }

    /**
     * 検索キーワードが入力されているかを判定します。
     *
     * @param keyword 整形済みの検索キーワード
     * @return キーワードが入力されている場合はtrue
     */
    private boolean hasKeyword(String keyword) {
        return !keyword.isEmpty();
    }

    /**
     * 表示条件とキーワードに応じて、登録順でタスク一覧を取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @param keyword    整形済みの検索キーワード
     * @return 登録順のタスク一覧
     */
    private List<Task> findTasksOrderByCreated(String ownerId, TaskFilterType filterType, String keyword) {

        if (hasKeyword(keyword)) {
            return findTasksOrderByCreatedWithKeyword(ownerId, filterType, keyword);
        }

        return findTasksOrderByCreatedWithoutKeyword(ownerId, filterType);
    }

    /**
     * 表示条件に応じて、登録順でタスク一覧を取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @return 登録順のタスク一覧
     */
    private List<Task> findTasksOrderByCreatedWithoutKeyword(String ownerId, TaskFilterType filterType) {

        TaskFilterType actualFilterType = normalizeFilterType(filterType);

        return switch (actualFilterType) {
            case ALL -> taskRepository.findByOwnerIdOrderByIdAsc(ownerId);
            case ACTIVE -> taskRepository.findByOwnerIdAndDoneOrderByIdAsc(ownerId, false);
            case DONE -> taskRepository.findByOwnerIdAndDoneOrderByIdAsc(ownerId, true);
        };
    }

    /**
     * 表示条件とキーワードに応じて、登録順でタスク一覧を取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @param keyword    整形済みの検索キーワード
     * @return 登録順のタスク一覧
     */
    private List<Task> findTasksOrderByCreatedWithKeyword(String ownerId, TaskFilterType filterType, String keyword) {

        TaskFilterType actualFilterType = normalizeFilterType(filterType);

        return switch (actualFilterType) {
            case ALL -> taskRepository.findByOwnerIdAndTitleContainingIgnoreCaseOrderByIdAsc(
                    ownerId,
                    keyword);
            case ACTIVE -> taskRepository.findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByIdAsc(
                    ownerId,
                    false,
                    keyword);
            case DONE -> taskRepository.findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByIdAsc(
                    ownerId,
                    true,
                    keyword);
        };
    }

    /**
     * 表示条件とキーワードに応じて、期限が近い順でタスク一覧を取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @param keyword    整形済みの検索キーワード
     * @return 期限が近い順のタスク一覧
     */
    private List<Task> findTasksOrderByDueDate(String ownerId, TaskFilterType filterType, String keyword) {

        if (hasKeyword(keyword)) {
            return findTasksOrderByDueDateWithKeyword(ownerId, filterType, keyword);
        }

        return findTasksOrderByDueDateWithoutKeyword(ownerId, filterType);
    }

    /**
     * 表示条件に応じて、期限が近い順でタスク一覧を取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @return 期限が近い順のタスク一覧
     */
    private List<Task> findTasksOrderByDueDateWithoutKeyword(String ownerId, TaskFilterType filterType) {

        TaskFilterType actualFilterType = normalizeFilterType(filterType);

        return switch (actualFilterType) {
            case ALL -> taskRepository.findByOwnerIdOrderByDueDateAscNullsLast(ownerId);
            case ACTIVE -> taskRepository.findByOwnerIdAndDoneOrderByDueDateAscNullsLast(
                    ownerId,
                    false);
            case DONE -> taskRepository.findByOwnerIdAndDoneOrderByDueDateAscNullsLast(
                    ownerId,
                    true);
        };
    }

    /**
     * 表示条件とキーワードに応じて、期限が近い順でタスク一覧を取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @param keyword    整形済みの検索キーワード
     * @return 期限が近い順のタスク一覧
     */
    private List<Task> findTasksOrderByDueDateWithKeyword(String ownerId, TaskFilterType filterType, String keyword) {

        TaskFilterType actualFilterType = normalizeFilterType(filterType);

        return switch (actualFilterType) {
            case ALL -> taskRepository.findByOwnerIdAndTitleContainingIgnoreCaseOrderByDueDateAscNullsLast(
                    ownerId,
                    keyword);
            case ACTIVE -> taskRepository.findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByDueDateAscNullsLast(
                    ownerId,
                    false,
                    keyword);
            case DONE -> taskRepository.findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByDueDateAscNullsLast(
                    ownerId,
                    true,
                    keyword);
        };
    }

    /**
     * 表示条件とキーワードに応じて、優先度が高い順でタスク一覧を取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @param keyword    整形済みの検索キーワード
     * @return 優先度が高い順のタスク一覧
     */
    private List<Task> findTasksOrderByPriority(String ownerId, TaskFilterType filterType, String keyword) {

        if (hasKeyword(keyword)) {
            return findTasksOrderByPriorityWithKeyword(ownerId, filterType, keyword);
        }

        return findTasksOrderByPriorityWithoutKeyword(ownerId, filterType);
    }

    /**
     * 表示条件に応じて、優先度が高い順でタスク一覧を取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @return 優先度が高い順のタスク一覧
     */
    private List<Task> findTasksOrderByPriorityWithoutKeyword(String ownerId, TaskFilterType filterType) {

        TaskFilterType actualFilterType = normalizeFilterType(filterType);

        return switch (actualFilterType) {
            case ALL -> taskRepository.findByOwnerIdOrderByPriorityHighFirst(ownerId);
            case ACTIVE -> taskRepository.findByOwnerIdAndDoneOrderByPriorityHighFirst(
                    ownerId,
                    false);
            case DONE -> taskRepository.findByOwnerIdAndDoneOrderByPriorityHighFirst(
                    ownerId,
                    true);
        };
    }

    /**
     * 表示条件とキーワードに応じて、優先度が高い順でタスク一覧を取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @param keyword    整形済みの検索キーワード
     * @return 優先度が高い順のタスク一覧
     */
    private List<Task> findTasksOrderByPriorityWithKeyword(String ownerId, TaskFilterType filterType, String keyword) {

        TaskFilterType actualFilterType = normalizeFilterType(filterType);

        return switch (actualFilterType) {
            case ALL -> taskRepository.findByOwnerIdAndTitleContainingIgnoreCaseOrderByPriorityHighFirst(
                    ownerId,
                    keyword);
            case ACTIVE -> taskRepository.findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByPriorityHighFirst(
                    ownerId,
                    false,
                    keyword);
            case DONE -> taskRepository.findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByPriorityHighFirst(
                    ownerId,
                    true,
                    keyword);
        };
    }

    /**
     * 新しいタスクを追加します。
     *
     * @param title    タスクのタイトル
     * @param dueDate  タスクの期限日。未入力の場合は null
     * @param priority タスクの優先度
     * @param ownerId  匿名ユーザーID
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
     * @param id      削除するタスクのID
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
     * @param id      完了状態を切り替えるタスクのID
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
     * @param id      編集対象のタスクID
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
     * @param ownerId        匿名ユーザーID
     * @return 更新できた場合はtrue、対象タスクが見つからなかった場合はfalse
     */
    public boolean updateTask(TaskUpdateForm taskUpdateForm, String ownerId) {
        Optional<Task> optionalTask = taskRepository.findByIdAndOwnerId(taskUpdateForm.getId(), ownerId);

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

    /**
     * 表示条件を扱いやすい形に整えます。
     *
     * nullの場合はALLとして扱います。
     *
     * @param filterType 表示条件
     * @return 整形後の表示条件
     */
    private TaskFilterType normalizeFilterType(TaskFilterType filterType) {
        if (filterType == null) {
            return TaskFilterType.ALL;
        }

        return filterType;
    }
}