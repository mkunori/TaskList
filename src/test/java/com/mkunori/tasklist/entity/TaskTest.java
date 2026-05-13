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
     * 新しく作成したタスクが未完了状態になることを確認します。
     */
    @Test
    void constructor_createsUndoneTask() {
        Task task = new Task(
                "テストタスク",
                LocalDate.of(2026, 5, 10),
                Priority.MEDIUM);

        assertFalse(task.isDone());
    }

    /**
     * toggleDoneを呼ぶと、未完了から完了に切り替わることを確認します。
     */
    @Test
    void toggleDone_changesUndoneToDone() {
        Task task = new Task(
                "テストタスク",
                null,
                Priority.MEDIUM);

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
                Priority.MEDIUM);

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
                Priority.MEDIUM);

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
                Priority.HIGH);

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
                Priority.HIGH);

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
                Priority.LOW);

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
                Priority.HIGH);

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
                Priority.HIGH);

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
                Priority.MEDIUM);

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
                Priority.LOW);

        assertEquals("", task.getDueDateClass());
    }
}