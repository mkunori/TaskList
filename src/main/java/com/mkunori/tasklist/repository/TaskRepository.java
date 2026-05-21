package com.mkunori.tasklist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
     * 指定された匿名ユーザーIDと完了状態に一致するタスクを取得します。
     *
     * @param ownerId 匿名ユーザーID
     * @param done    完了状態。trueなら完了済み、falseなら未完了
     * @return 条件に一致するタスク一覧
     */
    List<Task> findByOwnerIdAndDone(String ownerId, boolean done);

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
}