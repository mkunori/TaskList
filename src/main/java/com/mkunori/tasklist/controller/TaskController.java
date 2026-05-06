package com.mkunori.tasklist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.mkunori.tasklist.entity.Task;
import com.mkunori.tasklist.form.TaskForm;
import com.mkunori.tasklist.repository.TaskRepository;

/**
 * タスク一覧画面の表示、タスク追加、タスク削除を担当するコントローラです。
 *
 * Controllerは、ブラウザからのリクエストを受け取り、
 * 必要な処理を実行して、次に表示する画面を決めます。
 */
@Controller
public class TaskController {

    /**
     * タスクをDBから読み書きするためのリポジトリです。
     */
    private final TaskRepository taskRepository;

    /**
     * コンストラクタです。
     *
     * SpringがTaskRepositoryを自動で渡してくれます。
     */
    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * タスク一覧画面を表示します。
     *
     * DBからすべてのタスクを取得し、HTMLへ渡します。
     */
    @GetMapping("/")
    public String showTaskList(Model model) {
        // DBから全タスクを取得して、tasks という名前でHTMLへ渡す
        model.addAttribute("tasks", taskRepository.findAll());

        // タスク追加フォーム用の空オブジェクトをHTMLへ渡す
        model.addAttribute("taskForm", new TaskForm());

        // src/main/resources/templates/tasks.html を表示する
        return "tasks";
    }

    /**
     * 入力されたタスクをDBに保存します。
     *
     * HTMLフォームから送信されたTaskFormを受け取り、
     * DB保存用のTaskエンティティに変換して保存します。
     */
    @PostMapping("/tasks")
    public String addTask(@ModelAttribute TaskForm taskForm) {
        // フォーム入力値から、DB保存用のEntityを作成する
        Task task = new Task(taskForm.getTitle());

        // Repositoryを使ってDBに保存する
        taskRepository.save(task);

        // 保存後は一覧画面へリダイレクトする
        return "redirect:/";
    }

    /**
     * 指定されたIDのタスクを削除します。
     *
     * URLに含まれるIDを受け取り、そのIDに対応するタスクをDBから削除します。
     */
    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id) {
        // URLから受け取ったIDを指定して、DBからタスクを削除する
        taskRepository.deleteById(id);

        // 削除後は一覧画面へリダイレクトする
        return "redirect:/";
    }
}