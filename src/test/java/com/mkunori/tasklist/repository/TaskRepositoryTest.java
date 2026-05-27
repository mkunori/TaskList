package com.mkunori.tasklist.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.mkunori.tasklist.entity.Priority;
import com.mkunori.tasklist.entity.Task;

/**
 * TaskRepositoryのテストです。
 *
 * Repository層のテストでは、実際にJPAを使ってDBへ保存・検索を行います。
 * Service層は使わず、Repositoryのメソッド名クエリや@Queryが期待通りに動くことを確認します。
 */
@DataJpaTest
class TaskRepositoryTest {

    /**
     * テスト対象のRepositoryです。
     */
    @Autowired
    private TaskRepository taskRepository;

    /**
     * テスト用の匿名ユーザーIDです。
     */
    private static final String OWNER_A = "owner-a";

    /**
     * 別ブラウザ・別ユーザーを表すテスト用の匿名ユーザーIDです。
     */
    private static final String OWNER_B = "owner-b";

    /**
     * ownerIdが一致するタスクだけを、IDの昇順で取得できることを確認します。
     *
     * IDは自動採番されるため、IDの昇順は登録順に近い並びになります。
     */
    @Test
    void findByOwnerIdOrderByIdAsc_returnsOnlyOwnTasksOrderById() {
        Task ownerATask1 = new Task("Aのタスク1", null, Priority.MEDIUM, OWNER_A);
        Task ownerBTask = new Task("Bのタスク", null, Priority.HIGH, OWNER_B);
        Task ownerATask2 = new Task("Aのタスク2", null, Priority.LOW, OWNER_A);

        taskRepository.save(ownerATask1);
        taskRepository.save(ownerBTask);
        taskRepository.save(ownerATask2);

        List<Task> actual = taskRepository.findByOwnerIdOrderByIdAsc(OWNER_A);

        assertEquals(2, actual.size());
        assertEquals("Aのタスク1", actual.get(0).getTitle());
        assertEquals("Aのタスク2", actual.get(1).getTitle());
    }

    /**
     * ownerIdと完了状態が一致するタスクだけを取得できることを確認します。
     */
    @Test
    void findByOwnerIdAndDoneOrderByIdAsc_returnsOnlyMatchingDoneTasks() {
        Task undoneTask = new Task("未完了タスク", null, Priority.MEDIUM, OWNER_A);
        Task doneTask = new Task("完了済みタスク", null, Priority.MEDIUM, OWNER_A);
        doneTask.setDone(true);

        Task otherOwnerTask = new Task("別ユーザーの未完了タスク", null, Priority.MEDIUM, OWNER_B);

        taskRepository.save(undoneTask);
        taskRepository.save(doneTask);
        taskRepository.save(otherOwnerTask);

        List<Task> actual = taskRepository.findByOwnerIdAndDoneOrderByIdAsc(OWNER_A, false);

        assertEquals(1, actual.size());
        assertEquals("未完了タスク", actual.get(0).getTitle());
    }

    /**
     * 期限が近い順で取得したとき、期限なしのタスクが最後に並ぶことを確認します。
     */
    @Test
    void findByOwnerIdOrderByDueDateAscNullsLast_putsNullDueDateLast() {
        Task noDueDateTask = new Task("期限なし", null, Priority.MEDIUM, OWNER_A);
        Task laterTask = new Task("後の期限", LocalDate.of(2026, 5, 20), Priority.MEDIUM, OWNER_A);
        Task earlierTask = new Task("先の期限", LocalDate.of(2026, 5, 10), Priority.MEDIUM, OWNER_A);

        taskRepository.save(noDueDateTask);
        taskRepository.save(laterTask);
        taskRepository.save(earlierTask);

        List<Task> actual = taskRepository.findByOwnerIdOrderByDueDateAscNullsLast(OWNER_A);

        assertEquals(3, actual.size());
        assertEquals("先の期限", actual.get(0).getTitle());
        assertEquals("後の期限", actual.get(1).getTitle());
        assertEquals("期限なし", actual.get(2).getTitle());
    }

