package com.mkunori.tasklist.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Taskエンティティの単体テストです。
 *
 * Taskが持つ基本的な状態や、期限表示用メソッドの動作を確認します。
 * DBには接続せず、Taskクラス単体の振る舞いだけをテストします。
 */
class TaskTest {

    /**
     * テスト用の匿名ユーザーIDです。
     */
    private static final String TEST_OWNER_ID = "test-owner";

    /**
     * 新しく作成したタスクが未完了状態になることを確認します。
     */
    @Test
    void constructor_createsUndoneTask() {
        Task task = new Task(
                "テストタスク",
                LocalDate.of(2026, 5, 10),
                Priority.MEDIUM,
                TEST_OWNER_ID);

        assertFalse(task.isDone());
    }

    /**
     * 新しく作成したタスクに所有者IDが設定されることを確認します。
     */
    @Test
    void constructor_setsOwnerId() {
        Task task = new Task(
                "テストタスク",
                LocalDate.of(2026, 5, 10),
                Priority.MEDIUM,
                TEST_OWNER_ID);

        assertEquals(TEST_OWNER_ID, task.getOwnerId());
    }

    /**
     * toggleDoneを呼ぶと、未完了から完了に切り替わることを確認します。
     */
    @Test
    void toggleDone_changesUndoneToDone() {
        Task task = new Task(
                "テストタスク",
                null,
                Priority.MEDIUM,
                TEST_OWNER_ID);

        task.toggleDone();

        assertTrue(task.isDone());
    }

    /**
     * toggleDoneを2回呼ぶと、未完了に戻ることを確認します。
     */
    @Test
    void toggleDone_twice_changesBackToUndone() {
        Task task = new Task(
                "テストタスク",
                null,
                Priority.MEDIUM,
                TEST_OWNER_ID);

        task.toggleDone();
        task.toggleDone();

        assertFalse(task.isDone());
    }

    /**
     * 期限日が未設定の場合、「期限なし」と表示されることを確認します。
     */
    @Test
    void getDueDateLabel_returnsNoDueDate_whenDueDateIsNull() {
        Task task = new Task(
                "期限なしタスク",
                null,
                Priority.MEDIUM,
                TEST_OWNER_ID);

        assertEquals("期限なし", task.getDueDateLabel());
    }

    /**
     * 期限日が過去の場合、「期限切れ」と表示されることを確認します。
     */
    @Test
    void getDueDateLabel_returnsOverdue_whenDueDateIsPast() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        Task task = new Task(
                "期限切れタスク",
                yesterday,
                Priority.HIGH,
                TEST_OWNER_ID);

        assertEquals("期限: " + yesterday + "（期限切れ）", task.getDueDateLabel());
    }

    /**
     * 期限日が今日の場合、「今日」と表示されることを確認します。
     */
    @Test
    void getDueDateLabel_returnsToday_whenDueDateIsToday() {
        LocalDate today = LocalDate.now();

        Task task = new Task(
                "今日が期限のタスク",
                today,
                Priority.HIGH,
                TEST_OWNER_ID);

        assertEquals("期限: " + today + "（今日）", task.getDueDateLabel());
    }

    /**
     * 期限日が未来の場合、通常の期限表示になることを確認します。
     */
    @Test
    void getDueDateLabel_returnsNormalDueDate_whenDueDateIsFuture() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        Task task = new Task(
                "未来期限のタスク",
                tomorrow,
                Priority.LOW,
                TEST_OWNER_ID);

        assertEquals("期限: " + tomorrow, task.getDueDateLabel());
    }

    /**
     * 期限日が過去の場合、期限切れ用のCSSクラスが返ることを確認します。
     */
    @Test
    void getDueDateClass_returnsOverdueClass_whenDueDateIsPast() {
        Task task = new Task(
                "期限切れタスク",
                LocalDate.now().minusDays(1),
                Priority.HIGH,
                TEST_OWNER_ID);

        assertEquals("due-overdue", task.getDueDateClass());
    }

    /**
     * 期限日が今日の場合、今日が期限であることを示すCSSクラスが返ることを確認します。
     */
    @Test
    void getDueDateClass_returnsTodayClass_whenDueDateIsToday() {
        Task task = new Task(
                "今日が期限のタスク",
                LocalDate.now(),
                Priority.HIGH,
                TEST_OWNER_ID);

        assertEquals("due-today", task.getDueDateClass());
    }

    /**
     * 期限日が未設定の場合、CSSクラスとして空文字が返ることを確認します。
     */
    @Test
    void getDueDateClass_returnsEmptyString_whenDueDateIsNull() {
        Task task = new Task(
                "期限なしタスク",
                null,
                Priority.MEDIUM,
                TEST_OWNER_ID);

        assertEquals("", task.getDueDateClass());
    }

    /**
     * 期限日が未来の場合、CSSクラスとして空文字が返ることを確認します。
     */
    @Test
    void getDueDateClass_returnsEmptyString_whenDueDateIsFuture() {
        Task task = new Task(
                "未来期限のタスク",
                LocalDate.now().plusDays(1),
                Priority.LOW,
                TEST_OWNER_ID);

        assertEquals("", task.getDueDateClass());
    }
}