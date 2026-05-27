package com.mkunori.tasklist.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import com.mkunori.tasklist.entity.Priority;
import com.mkunori.tasklist.form.TaskForm;
import com.mkunori.tasklist.form.TaskUpdateForm;
import com.mkunori.tasklist.service.AnonymousUserService;
import com.mkunori.tasklist.service.TaskFilterType;
import com.mkunori.tasklist.service.TaskService;
import com.mkunori.tasklist.service.TaskSortType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
@RequestMapping("/tasklist")
public class TaskController {

    /**
     * タスクに関する処理を担当するサービスです。
     */
    private final TaskService taskService;

    /**
     * 匿名ユーザーIDを管理するサービスです。
     *
     * Cookieから匿名ユーザーIDを取得し、
     * 存在しない場合は新しく作成します。
     */
    private final AnonymousUserService anonymousUserService;

    /**
     * コンストラクタです。
     *
     * SpringがTaskServiceとAnonymousUserServiceを自動で渡してくれます。
     *
     * @param taskService          タスクサービス
     * @param anonymousUserService 匿名ユーザーIDを管理するサービス
     */
    public TaskController(
            TaskService taskService,
            AnonymousUserService anonymousUserService) {
        this.taskService = taskService;
        this.anonymousUserService = anonymousUserService;
    }