    /**
     * idとownerIdの両方が一致する場合だけ、タスクを取得できることを確認します。
     */
    @Test
    void findByIdAndOwnerId_returnsTaskOnlyWhenOwnerMatches() {
        Task task = new Task("Aのタスク", null, Priority.MEDIUM, OWNER_A);
        Task savedTask = taskRepository.save(task);

        Optional<Task> ownTask = taskRepository.findByIdAndOwnerId(savedTask.getId(), OWNER_A);
        Optional<Task> otherOwnerTask = taskRepository.findByIdAndOwnerId(savedTask.getId(), OWNER_B);

        assertTrue(ownTask.isPresent());
        assertTrue(otherOwnerTask.isEmpty());
    }

    /**
     * ownerIdが一致し、タイトルにキーワードを含むタスクだけを取得できることを確認します。
     *
     * 大文字小文字を区別しない検索であることも確認します。
     */
    @Test
    void findByOwnerIdAndTitleContainingIgnoreCaseOrderByIdAsc_returnsMatchedOwnTasks() {
        Task springTask1 = new Task("Spring Bootを学ぶ", null, Priority.MEDIUM, OWNER_A);
        Task javaTask = new Task("Java Silverを復習する", null, Priority.HIGH, OWNER_A);
        Task springTask2 = new Task("spring data jpaを確認する", null, Priority.LOW, OWNER_A);
        Task otherOwnerTask = new Task("Spring Securityを学ぶ", null, Priority.HIGH, OWNER_B);

        taskRepository.save(springTask1);
        taskRepository.save(javaTask);
        taskRepository.save(springTask2);
        taskRepository.save(otherOwnerTask);

        List<Task> actual =
                taskRepository.findByOwnerIdAndTitleContainingIgnoreCaseOrderByIdAsc(
                        OWNER_A,
                        "Spring");

        assertEquals(2, actual.size());
        assertEquals("Spring Bootを学ぶ", actual.get(0).getTitle());
        assertEquals("spring data jpaを確認する", actual.get(1).getTitle());
    }

    /**
     * ownerId、完了状態、キーワードがすべて一致するタスクだけを取得できることを確認します。
     */
    @Test
    void findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByIdAsc_returnsMatchedOwnDoneStateTasks() {
        Task undoneSpringTask = new Task("Spring Bootを学ぶ", null, Priority.MEDIUM, OWNER_A);

        Task doneSpringTask = new Task("Spring JPAを復習する", null, Priority.HIGH, OWNER_A);
        doneSpringTask.setDone(true);

        Task undoneJavaTask = new Task("Javaを復習する", null, Priority.LOW, OWNER_A);

        Task otherOwnerSpringTask = new Task("Spring MVCを確認する", null, Priority.HIGH, OWNER_B);

        taskRepository.save(undoneSpringTask);
        taskRepository.save(doneSpringTask);
        taskRepository.save(undoneJavaTask);
        taskRepository.save(otherOwnerSpringTask);

        List<Task> actual =
                taskRepository.findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByIdAsc(
                        OWNER_A,
                        false,
                        "Spring");

        assertEquals(1, actual.size());
        assertEquals("Spring Bootを学ぶ", actual.get(0).getTitle());
    }

    /**
     * 優先度が高い順で取得できることを確認します。
     *
     * HIGH、MEDIUM、LOW の順で並ぶことを確認します。
     */
    @Test
    void findByOwnerIdOrderByPriorityHighFirst_returnsHighPriorityFirst() {
        Task lowTask = new Task("低優先度タスク", null, Priority.LOW, OWNER_A);
        Task highTask = new Task("高優先度タスク", null, Priority.HIGH, OWNER_A);
        Task mediumTask = new Task("中優先度タスク", null, Priority.MEDIUM, OWNER_A);
        Task otherOwnerHighTask = new Task("別ユーザーの高優先度タスク", null, Priority.HIGH, OWNER_B);

        taskRepository.save(lowTask);
        taskRepository.save(highTask);
        taskRepository.save(mediumTask);
        taskRepository.save(otherOwnerHighTask);

        List<Task> actual = taskRepository.findByOwnerIdOrderByPriorityHighFirst(OWNER_A);

        assertEquals(3, actual.size());
        assertEquals("高優先度タスク", actual.get(0).getTitle());
        assertEquals("中優先度タスク", actual.get(1).getTitle());
        assertEquals("低優先度タスク", actual.get(2).getTitle());
    }

