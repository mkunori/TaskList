package com.mkunori.tasklist.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mkunori.tasklist.entity.Priority;
import com.mkunori.tasklist.entity.Task;
import com.mkunori.tasklist.form.TaskUpdateForm;
import com.mkunori.tasklist.repository.TaskRepository;

/**
 * TaskServiceの単体テストです。
 *
 * Repositoryはモックにして、DBには接続せずにServiceの処理だけを確認します。
 * 検索、絞り込み、並び替え、追加、更新、削除、完了切り替えをテストします。
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    /**
     * DB操作を担当するRepositoryのモックです。
     *
     * 本物のDBには接続せず、テストで指定した戻り値を返すようにします。
     */
    @Mock
    private TaskRepository taskRepository;

    /**
     * テスト対象のServiceです。
     *
     * @InjectMocks により、上で作成した taskRepository のモックが自動で注入されます。
     */
    @InjectMocks
    private TaskService taskService;

    /**
     * 表示条件がALLの場合、すべてのタスクが取得されることを確認します。
     */
    @Test
    void findTasks_all_returnsAllTasks() {
        Task task1 = createTask(1L, "Javaを学ぶ", false, null, Priority.MEDIUM);
        Task task2 = createTask(2L, "Springを学ぶ", true, null, Priority.HIGH);

        when(taskRepository.findAll()).thenReturn(List.of(task1, task2));

        List<Task> actual = taskService.findTasks(
                TaskFilterType.ALL,
                TaskSortType.CREATED,
                "");

        assertEquals(2, actual.size());
        assertEquals("Javaを学ぶ", actual.get(0).getTitle());
        assertEquals("Springを学ぶ", actual.get(1).getTitle());
    }

    /**
     * 表示条件がACTIVEの場合、未完了のタスクだけが取得されることを確認します。
     */
    @Test
    void findTasks_active_returnsOnlyUndoneTasks() {
        Task undoneTask = createTask(1L, "未完了タスク", false, null, Priority.MEDIUM);
        Task doneTask = createTask(2L, "完了済みタスク", true, null, Priority.MEDIUM);

        when(taskRepository.findAll()).thenReturn(List.of(undoneTask, doneTask));

        List<Task> actual = taskService.findTasks(
                TaskFilterType.ACTIVE,
                TaskSortType.CREATED,
                "");

        assertEquals(1, actual.size());
        assertEquals("未完了タスク", actual.get(0).getTitle());
        assertFalse(actual.get(0).isDone());
    }

    /**
     * 表示条件がDONEの場合、完了済みのタスクだけが取得されることを確認します。
     */
    @Test
    void findTasks_done_returnsOnlyDoneTasks() {
        Task undoneTask = createTask(1L, "未完了タスク", false, null, Priority.MEDIUM);
        Task doneTask = createTask(2L, "完了済みタスク", true, null, Priority.MEDIUM);

        when(taskRepository.findAll()).thenReturn(List.of(undoneTask, doneTask));

        List<Task> actual = taskService.findTasks(
                TaskFilterType.DONE,
                TaskSortType.CREATED,
                "");

        assertEquals(1, actual.size());
        assertEquals("完了済みタスク", actual.get(0).getTitle());
        assertTrue(actual.get(0).isDone());
    }

    /**
     * キーワード検索で、タイトルにキーワードを含むタスクだけが取得されることを確認します。
     */
    @Test
    void findTasks_keyword_returnsMatchedTasks() {
        Task task1 = createTask(1L, "Spring Bootを学ぶ", false, null, Priority.MEDIUM);
        Task task2 = createTask(2L, "Java Silver復習", false, null, Priority.MEDIUM);
        Task task3 = createTask(3L, "Spring JPA確認", false, null, Priority.MEDIUM);

        when(taskRepository.findAll()).thenReturn(List.of(task1, task2, task3));

        List<Task> actual = taskService.findTasks(
                TaskFilterType.ALL,
                TaskSortType.CREATED,
                "Spring");

        assertEquals(2, actual.size());
        assertEquals("Spring Bootを学ぶ", actual.get(0).getTitle());
        assertEquals("Spring JPA確認", actual.get(1).getTitle());
    }

    /**
     * 期限が近い順で並び替えられることを確認します。
     *
     * 期限なしのタスクは最後に表示されます。
     */
    @Test
    void findTasks_dueDateSort_putsNullDueDateLast() {
        Task noDueDate = createTask(1L, "期限なし", false, null, Priority.MEDIUM);
        Task later = createTask(2L, "あと", false, LocalDate.of(2026, 5, 20), Priority.MEDIUM);
        Task earlier = createTask(3L, "先", false, LocalDate.of(2026, 5, 10), Priority.MEDIUM);

        when(taskRepository.findAll()).thenReturn(List.of(noDueDate, later, earlier));

        List<Task> actual = taskService.findTasks(
                TaskFilterType.ALL,
                TaskSortType.DUE_DATE,
                "");

        assertEquals("先", actual.get(0).getTitle());
        assertEquals("あと", actual.get(1).getTitle());
        assertEquals("期限なし", actual.get(2).getTitle());
    }

    /**
     * 優先度が高い順で並び替えられることを確認します。
     */
    @Test
    void findTasks_prioritySort_returnsHighPriorityFirst() {
        Task low = createTask(1L, "低い優先度", false, null, Priority.LOW);
        Task high = createTask(2L, "高い優先度", false, null, Priority.HIGH);
        Task medium = createTask(3L, "普通の優先度", false, null, Priority.MEDIUM);

        when(taskRepository.findAll()).thenReturn(List.of(low, high, medium));

        List<Task> actual = taskService.findTasks(
                TaskFilterType.ALL,
                TaskSortType.PRIORITY,
                "");

        assertEquals("高い優先度", actual.get(0).getTitle());
        assertEquals("普通の優先度", actual.get(1).getTitle());
        assertEquals("低い優先度", actual.get(2).getTitle());
    }

    /**
     * タスク追加時に、Repositoryのsaveが呼ばれることを確認します。
     */
    @Test
    void addTask_callsSave() {
        taskService.addTask(
                "新しいタスク",
                LocalDate.of(2026, 5, 10),
                Priority.HIGH);

        verify(taskRepository).save(any(Task.class));
    }

    /**
     * 指定したIDのタスクの完了状態を切り替えられることを確認します。
     */
    @Test
    void toggleTaskDone_togglesDoneFlag() {
        Task task = createTask(1L, "切り替え対象", false, null, Priority.MEDIUM);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.toggleTaskDone(1L);

        assertTrue(task.isDone());
        verify(taskRepository).save(task);
    }

    /**
     * 指定したIDのタスクを更新できることを確認します。
     */
    @Test
    void updateTask_updatesTaskFields() {
        Task task = createTask(1L, "変更前", false, null, Priority.LOW);

        TaskUpdateForm form = new TaskUpdateForm();
        form.setId(1L);
        form.setTitle("変更後");
        form.setDueDate(LocalDate.of(2026, 5, 12));
        form.setPriority(Priority.HIGH);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        boolean updated = taskService.updateTask(form);

        assertTrue(updated);
        assertEquals("変更後", task.getTitle());
        assertEquals(LocalDate.of(2026, 5, 12), task.getDueDate());
        assertEquals(Priority.HIGH, task.getPriority());
        verify(taskRepository).save(task);
    }

    /**
     * 更新対象のタスクが見つからない場合、falseが返ることを確認します。
     */
    @Test
    void updateTask_returnsFalse_whenTaskNotFound() {
        TaskUpdateForm form = new TaskUpdateForm();
        form.setId(999L);
        form.setTitle("存在しないタスク");
        form.setPriority(Priority.MEDIUM);

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        boolean updated = taskService.updateTask(form);

        assertFalse(updated);
    }

    /**
     * タスク削除時に、RepositoryのdeleteByIdが呼ばれることを確認します。
     */
    @Test
    void deleteTask_callsDeleteById() {
        taskService.deleteTask(1L);

        verify(taskRepository).deleteById(1L);
    }

    /**
     * テスト用のTaskを作成します。
     *
     * Taskのidは自動採番想定のため、通常のsetterは用意していません。
     * テストでは並び替え確認のためにidが必要なので、ReflectionTestUtilsで値を設定しています。
     *
     * @param id タスクID
     * @param title タイトル
     * @param done 完了状態
     * @param dueDate 期限日
     * @param priority 優先度
     * @return テスト用Task
     */
    private Task createTask(
            Long id,
            String title,
            boolean done,
            LocalDate dueDate,
            Priority priority) {

        Task task = new Task(title, dueDate, priority);

        ReflectionTestUtils.setField(task, "id", id);
        task.setDone(done);

        return task;
    }
}