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
 * Controllerから依頼を受けて、アプリケーションの処理を実行します。
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
     * すべてのタスクを登録順で取得します。
     *
     * 既存の呼び出し箇所を残すためのメソッドです。
     * 内部では、表示条件を「すべて」、並び替え条件を「登録順」、
     * キーワードなしとして取得しています。
     *
     * @return 登録順のタスク一覧
     */
    public List<Task> findAllTasks() {
        return findTasks(TaskFilterType.ALL, TaskSortType.CREATED, "");
    }

    /**
     * 指定された表示条件、並び替え条件、キーワードでタスク一覧を取得します。
     *
     * 今回はDBから全件取得したあと、Java側で絞り込み、検索、並び替えを行っています。
     *
     * @param filterType 表示条件
     * @param sortType 並び替え条件
     * @param keyword 検索キーワード
     * @return 絞り込み、検索、並び替えを行ったタスク一覧
     */
    public List<Task> findTasks(TaskFilterType filterType, TaskSortType sortType, String keyword) {
        // DBからすべてのタスクを取得する
        List<Task> tasks = taskRepository.findAll();

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
        // filterTypeがnullの場合は、すべて表示として扱う
        if (filterType == null) {
            return tasks;
        }

        return switch (filterType) {
            case ALL -> tasks;
            case ACTIVE -> tasks.stream()
                    .filter(task -> !task.isDone())
                    .toList();
            case DONE -> tasks.stream()
                    .filter(task -> task.isDone())
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
        // keyword が null の場合でも扱えるように、空文字へ変換する
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        // キーワードが空なら、検索せずにそのまま返す
        if (normalizedKeyword.isEmpty()) {
            return tasks;
        }

        // 大文字小文字を区別しないため、小文字に変換して比較する
        String lowerKeyword = normalizedKeyword.toLowerCase();

        return tasks.stream()
                .filter(task -> task.getTitle().toLowerCase().contains(lowerKeyword))
                .toList();
    }

    /**
     * 並び替え条件に応じてタスク一覧を並び替えます。
     *
     * @param tasks 並び替え前のタスク一覧
     * @param sortType 並び替え条件
     * @return 並び替え後のタスク一覧
     */
    private List<Task> sortTasks(List<Task> tasks, TaskSortType sortType) {
        // sortTypeがnullの場合は、登録順として扱う
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
                .sorted(Comparator.comparing(task -> task.getId()))
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
                        task -> task.getDueDate(),
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /**
     * タスク一覧を優先度が高い順で並び替えます。
     *
     * PriorityのsortOrderを使い、HIGH、MEDIUM、LOW の順に表示します。
     * 優先度が同じタスクは、登録順で並べます。
     *
     * @param tasks 並び替え前のタスク一覧
     * @return 優先度が高い順に並び替えたタスク一覧
     */
    private List<Task> sortByPriority(List<Task> tasks) {
        return tasks.stream()
                .sorted(Comparator
                        .comparing((Task task) -> task.getPriority().getSortOrder())
                        .reversed()
                        .thenComparing(task -> task.getId()))
                .toList();
    }

    /**
     * 新しいタスクを追加します。
     * 
     * 画面から受け取ったタイトルと期限日を使ってTaskエンティティを作成し、
     * DBへ保存します。
     *
     * @param title タスクのタイトル
     * @param dueDate タスクの期限日。未入力の場合は null
     * @param priority タスクの優先度
     */
    public void addTask(String title, LocalDate dueDate, Priority priority) {
        // フォーム入力値から、DB保存用のEntityを作成する
        Task task = new Task(title, dueDate, priority);

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
        form.setDueDate(task.getDueDate());
        form.setPriority(task.getPriority());

        return Optional.of(form);
    }

    /**
     * 指定されたタスクのタイトル、期限日、優先度を更新します。
     * 
     * 更新対象のタスクをDBから取得し、フォームの値で上書きして保存します。
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

        // DBから取得したEntityを、フォームの値で更新する
        Task task = optionalTask.get();
        task.setTitle(taskUpdateForm.getTitle());
        task.setDueDate(taskUpdateForm.getDueDate());
        task.setPriority(taskUpdateForm.getPriority());

        // 変更したEntityをDBへ保存する
        taskRepository.save(task);

        return true;
    }
}