    /**
     * タスク一覧画面を表示します。
     *
     * filterパラメータ、sortパラメータ、keywordパラメータを受け取り、
     * 指定された条件で絞り込み・検索・並び替えをしたタスク一覧をHTMLへ渡します。
     *
     * Cookieから匿名ユーザーIDを取得し、そのユーザーに紐づくタスクだけを表示します。
     *
     * @param filterType 表示条件。未指定の場合はすべて表示
     * @param sortType   並び替え条件。未指定の場合は登録順
     * @param keyword    検索キーワード。未指定の場合は空文字
     * @param request    ブラウザからのリクエスト
     * @param response   ブラウザへのレスポンス
     * @param model      画面へ値を渡すためのオブジェクト
     * @return 表示するテンプレート名
     */
    @GetMapping({"", "/"})
    public String showTaskList(
            @RequestParam(name = "filter", defaultValue = "ALL") TaskFilterType filterType,
            @RequestParam(name = "sort", defaultValue = "CREATED") TaskSortType sortType,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        // Cookieから匿名ユーザーIDを取得する
        // 初回アクセスでCookieがない場合は、新しいIDを作成してCookieへ保存する
        String ownerId = anonymousUserService.getOrCreateOwnerId(request, response);

        model.addAttribute("tasks", taskService.findTasks(ownerId, filterType, sortType, keyword));
        model.addAttribute("selectedFilter", filterType);
        model.addAttribute("selectedSort", sortType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("taskForm", new TaskForm());

        return "tasks";
    }

    /**
     * 入力されたタスクをDBに保存します。
     *
     * 入力チェックに成功した場合だけ、Serviceへタスク追加を依頼します。
     * 操作後は、現在の表示条件、並び替え条件、検索キーワードを維持したまま一覧へ戻ります。
     *
     * Cookieから匿名ユーザーIDを取得し、そのユーザーのタスクとして保存します。
     *
     * @param taskForm      画面から送信された入力値
     * @param bindingResult 入力チェックの結果
     * @param filterType    表示条件
     * @param sortType      並び替え条件
     * @param keyword       検索キーワード
     * @param request       ブラウザからのリクエスト
     * @param response      ブラウザへのレスポンス
     * @param model         画面へ値を渡すためのオブジェクト
     * @return エラーがあれば一覧画面、成功すれば条件を維持して一覧画面へリダイレクト
     */
    @PostMapping("/tasks")
    public String addTask(
            @Valid @ModelAttribute TaskForm taskForm,
            BindingResult bindingResult,
            @RequestParam(name = "filter", defaultValue = "ALL") TaskFilterType filterType,
            @RequestParam(name = "sort", defaultValue = "CREATED") TaskSortType sortType,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        String ownerId = anonymousUserService.getOrCreateOwnerId(request, response);

        if (bindingResult.hasErrors()) {
            model.addAttribute("tasks", taskService.findTasks(ownerId, filterType, sortType, keyword));
            model.addAttribute("selectedFilter", filterType);
            model.addAttribute("selectedSort", sortType);
            model.addAttribute("keyword", keyword);

            return "tasks";
        }

        taskService.addTask(
                taskForm.getTitle(),
                taskForm.getDueDate(),
                taskForm.getPriority(),
                ownerId);

        return redirectToTaskList(filterType, sortType, keyword);
    }

    /**
     * 指定されたIDのタスクを削除します。
     *
     * URLに含まれるIDを受け取り、Serviceへ削除処理を依頼します。
     * 削除後は、現在の表示条件、並び替え条件、検索キーワードを維持したまま一覧へ戻ります。
     *
     * Cookieから匿名ユーザーIDを取得し、そのユーザーに紐づくタスクだけを削除対象にします。
     *
     * @param id         削除するタスクのID
     * @param filterType 表示条件
     * @param sortType   並び替え条件
     * @param keyword    検索キーワード
     * @param request    ブラウザからのリクエスト
     * @param response   ブラウザへのレスポンス
     * @return 条件を維持した一覧画面へのリダイレクト
     */
    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(
            @PathVariable Long id,
            @RequestParam(name = "filter", defaultValue = "ALL") TaskFilterType filterType,
            @RequestParam(name = "sort", defaultValue = "CREATED") TaskSortType sortType,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            HttpServletRequest request,
            HttpServletResponse response) {

        String ownerId = anonymousUserService.getOrCreateOwnerId(request, response);

        taskService.deleteTask(id, ownerId);

        return redirectToTaskList(filterType, sortType, keyword);
    }

    /**
     * 指定されたIDのタスクの完了状態を切り替えます。
     *
     * URLに含まれるIDを受け取り、Serviceへ完了状態の切り替えを依頼します。
     * 処理後は、現在の表示条件、並び替え条件、検索キーワードを維持したまま一覧へ戻ります。
     *
     * Cookieから匿名ユーザーIDを取得し、そのユーザーに紐づくタスクだけを操作対象にします。
     *
     * @param id         完了状態を切り替えるタスクのID
     * @param filterType 表示条件
     * @param sortType   並び替え条件
     * @param keyword    検索キーワード
     * @param request    ブラウザからのリクエスト
     * @param response   ブラウザへのレスポンス
     * @return 条件を維持した一覧画面へのリダイレクト
     */
    @PostMapping("/tasks/{id}/toggle")
    public String toggleTaskDone(
            @PathVariable Long id,
            @RequestParam(name = "filter", defaultValue = "ALL") TaskFilterType filterType,
            @RequestParam(name = "sort", defaultValue = "CREATED") TaskSortType sortType,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            HttpServletRequest request,
            HttpServletResponse response) {

        String ownerId = anonymousUserService.getOrCreateOwnerId(request, response);

        taskService.toggleTaskDone(id, ownerId);

        return redirectToTaskList(filterType, sortType, keyword);
    }

    /**
     * タスク編集画面を表示します。
     *
     * URLに含まれるIDを使って編集対象のタスクを取得し、
     * 編集フォームに値を入れて画面へ渡します。
     *
     * 一覧画面の表示条件、並び替え条件、検索キーワードも編集画面へ渡し、
     * 更新後に同じ条件の一覧へ戻れるようにします。
     *
     * Cookieから匿名ユーザーIDを取得し、そのユーザーに紐づくタスクだけを編集対象にします。
     *
     * @param id         編集対象のタスクID
     * @param filterType 表示条件
     * @param sortType   並び替え条件
     * @param keyword    検索キーワード
     * @param request    ブラウザからのリクエスト
     * @param response   ブラウザへのレスポンス
     * @param model      画面へ値を渡すためのオブジェクト
     * @return 編集画面のテンプレート名。タスクが見つからない場合は一覧画面へリダイレクト
     */
    @GetMapping("/tasks/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            @RequestParam(name = "filter", defaultValue = "ALL") TaskFilterType filterType,
            @RequestParam(name = "sort", defaultValue = "CREATED") TaskSortType sortType,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        String ownerId = anonymousUserService.getOrCreateOwnerId(request, response);

        Optional<TaskUpdateForm> optionalForm = taskService.findUpdateFormById(id, ownerId);

        if (optionalForm.isEmpty()) {
            return redirectToTaskList(filterType, sortType, keyword);
        }

        model.addAttribute("taskUpdateForm", optionalForm.get());
        model.addAttribute("selectedFilter", filterType);
        model.addAttribute("selectedSort", sortType);
        model.addAttribute("keyword", keyword);

        return "edit-task";
    }

    /**
     * 編集画面から送信された内容でタスクを更新します。
     *
     * 入力チェックに成功した場合だけ、Serviceへ更新処理を依頼します。
     * 更新後は、現在の表示条件、並び替え条件、検索キーワードを維持したまま一覧へ戻ります。
     *
     * Cookieから匿名ユーザーIDを取得し、そのユーザーに紐づくタスクだけを更新対象にします。
     *
     * @param id             URLに含まれるタスクID
     * @param taskUpdateForm 編集画面から送信された入力値
     * @param bindingResult  入力チェックの結果
     * @param filterType     表示条件
     * @param sortType       並び替え条件
     * @param keyword        検索キーワード
     * @param request        ブラウザからのリクエスト
     * @param response       ブラウザへのレスポンス
     * @param model          画面へ値を渡すためのオブジェクト
     * @return エラーがあれば編集画面、成功すれば条件を維持して一覧画面へリダイレクト
     */
    @PostMapping("/tasks/{id}/update")
    public String updateTask(
            @PathVariable Long id,
            @Valid @ModelAttribute TaskUpdateForm taskUpdateForm,
            BindingResult bindingResult,
            @RequestParam(name = "filter", defaultValue = "ALL") TaskFilterType filterType,
            @RequestParam(name = "sort", defaultValue = "CREATED") TaskSortType sortType,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        String ownerId = anonymousUserService.getOrCreateOwnerId(request, response);

        taskUpdateForm.setId(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("selectedFilter", filterType);
            model.addAttribute("selectedSort", sortType);
            model.addAttribute("keyword", keyword);

            return "edit-task";
        }

        boolean updated = taskService.updateTask(taskUpdateForm, ownerId);

        if (!updated) {
            return redirectToTaskList(filterType, sortType, keyword);
        }

        return redirectToTaskList(filterType, sortType, keyword);
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

    /**
     * タスク一覧画面へ戻るためのリダイレクトURLを作成します。
     *
     * 現在の表示条件、並び替え条件、検索キーワードをURLに含めることで、
     * 追加・更新・削除などの操作後も同じ条件の一覧へ戻れるようにします。
     *
     * @param filterType 表示条件
     * @param sortType   並び替え条件
     * @param keyword    検索キーワード
     * @return 一覧画面へのリダイレクト文字列
     */
    private String redirectToTaskList(
            TaskFilterType filterType,
            TaskSortType sortType,
            String keyword) {

        String safeKeyword = keyword == null ? "" : keyword;

        String url = UriComponentsBuilder.fromPath("/tasklist")
                .queryParam("filter", filterType)
                .queryParam("sort", sortType)
                .queryParam("keyword", safeKeyword)
                .build()
                .encode()
                .toUriString();

        return "redirect:" + url;
    }
}
