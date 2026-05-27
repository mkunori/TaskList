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
     * 完了状態による絞り込み、キーワード検索、登録順の並び替え、
     * 期限が近い順の並び替え、優先度が高い順の並び替えはRepository側で行います。
     *
     * @param ownerId 匿名ユーザーID
     * @param filterType 表示条件
     * @param sortType 並び替え条件
     * @param keyword 検索キーワード
     * @return 条件に一致するタスク一覧
     */
    public List<Task> findTasks(String ownerId, TaskFilterType filterType, TaskSortType sortType, String keyword) {

        if (sortType == null || sortType == TaskSortType.CREATED) {
            return findTasksByFilterAndKeywordOrderByCreated(ownerId, filterType, keyword);
        }

        if (sortType == TaskSortType.DUE_DATE) {
            return findTasksByFilterAndKeywordOrderByDueDate(ownerId, filterType, keyword);
        }

        if (sortType == TaskSortType.PRIORITY) {
            return findTasksByFilterAndKeywordOrderByPriority(ownerId, filterType, keyword);
        }

        return findTasksByFilterAndKeywordOrderByCreated(ownerId, filterType, keyword);
    }

    /**
     * 表示条件とキーワードに応じて、登録順でRepositoryからタスク一覧を取得します。
     *
     * キーワードが空の場合は、表示条件だけで取得します。
     * キーワードが入力されている場合は、タイトルにキーワードを含むタスクだけを取得します。
     * どちらの場合も、IDの昇順で取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @param keyword    検索キーワード
     * @return 登録順のタスク一覧
     */
    private List<Task> findTasksByFilterAndKeywordOrderByCreated(String ownerId, TaskFilterType filterType, String keyword) {

        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        if (normalizedKeyword.isEmpty()) {
            return findTasksByFilterOrderByCreated(ownerId, filterType);
        }

        return findTasksByFilterAndNonEmptyKeywordOrderByCreated(
                ownerId,
                filterType,
                normalizedKeyword);
    }

    /**
     * 表示条件に応じて、登録順でRepositoryからタスク一覧を取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @return 登録順のタスク一覧
     */
    private List<Task> findTasksByFilterOrderByCreated(String ownerId, TaskFilterType filterType) {

        if (filterType == null) {
            return taskRepository.findByOwnerIdOrderByIdAsc(ownerId);
        }

        return switch (filterType) {
            case ALL -> taskRepository.findByOwnerIdOrderByIdAsc(ownerId);
            case ACTIVE -> taskRepository.findByOwnerIdAndDoneOrderByIdAsc(ownerId, false);
            case DONE -> taskRepository.findByOwnerIdAndDoneOrderByIdAsc(ownerId, true);
        };
    }

    /**
     * 表示条件と空ではないキーワードに応じて、
     * 登録順でRepositoryからタスク一覧を取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @param keyword    空ではない検索キーワード
     * @return 登録順のタスク一覧
     */
    private List<Task> findTasksByFilterAndNonEmptyKeywordOrderByCreated(String ownerId, TaskFilterType filterType, String keyword) {

        if (filterType == null) {
            return taskRepository.findByOwnerIdAndTitleContainingIgnoreCaseOrderByIdAsc(ownerId, keyword);
        }

        return switch (filterType) {
            case ALL -> taskRepository.findByOwnerIdAndTitleContainingIgnoreCaseOrderByIdAsc(ownerId, keyword);
            case ACTIVE -> taskRepository.findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByIdAsc(ownerId, false, keyword);
            case DONE -> taskRepository.findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByIdAsc(ownerId, true, keyword);
        };
    }

    /**
     * 表示条件とキーワードに応じて、Repositoryからタスク一覧を取得します。
     *
     * キーワードが空の場合は、表示条件だけで取得します。
     * キーワードが入力されている場合は、タイトルにキーワードを含むタスクだけを取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @param keyword    検索キーワード
     * @return 表示条件とキーワードに一致するタスク一覧
     */
    private List<Task> findTasksByFilterAndKeyword(String ownerId, TaskFilterType filterType, String keyword) {

        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        if (normalizedKeyword.isEmpty()) {
            return findTasksByFilter(ownerId, filterType);
        }

        return findTasksByFilterAndNonEmptyKeyword(ownerId, filterType, normalizedKeyword);
    }

    /**
     * 表示条件に応じて、Repositoryからタスク一覧を取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @return 表示条件に一致するタスク一覧
     */
    private List<Task> findTasksByFilter(String ownerId, TaskFilterType filterType) {
        if (filterType == null) {
            return taskRepository.findByOwnerId(ownerId);
        }

        return switch (filterType) {
            case ALL -> taskRepository.findByOwnerId(ownerId);
            case ACTIVE -> taskRepository.findByOwnerIdAndDone(ownerId, false);
            case DONE -> taskRepository.findByOwnerIdAndDone(ownerId, true);
        };
    }

    /**
     * 表示条件と空ではないキーワードに応じて、Repositoryからタスク一覧を取得します。
     *
     * @param ownerId    匿名ユーザーID
     * @param filterType 表示条件
     * @param keyword    空ではない検索キーワード
     * @return 表示条件とキーワードに一致するタスク一覧
     */
    private List<Task> findTasksByFilterAndNonEmptyKeyword(String ownerId, TaskFilterType filterType, String keyword) {

        if (filterType == null) {
            return taskRepository.findByOwnerIdAndTitleContainingIgnoreCase(ownerId, keyword);
        }

        return switch (filterType) {
            case ALL -> taskRepository.findByOwnerIdAndTitleContainingIgnoreCase(ownerId, keyword);
            case ACTIVE -> taskRepository.findByOwnerIdAndDoneAndTitleContainingIgnoreCase(ownerId, false, keyword);
            case DONE -> taskRepository.findByOwnerIdAndDoneAndTitleContainingIgnoreCase(ownerId, true, keyword);
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
     * 表示条件とキーワードに応じて、期限が近い順でRepositoryからタスク一覧を取得します。
     *
     * キーワードが空の場合は、表示条件だけで取得します。
     * キーワードが入力されている場合は、タイトルにキーワードを含むタスクだけを取得します。
     * どちらの場合も、期限なしのタスクは最後に表示します。
     *
     * @param ownerId 匿名ユーザーID
     * @param filterType 表示条件
     * @param keyword 検索キーワード
     * @return 期限が近い順のタスク一覧
     */
    private List<Task> findTasksByFilterAndKeywordOrderByDueDate(String ownerId, TaskFilterType filterType, String keyword) {

        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        if (normalizedKeyword.isEmpty()) {
            return findTasksByFilterOrderByDueDate(ownerId, filterType);
        }

        return findTasksByFilterAndNonEmptyKeywordOrderByDueDate(ownerId, filterType, normalizedKeyword);
    }

    /**
     * 表示条件に応じて、期限が近い順でRepositoryからタスク一覧を取得します。
     *
     * @param ownerId 匿名ユーザーID
     * @param filterType 表示条件
     * @return 期限が近い順のタスク一覧
     */
    private List<Task> findTasksByFilterOrderByDueDate(String ownerId, TaskFilterType filterType) {

        if (filterType == null) {
            return taskRepository.findByOwnerIdOrderByDueDateAscNullsLast(ownerId);
        }

        return switch (filterType) {
            case ALL -> taskRepository.findByOwnerIdOrderByDueDateAscNullsLast(ownerId);
            case ACTIVE -> taskRepository.findByOwnerIdAndDoneOrderByDueDateAscNullsLast(ownerId, false);
            case DONE -> taskRepository.findByOwnerIdAndDoneOrderByDueDateAscNullsLast(ownerId, true);
        };
    }

    /**
     * 表示条件と空ではないキーワードに応じて、
     * 期限が近い順でRepositoryからタスク一覧を取得します。
     *
     * @param ownerId 匿名ユーザーID
     * @param filterType 表示条件
     * @param keyword 空ではない検索キーワード
     * @return 期限が近い順のタスク一覧
     */
    private List<Task> findTasksByFilterAndNonEmptyKeywordOrderByDueDate(String ownerId, TaskFilterType filterType, String keyword) {

        if (filterType == null) {
            return taskRepository.findByOwnerIdAndTitleContainingIgnoreCaseOrderByDueDateAscNullsLast(ownerId, keyword);
        }

        return switch (filterType) {
            case ALL -> taskRepository.findByOwnerIdAndTitleContainingIgnoreCaseOrderByDueDateAscNullsLast(ownerId, keyword);
            case ACTIVE -> taskRepository.findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByDueDateAscNullsLast(ownerId, false, keyword);
            case DONE -> taskRepository.findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByDueDateAscNullsLast(ownerId, true, keyword);
        };
    }

    /**
     * 表示条件とキーワードに応じて、優先度が高い順でRepositoryからタスク一覧を取得します。
     *
     * キーワードが空の場合は、表示条件だけで取得します。
     * キーワードが入力されている場合は、タイトルにキーワードを含むタスクだけを取得します。
     *
     * @param ownerId 匿名ユーザーID
     * @param filterType 表示条件
     * @param keyword 検索キーワード
     * @return 優先度が高い順のタスク一覧
     */
    private List<Task> findTasksByFilterAndKeywordOrderByPriority(String ownerId, TaskFilterType filterType, String keyword) {

        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        if (normalizedKeyword.isEmpty()) {
            return findTasksByFilterOrderByPriority(ownerId, filterType);
        }

        return findTasksByFilterAndNonEmptyKeywordOrderByPriority(
                ownerId,
                filterType,
                normalizedKeyword);
    }

    /**
     * 表示条件に応じて、優先度が高い順でRepositoryからタスク一覧を取得します。
     *
     * @param ownerId 匿名ユーザーID
     * @param filterType 表示条件
     * @return 優先度が高い順のタスク一覧
     */
    private List<Task> findTasksByFilterOrderByPriority(String ownerId, TaskFilterType filterType) {

        if (filterType == null) {
            return taskRepository.findByOwnerIdOrderByPriorityHighFirst(ownerId);
        }

        return switch (filterType) {
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
     * 表示条件と空ではないキーワードに応じて、
     * 優先度が高い順でRepositoryからタスク一覧を取得します。
     *
     * @param ownerId 匿名ユーザーID
     * @param filterType 表示条件
     * @param keyword 空ではない検索キーワード
     * @return 優先度が高い順のタスク一覧
     */
    private List<Task> findTasksByFilterAndNonEmptyKeywordOrderByPriority(String ownerId, TaskFilterType filterType, String keyword) {

        if (filterType == null) {
            return taskRepository.findByOwnerIdAndTitleContainingIgnoreCaseOrderByPriorityHighFirst(
                    ownerId,
                    keyword);
        }

        return switch (filterType) {
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
}