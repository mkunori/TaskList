package com.example.tasklist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.tasklist.entity.Task;
import com.example.tasklist.form.TaskForm;
import com.example.tasklist.repository.TaskRepository;

/**
 * タスク一覧画面の表示と、タスク追加を担当するコントローラです。
 */
@Controller
public class TaskController {

    /**
     * タスクをDBから読み書きするためのリポジトリです。
     */
    private final TaskRepository taskRepository;

    /**
     * リポジトリを受け取ってコントローラを作成します。
     * 
     * @param taskRepository タスクリポジトリ
     */
    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * タスク一覧画面を表示します。
     * 
     * @param model 画面に値を渡すためのオブジェクト
     * @return 表示するテンプレート名
     */
    @GetMapping("/")
    public String showTaskList(Model model) {
        // DBから全タスクを取得して画面へ渡す
        model.addAttribute("tasks", taskRepository.findAll());

        // フォーム表示用の空オブジェクトを渡す
        model.addAttribute("taskForm", new TaskForm());

        return "tasks";
    }

    /**
     * 入力されたタスクを保存します。
     * 
     * @param taskForm 画面から送信された入力値
     * @return 一覧画面へリダイレクト
     */
    @PostMapping("/tasks")
    public String addTask(@ModelAttribute TaskForm taskForm) {
        // フォームの値を使って Entity を作る
        Task task = new Task(taskForm.getTitle());

        // DBへ保存する
        taskRepository.save(task);

        // 保存後は一覧画面へ戻す
        return "redirect:/";
    }
}