    /**
     * ownerIdと完了状態が一致するタスクを、優先度が高い順で取得できることを確認します。
     */
    @Test
    void findByOwnerIdAndDoneOrderByPriorityHighFirst_returnsMatchingDoneTasksOrderByPriority() {
        Task lowUndoneTask = new Task("低優先度の未完了タスク", null, Priority.LOW, OWNER_A);
        Task highUndoneTask = new Task("高優先度の未完了タスク", null, Priority.HIGH, OWNER_A);

        Task highDoneTask = new Task("高優先度の完了済みタスク", null, Priority.HIGH, OWNER_A);
        highDoneTask.setDone(true);

        Task otherOwnerHighTask = new Task("別ユーザーの高優先度タスク", null, Priority.HIGH, OWNER_B);

        taskRepository.save(lowUndoneTask);
        taskRepository.save(highUndoneTask);
        taskRepository.save(highDoneTask);
        taskRepository.save(otherOwnerHighTask);

        List<Task> actual =
                taskRepository.findByOwnerIdAndDoneOrderByPriorityHighFirst(
                        OWNER_A,
                        false);

        assertEquals(2, actual.size());
        assertEquals("高優先度の未完了タスク", actual.get(0).getTitle());
        assertEquals("低優先度の未完了タスク", actual.get(1).getTitle());
    }

    /**
     * ownerIdとキーワードが一致するタスクを、優先度が高い順で取得できることを確認します。
     */
    @Test
    void findByOwnerIdAndTitleContainingIgnoreCaseOrderByPriorityHighFirst_returnsMatchedTasksOrderByPriority() {
        Task lowSpringTask = new Task("Spring低優先度", null, Priority.LOW, OWNER_A);
        Task highSpringTask = new Task("Spring高優先度", null, Priority.HIGH, OWNER_A);
        Task mediumJavaTask = new Task("Java中優先度", null, Priority.MEDIUM, OWNER_A);
        Task otherOwnerHighSpringTask = new Task("Spring別ユーザー高優先度", null, Priority.HIGH, OWNER_B);

        taskRepository.save(lowSpringTask);
        taskRepository.save(highSpringTask);
        taskRepository.save(mediumJavaTask);
        taskRepository.save(otherOwnerHighSpringTask);

        List<Task> actual =
                taskRepository.findByOwnerIdAndTitleContainingIgnoreCaseOrderByPriorityHighFirst(
                        OWNER_A,
                        "Spring");

        assertEquals(2, actual.size());
        assertEquals("Spring高優先度", actual.get(0).getTitle());
        assertEquals("Spring低優先度", actual.get(1).getTitle());
    }

    /**
     * idとownerIdが一致するタスクだけを削除できることを確認します。
     *
     * ownerIdが一致しない場合は、同じidを指定しても削除されません。
     */
    @Test
    void deleteByIdAndOwnerId_deletesTaskOnlyWhenOwnerMatches() {
        Task task = new Task("削除対象タスク", null, Priority.MEDIUM, OWNER_A);
        Task savedTask = taskRepository.save(task);

        taskRepository.deleteByIdAndOwnerId(savedTask.getId(), OWNER_B);

        Optional<Task> taskAfterWrongOwnerDelete =
                taskRepository.findById(savedTask.getId());

        assertTrue(taskAfterWrongOwnerDelete.isPresent());

        taskRepository.deleteByIdAndOwnerId(savedTask.getId(), OWNER_A);

        Optional<Task> taskAfterCorrectOwnerDelete =
                taskRepository.findById(savedTask.getId());

        assertTrue(taskAfterCorrectOwnerDelete.isEmpty());
    }
}