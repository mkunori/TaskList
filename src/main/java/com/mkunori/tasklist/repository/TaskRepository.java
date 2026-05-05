package com.mkunori.tasklist.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mkunori.tasklist.entity.Task;

/**
 * Task エンティティを操作するリポジトリです。
 * 
 * JpaRepository を継承することで、基本的なDB操作を簡単に使えます。
 */
public interface TaskRepository extends JpaRepository<Task, Long> {
}