package com.mkunori.tasklist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mkunori.tasklist.entity.Task;

/**
 * Taskエンティティを操作するリポジトリです。
 *
 * JpaRepositoryを継承することで、基本的なDB操作を簡単に使えます。
 * このアプリでは、ログイン機能の代わりに匿名ユーザーIDを使うため、
 * ownerIdを条件にした検索メソッドも定義しています。
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * 指定された匿名ユーザーIDに紐づくタスクをすべて取得します。
     *
     * @param ownerId 匿名ユーザーID
     * @return 指定された匿名ユーザーのタスク一覧
     */
    List<Task> findByOwnerId(String ownerId);

    /**
     * 指定された匿名ユーザーIDに紐づくタスクを、IDの昇順で取得します。
     *
     * IDは自動採番されるため、IDの昇順にすると登録順になります。
     *
     * @param ownerId 匿名ユーザーID
     * @return 登録順のタスク一覧
     */
    List<Task> findByOwnerIdOrderByIdAsc(String ownerId);

    /**
     * 指定された匿名ユーザーIDと完了状態に一致するタスクを取得します。
     *
     * @param ownerId 匿名ユーザーID
     * @param done    完了状態。trueなら完了済み、falseなら未完了
     * @return 条件に一致するタスク一覧
     */
    List<Task> findByOwnerIdAndDone(String ownerId, boolean done);

    /**
     * 指定された匿名ユーザーIDと完了状態に一致するタスクを、IDの昇順で取得します。
     *
     * @param ownerId 匿名ユーザーID
     * @param done    完了状態。trueなら完了済み、falseなら未完了
     * @return 登録順のタスク一覧
     */
    List<Task> findByOwnerIdAndDoneOrderByIdAsc(String ownerId, boolean done);
    
    /**
     * 指定された匿名ユーザーIDに紐づき、タイトルにキーワードを含むタスクを取得します。
     *
     * 大文字小文字を区別せずに検索します。
     *
     * @param ownerId 匿名ユーザーID
     * @param keyword 検索キーワード
     * @return 条件に一致するタスク一覧
     */
    List<Task> findByOwnerIdAndTitleContainingIgnoreCase(String ownerId, String keyword);
   
    /**
     * 指定された匿名ユーザーIDに紐づき、タイトルにキーワードを含むタスクを、
     * IDの昇順で取得します。
     *
     * 大文字小文字を区別せずに検索します。
     *
     * @param ownerId 匿名ユーザーID
     * @param keyword 検索キーワード
     * @return 登録順のタスク一覧
     */
    List<Task> findByOwnerIdAndTitleContainingIgnoreCaseOrderByIdAsc(String ownerId, String keyword);
    
   /**
     * 指定された匿名ユーザーID、完了状態に一致し、
     * タイトルにキーワードを含むタスクを取得します。
     *
     * 大文字小文字を区別せずに検索します。
     *
     * @param ownerId 匿名ユーザーID
     * @param done    完了状態。trueなら完了済み、falseなら未完了
     * @param keyword 検索キーワード
     * @return 条件に一致するタスク一覧
     */
    List<Task> findByOwnerIdAndDoneAndTitleContainingIgnoreCase(String ownerId, boolean done, String keyword);

    /**
     * 指定された匿名ユーザーID、完了状態に一致し、
     * タイトルにキーワードを含むタスクを、IDの昇順で取得します。
     *
     * 大文字小文字を区別せずに検索します。
     *
     * @param ownerId 匿名ユーザーID
     * @param done 完了状態。trueなら完了済み、falseなら未完了
     * @param keyword 検索キーワード
     * @return 登録順のタスク一覧
     */
    List<Task> findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByIdAsc(String ownerId, boolean done, String keyword);

    /**
     * 指定されたタスクIDと匿名ユーザーIDに一致するタスクを取得します。
     *
     * IDだけで取得すると、他の匿名ユーザーのタスクを操作できてしまう可能性があります。
     * そのため、更新・削除・完了切り替えでは ownerId も条件に含めます。
     *
     * @param id      タスクID
     * @param ownerId 匿名ユーザーID
     * @return 条件に一致するタスク。存在しない場合は空のOptional
     */
    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.ownerId = :ownerId")
    Optional<Task> findByIdAndOwnerId(Long id, String ownerId);

    /**
     * 指定されたタスクIDと匿名ユーザーIDに一致するタスクを削除します。
     *
     * @param id      タスクID
     * @param ownerId 匿名ユーザーID
     */
    void deleteByIdAndOwnerId(Long id, String ownerId);

    /**
     * 指定された匿名ユーザーIDに紐づくタスクを、期限が近い順で取得します。
     *
     * 期限なしのタスクは最後に表示します。
     *
     * @param ownerId 匿名ユーザーID
     * @return 期限が近い順のタスク一覧
     */
    @Query("""
            SELECT task
            FROM Task task
            WHERE task.ownerId = :ownerId
            ORDER BY
                CASE WHEN task.dueDate IS NULL THEN 1 ELSE 0 END,
                task.dueDate ASC,
                task.id ASC
            """)
    List<Task> findByOwnerIdOrderByDueDateAscNullsLast(
            @Param("ownerId") String ownerId);

    /**
     * 指定された匿名ユーザーIDと完了状態に一致するタスクを、期限が近い順で取得します。
     *
     * 期限なしのタスクは最後に表示します。
     *
     * @param ownerId 匿名ユーザーID
     * @param done 完了状態。trueなら完了済み、falseなら未完了
     * @return 期限が近い順のタスク一覧
     */
    @Query("""
            SELECT task
            FROM Task task
            WHERE task.ownerId = :ownerId
            AND task.done = :done
            ORDER BY
                CASE WHEN task.dueDate IS NULL THEN 1 ELSE 0 END,
                task.dueDate ASC,
                task.id ASC
            """)
    List<Task> findByOwnerIdAndDoneOrderByDueDateAscNullsLast(
            @Param("ownerId") String ownerId,
            @Param("done") boolean done);

    /**
     * 指定された匿名ユーザーIDに紐づき、タイトルにキーワードを含むタスクを、
     * 期限が近い順で取得します。
     *
     * 大文字小文字を区別せずに検索します。
     * 期限なしのタスクは最後に表示します。
     *
     * @param ownerId 匿名ユーザーID
     * @param keyword 検索キーワード
     * @return 期限が近い順のタスク一覧
     */
    @Query("""
            SELECT task
            FROM Task task
            WHERE task.ownerId = :ownerId
            AND LOWER(task.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY
                CASE WHEN task.dueDate IS NULL THEN 1 ELSE 0 END,
                task.dueDate ASC,
                task.id ASC
            """)
    List<Task> findByOwnerIdAndTitleContainingIgnoreCaseOrderByDueDateAscNullsLast(
            @Param("ownerId") String ownerId,
            @Param("keyword") String keyword);
    
    /**
     * 指定された匿名ユーザーID、完了状態に一致し、
     * タイトルにキーワードを含むタスクを、期限が近い順で取得します。
     *
     * 大文字小文字を区別せずに検索します。
     * 期限なしのタスクは最後に表示します。
     *
     * @param ownerId 匿名ユーザーID
     * @param done 完了状態。trueなら完了済み、falseなら未完了
     * @param keyword 検索キーワード
     * @return 期限が近い順のタスク一覧
     */
    @Query("""
            SELECT task
            FROM Task task
            WHERE task.ownerId = :ownerId
            AND task.done = :done
            AND LOWER(task.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY
                CASE WHEN task.dueDate IS NULL THEN 1 ELSE 0 END,
                task.dueDate ASC,
                task.id ASC
            """)
    List<Task> findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByDueDateAscNullsLast(
            @Param("ownerId") String ownerId,
            @Param("done") boolean done,
            @Param("keyword") String keyword);
    
    /**
     * 指定された匿名ユーザーIDに紐づくタスクを、優先度が高い順で取得します。
     *
     * HIGH、MEDIUM、LOW の順で表示します。
     * 優先度が未設定のタスクは最後に表示します。
     *
     * @param ownerId 匿名ユーザーID
     * @return 優先度が高い順のタスク一覧
     */
    @Query("""
            SELECT task
            FROM Task task
            WHERE task.ownerId = :ownerId
            ORDER BY
                CASE task.priority
                    WHEN com.mkunori.tasklist.entity.Priority.HIGH THEN 1
                    WHEN com.mkunori.tasklist.entity.Priority.MEDIUM THEN 2
                    WHEN com.mkunori.tasklist.entity.Priority.LOW THEN 3
                    ELSE 4
                END,
                task.id ASC
            """)
    List<Task> findByOwnerIdOrderByPriorityHighFirst(
            @Param("ownerId") String ownerId);
    
    /**
     * 指定された匿名ユーザーIDと完了状態に一致するタスクを、
     * 優先度が高い順で取得します。
     *
     * HIGH、MEDIUM、LOW の順で表示します。
     * 優先度が未設定のタスクは最後に表示します。
     *
     * @param ownerId 匿名ユーザーID
     * @param done 完了状態。trueなら完了済み、falseなら未完了
     * @return 優先度が高い順のタスク一覧
     */
    @Query("""
            SELECT task
            FROM Task task
            WHERE task.ownerId = :ownerId
            AND task.done = :done
            ORDER BY
                CASE task.priority
                    WHEN com.mkunori.tasklist.entity.Priority.HIGH THEN 1
                    WHEN com.mkunori.tasklist.entity.Priority.MEDIUM THEN 2
                    WHEN com.mkunori.tasklist.entity.Priority.LOW THEN 3
                    ELSE 4
                END,
                task.id ASC
            """)
    List<Task> findByOwnerIdAndDoneOrderByPriorityHighFirst(
            @Param("ownerId") String ownerId,
            @Param("done") boolean done);
    
    /**
     * 指定された匿名ユーザーID、完了状態に一致し、
     * タイトルにキーワードを含むタスクを、優先度が高い順で取得します。
     *
     * 大文字小文字を区別せずに検索します。
     * HIGH、MEDIUM、LOW の順で表示します。
     *
     * @param ownerId 匿名ユーザーID
     * @param done 完了状態。trueなら完了済み、falseなら未完了
     * @param keyword 検索キーワード
     * @return 優先度が高い順のタスク一覧
     */
    @Query("""
            SELECT task
            FROM Task task
            WHERE task.ownerId = :ownerId
            AND task.done = :done
            AND LOWER(task.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY
                CASE task.priority
                    WHEN com.mkunori.tasklist.entity.Priority.HIGH THEN 1
                    WHEN com.mkunori.tasklist.entity.Priority.MEDIUM THEN 2
                    WHEN com.mkunori.tasklist.entity.Priority.LOW THEN 3
                    ELSE 4
                END,
                task.id ASC
            """)
    List<Task> findByOwnerIdAndDoneAndTitleContainingIgnoreCaseOrderByPriorityHighFirst(
            @Param("ownerId") String ownerId,
            @Param("done") boolean done,
            @Param("keyword") String keyword);

        /**
         * 指定された匿名ユーザーIDに紐づき、タイトルにキーワードを含むタスクを、
         * 優先度が高い順で取得します。
         *
         * 大文字小文字を区別せずに検索します。
         * HIGH、MEDIUM、LOW の順で表示します。
         *
         * @param ownerId 匿名ユーザーID
         * @param keyword 検索キーワード
         * @return 優先度が高い順のタスク一覧
         */
        @Query("""
                SELECT task
                FROM Task task
                WHERE task.ownerId = :ownerId
                AND LOWER(task.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                ORDER BY
                CASE task.priority
                        WHEN com.mkunori.tasklist.entity.Priority.HIGH THEN 1
                        WHEN com.mkunori.tasklist.entity.Priority.MEDIUM THEN 2
                        WHEN com.mkunori.tasklist.entity.Priority.LOW THEN 3
                        ELSE 4
                END,
                task.id ASC
                """)
        List<Task> findByOwnerIdAndTitleContainingIgnoreCaseOrderByPriorityHighFirst(
                @Param("ownerId") String ownerId,
                @Param("keyword") String keyword);
}