package com.mkunori.tasklist.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mkunori.tasklist.entity.Priority;
import com.mkunori.tasklist.form.TaskForm;
import com.mkunori.tasklist.form.TaskUpdateForm;
import com.mkunori.tasklist.service.TaskFilterType;
import com.mkunori.tasklist.service.TaskService;
import com.mkunori.tasklist.service.TaskSortType;

import jakarta.validation.Valid;

/**
 * タスク一覧画面の表示、タスク追加、更新、削除、完了状態の切り替えを担当するコントローラです。
 *
 * 一覧画面では、表示条件による絞り込み、並び替え、キーワード検索も扱います。
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
     * filterパラメータ、sortパラメータ、keywordパラメータを受け取り、
     * 指定された条件で絞り込み・検索・並び替えをしたタスク一覧をHTMLへ渡します。
     *
     * @param filterType 表示条件。未指定の場合はすべて表示
     * @param sortType 並び替え条件。未指定の場合は登録順
     * @param keyword 検索キーワード。未指定の場合は空文字
     * @param model 画面へ値を渡すためのオブジェクト
     * @return 表示するテンプレート名
     */
    @GetMapping("/")
    public String showTaskList(
            @RequestParam(name = "filter", defaultValue = "ALL") TaskFilterType filterType,
            @RequestParam(name = "sort", defaultValue = "CREATED") TaskSortType sortType,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            Model model) {

        // Serviceから絞り込み・検索・並び替え済みのタスク一覧を取得してHTMLへ渡す
        model.addAttribute("tasks", taskService.findTasks(filterType, sortType, keyword));

        // 現在選択中の表示条件をHTMLへ渡す
        model.addAttribute("selectedFilter", filterType);

        // 現在選択中の並び替え条件をHTMLへ渡す
        model.addAttribute("selectedSort", sortType);

        // 現在入力されている検索キーワードをHTMLへ渡す
        model.addAttribute("keyword", keyword);

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
            // エラー時は「すべて表示」「登録順」「キーワードなし」に戻す
            TaskFilterType filterType = TaskFilterType.ALL;
            TaskSortType sortType = TaskSortType.CREATED;
            String keyword = "";

            // 一覧画面を再表示するため、タスク一覧をもう一度HTMLへ渡す
            model.addAttribute("tasks", taskService.findTasks(filterType, sortType, keyword));

            // 現在選択中の表示条件、並び替え条件、検索キーワードをHTMLへ渡す
            model.addAttribute("selectedFilter", filterType);
            model.addAttribute("selectedSort", sortType);
            model.addAttribute("keyword", keyword);

            // redirectではなくtasksを返すことで、エラー情報を画面に表示できる
            return "tasks";
        }

        // Serviceにタスク追加処理を依頼する
        taskService.addTask(taskForm.getTitle(), taskForm.getDueDate(), taskForm.getPriority());

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

    /**
     * タスク編集画面を表示します。
     *
     * URLに含まれるIDを使って編集対象のタスクを取得し、
     * 編集フォームに値を入れて画面へ渡します。
     *
     * @param id 編集対象のタスクID
     * @param model 画面へ値を渡すためのオブジェクト
     * @return 編集画面のテンプレート名。タスクが見つからない場合は一覧画面へリダイレクト
     */
    @GetMapping("/tasks/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        // Serviceから編集画面用のフォームを取得する
        Optional<TaskUpdateForm> optionalForm = taskService.findUpdateFormById(id);

        // 対象タスクが見つからない場合は一覧画面へ戻す
        if (optionalForm.isEmpty()) {
            return "redirect:/";
        }

        // 編集画面で使うフォームをHTMLへ渡す
        model.addAttribute("taskUpdateForm", optionalForm.get());

        // src/main/resources/templates/edit-task.html を表示する
        return "edit-task";
    }

    /**
     * 編集画面から送信された内容でタスクを更新します。
     *
     * 入力チェックに成功した場合だけ、Serviceへ更新処理を依頼します。
     *
     * @param id URLに含まれるタスクID
     * @param taskUpdateForm 編集画面から送信された入力値
     * @param bindingResult 入力チェックの結果
     * @return エラーがあれば編集画面、成功すれば一覧画面へリダイレクト
     */
    @PostMapping("/tasks/{id}/update")
    public String updateTask(
            @PathVariable Long id,
            @Valid @ModelAttribute TaskUpdateForm taskUpdateForm,
            BindingResult bindingResult) {

        // URLのIDをフォームへ設定する
        // hidden項目からもidは送られるが、URLの値を優先して使う
        taskUpdateForm.setId(id);

        // 入力チェックでエラーがある場合は、保存せずに編集画面へ戻す
        if (bindingResult.hasErrors()) {
            return "edit-task";
        }

        // Serviceに更新処理を依頼する
        boolean updated = taskService.updateTask(taskUpdateForm);

        // 対象タスクが存在しなかった場合は一覧画面へ戻す
        if (!updated) {
            return "redirect:/";
        }

        // 更新後は一覧画面へ戻る
        return "redirect:/";
    }

    /**
     * 画面で使用する優先度一覧を返します。
     *
     * このメソッドで返した値は、tasks.html や edit-task.html から
     * priorities という名前で参照できます。
     *
     * @return 優先度一覧
     */
    @ModelAttribute("priorities")
    public Priority[] priorities() {
        return Priority.values();
    }

    /**
     * 画面で使用する並び替え条件一覧を返します。
     *
     * このメソッドで返した値は、tasks.html から
     * sortTypes という名前で参照できます。
     *
     * @return 並び替え条件一覧
     */
    @ModelAttribute("sortTypes")
    public TaskSortType[] sortTypes() {
        return TaskSortType.values();
    }

    /**
     * 画面で使用する絞り込み条件一覧を返します。
     *
     * このメソッドで返した値は、tasks.html から
     * filterTypes という名前で参照できます。
     *
     * @return 絞り込み条件一覧
     */
    @ModelAttribute("filterTypes")
    public TaskFilterType[] filterTypes() {
        return TaskFilterType.values();
    }
}
