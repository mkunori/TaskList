package com.mkunori.tasklist.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.mkunori.tasklist.entity.Task;
import com.mkunori.tasklist.form.TaskForm;
import com.mkunori.tasklist.repository.TaskRepository;

import jakarta.validation.Valid;

/**
 * タスク一覧画面の表示、タスク追加、タスク削除、完了状態の切り替えを担当するコントローラです。
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
     *
     * @param taskRepository タスクリポジトリ
     */
    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * タスク一覧画面を表示します。
     *
     * DBからすべてのタスクを取得し、HTMLへ渡します。
     *
     * @param model 画面へ値を渡すためのオブジェクト
     * @return 表示するテンプレート名
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
     * @Validを付けることで、TaskFormに書いた入力チェックが実行されます。
     * BindingResultには、入力チェックの結果が入ります。
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
            model.addAttribute("tasks", taskRepository.findAll());

            // redirectではなくtasksを返すことで、エラー情報を画面に表示できる
            return "tasks";
        }

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
     *
     * @param id 削除するタスクのID
     * @return 一覧画面へリダイレクト
     */
    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id) {
        // URLから受け取ったIDを指定して、DBからタスクを削除する
        taskRepository.deleteById(id);

        // 削除後は一覧画面へリダイレクトする
        return "redirect:/";
    }

    /**
     * 指定されたIDのタスクの完了状態を切り替えます。
     *
     * 未完了のタスクなら完了にし、完了済みのタスクなら未完了に戻します。
     *
     * @param id 完了状態を切り替えるタスクのID
     * @return 一覧画面へリダイレクト
     */
    @PostMapping("/tasks/{id}/toggle")
    public String toggleTaskDone(@PathVariable Long id) {
        // IDを使ってDBからタスクを1件探す
        Optional<Task> optionalTask = taskRepository.findById(id);

        // タスクが見つかった場合だけ、完了状態を切り替える
        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();

            // Entityに用意したメソッドで、true/falseを反転する
            task.toggleDone();

            // 変更したEntityを保存する
            taskRepository.save(task);
        }

        // 処理後は一覧画面へ戻る
        return "redirect:/";
    }
}