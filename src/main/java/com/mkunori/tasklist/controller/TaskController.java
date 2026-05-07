package com.mkunori.tasklist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.mkunori.tasklist.form.TaskForm;
import com.mkunori.tasklist.service.TaskService;

import jakarta.validation.Valid;

/**
 * タスク一覧画面の表示、タスク追加、タスク削除、完了状態の切り替えを担当するコントローラです。
 *
 * Controllerは、ブラウザからのリクエストを受け取り、
 * Serviceへ処理を依頼して、次に表示する画面を決めます。
 */
@Controller
public class TaskController {

    /**
     * タスクに関する処理を担当するサービスです。
     */
    private final TaskService taskService;

    /**
     * コンストラクタです。
     *
     * SpringがTaskServiceを自動で渡してくれます。
     *
     * @param taskService タスクサービス
     */
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * タスク一覧画面を表示します。
     *
     * Serviceからすべてのタスクを取得し、HTMLへ渡します。
     *
     * @param model 画面へ値を渡すためのオブジェクト
     * @return 表示するテンプレート名
     */
    @GetMapping("/")
    public String showTaskList(Model model) {
        // Serviceから全タスクを取得して、tasks という名前でHTMLへ渡す
        model.addAttribute("tasks", taskService.findAllTasks());

        // タスク追加フォーム用の空オブジェクトをHTMLへ渡す
        model.addAttribute("taskForm", new TaskForm());

        // src/main/resources/templates/tasks.html を表示する
        return "tasks";
    }

    /**
     * 入力されたタスクをDBに保存します。
     *
     * 入力チェックに成功した場合だけ、Serviceへタスク追加を依頼します。
     *
     * @param taskForm 画面から送信された入力値
     * @param bindingResult 入力チェックの結果
     * @param model 画面へ値を渡すためのオブジェクト
     * @return エラーがあれば一覧画面、成功すれば一覧画面へリダイレクト
     */
    @PostMapping("/tasks")
    public String addTask(
            @Valid @ModelAttribute TaskForm taskForm,
            BindingResult bindingResult,
            Model model) {

        // 入力チェックでエラーがある場合は、保存せずに一覧画面へ戻す
        if (bindingResult.hasErrors()) {
            // 一覧画面を再表示するため、タスク一覧をもう一度HTMLへ渡す
            model.addAttribute("tasks", taskService.findAllTasks());

            // redirectではなくtasksを返すことで、エラー情報を画面に表示できる
            return "tasks";
        }

        // Serviceにタスク追加処理を依頼する
        taskService.addTask(taskForm.getTitle());

        // 保存後は一覧画面へリダイレクトする
        return "redirect:/";
    }

    /**
     * 指定されたIDのタスクを削除します。
     *
     * URLに含まれるIDを受け取り、Serviceへ削除処理を依頼します。
     *
     * @param id 削除するタスクのID
     * @return 一覧画面へリダイレクト
     */
    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id) {
        // Serviceに削除処理を依頼する
        taskService.deleteTask(id);

        // 削除後は一覧画面へリダイレクトする
        return "redirect:/";
    }

    /**
     * 指定されたIDのタスクの完了状態を切り替えます。
     *
     * URLに含まれるIDを受け取り、Serviceへ完了状態の切り替えを依頼します。
     *
     * @param id 完了状態を切り替えるタスクのID
     * @return 一覧画面へリダイレクト
     */
    @PostMapping("/tasks/{id}/toggle")
    public String toggleTaskDone(@PathVariable Long id) {
        // Serviceに完了状態の切り替え処理を依頼する
        taskService.toggleTaskDone(id);

        // 処理後は一覧画面へ戻る
        return "redirect:/";
    }
}