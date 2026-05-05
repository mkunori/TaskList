package com.mkunori.tasklist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TaskListアプリケーションの起動クラスです。
 * 
 * Spring Boot アプリは、このクラスから起動します。
 */
@SpringBootApplication
public class TaskListApplication {

    /**
     * アプリケーションを起動します。
     * 
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskListApplication.class, args);
    }
}