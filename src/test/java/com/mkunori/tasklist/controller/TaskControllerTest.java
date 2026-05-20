package com.mkunori.tasklist.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mkunori.tasklist.entity.Priority;
import com.mkunori.tasklist.entity.Task;
import com.mkunori.tasklist.form.TaskUpdateForm;
import com.mkunori.tasklist.service.AnonymousUserService;
import com.mkunori.tasklist.service.TaskFilterType;
import com.mkunori.tasklist.service.TaskService;
import com.mkunori.tasklist.service.TaskSortType;

/**
 * TaskControllerの単体テストです。
 *
 * WebMvcTestを使い、ControllerのURL、画面名、Modelへの値渡しを確認します。
 * TaskServiceはモックにしているため、DBには接続しません。
 */
@WebMvcTest(TaskController.class)
class TaskControllerTest {

        /**
         * Controllerのテスト用にHTTPリクエストを再現するためのオブジェクトです。
         *
         * 実際にブラウザを使わずに、GETやPOSTのリクエストをテストできます。
         */
        @Autowired
        private MockMvc mockMvc;

        /**
         * TaskControllerが利用するTaskServiceのモックです。
         *
         * Controllerだけをテストしたいので、Service本体の処理はここでは動かしません。
         */
        @MockitoBean
        private TaskService taskService;

        /**
         * TaskControllerが利用するAnonymousUserServiceのモックです。
         */
        @MockitoBean
        private AnonymousUserService anonymousUserService;

        /**
         * テスト用の匿名ユーザーIDです。
         */
        private static final String TEST_OWNER_ID = "test-owner";

        /**
         * 一覧画面にアクセスすると、tasks.htmlが表示されることを確認します。
         *
         * また、ControllerがServiceからタスク一覧を取得し、
         * ModelにtasksとtaskFormを渡していることも確認します。
         */
        @Test
        void showTaskList_returnsTasksView() throws Exception {
                Task task = new Task(
                                "Springを学ぶ",
                                LocalDate.of(2026, 5, 10),
                                Priority.HIGH,
                                TEST_OWNER_ID);

                when(anonymousUserService.getOrCreateOwnerId(any(), any()))
                                .thenReturn(TEST_OWNER_ID);

                when(taskService.findTasks(
                                TEST_OWNER_ID,
                                TaskFilterType.ALL,
                                TaskSortType.CREATED,
                                "")).thenReturn(List.of(task));

                mockMvc.perform(get("/"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("tasks"))
                                .andExpect(model().attributeExists("tasks"))
                                .andExpect(model().attribute("tasks", hasSize(1)))
                                .andExpect(model().attributeExists("taskForm"))
                                .andExpect(model().attribute("selectedFilter", TaskFilterType.ALL))
                                .andExpect(model().attribute("selectedSort", TaskSortType.CREATED))
                                .andExpect(model().attribute("keyword", ""));

                verify(anonymousUserService).getOrCreateOwnerId(any(), any());

                verify(taskService).findTasks(
                                TEST_OWNER_ID,
                                TaskFilterType.ALL,
                                TaskSortType.CREATED,
                                "");
        }

        /**
         * 正しい入力でタスクを追加すると、ServiceのaddTaskが呼ばれ、
         * 一覧画面へリダイレクトされることを確認します。
         */
        @Test
        void addTask_withValidInput_callsServiceAndRedirects() throws Exception {
                when(anonymousUserService.getOrCreateOwnerId(any(), any()))
                                .thenReturn(TEST_OWNER_ID);

                mockMvc.perform(post("/tasks")
                                .param("title", "新しいタスク")
                                .param("dueDate", "2026-05-10")
                                .param("priority", "HIGH")
                                .param("filter", "ALL")
                                .param("sort", "CREATED")
                                .param("keyword", ""))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("/?filter=ALL&sort=CREATED&keyword=*"));

                verify(taskService).addTask(
                                eq("新しいタスク"),
                                eq(LocalDate.of(2026, 5, 10)),
                                eq(Priority.HIGH),
                                eq(TEST_OWNER_ID));
        }

        /**
         * タイトルが空の状態でタスク追加を送信すると、
         * ServiceのaddTaskは呼ばれず、tasks.htmlに戻ることを確認します。
         */
        @Test
        void addTask_withInvalidInput_returnsTasksView() throws Exception {
                when(anonymousUserService.getOrCreateOwnerId(any(), any()))
                                .thenReturn(TEST_OWNER_ID);

                when(taskService.findTasks(
                                TEST_OWNER_ID,
                                TaskFilterType.ALL,
                                TaskSortType.CREATED,
                                "")).thenReturn(List.of());

                mockMvc.perform(post("/tasks")
                                .param("title", "")
                                .param("dueDate", "2026-05-10")
                                .param("priority", "HIGH")
                                .param("filter", "ALL")
                                .param("sort", "CREATED")
                                .param("keyword", ""))
                                .andExpect(status().isOk())
                                .andExpect(view().name("tasks"))
                                .andExpect(model().attributeHasFieldErrors("taskForm", "title"))
                                .andExpect(model().attributeExists("tasks"))
                                .andExpect(model().attribute("selectedFilter", TaskFilterType.ALL))
                                .andExpect(model().attribute("selectedSort", TaskSortType.CREATED))
                                .andExpect(model().attribute("keyword", ""));

                verify(taskService, never()).addTask(
                                eq(""),
                                eq(LocalDate.of(2026, 5, 10)),
                                eq(Priority.HIGH),
                                eq(TEST_OWNER_ID));
        }

        /**
         * 編集画面にアクセスすると、edit-task.htmlが表示されることを確認します。
         *
         * ControllerがCookieから匿名ユーザーIDを取得し、
         * ServiceからTaskUpdateFormを取得して、
         * ModelにtaskUpdateFormを渡していることも確認します。
         */
        @Test
        void showEditForm_returnsEditTaskView() throws Exception {
                TaskUpdateForm form = new TaskUpdateForm();
                form.setId(1L);
                form.setTitle("編集対象タスク");
                form.setDueDate(LocalDate.of(2026, 5, 10));
                form.setPriority(Priority.HIGH);

                when(anonymousUserService.getOrCreateOwnerId(any(), any()))
                                .thenReturn(TEST_OWNER_ID);

                when(taskService.findUpdateFormById(1L, TEST_OWNER_ID))
                                .thenReturn(Optional.of(form));

                mockMvc.perform(get("/tasks/1/edit")
                                .param("filter", "ACTIVE")
                                .param("sort", "DUE_DATE")
                                .param("keyword", "Spring"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("edit-task"))
                                .andExpect(model().attributeExists("taskUpdateForm"))
                                .andExpect(model().attribute("selectedFilter", TaskFilterType.ACTIVE))
                                .andExpect(model().attribute("selectedSort", TaskSortType.DUE_DATE))
                                .andExpect(model().attribute("keyword", "Spring"));

                verify(anonymousUserService).getOrCreateOwnerId(any(), any());
                verify(taskService).findUpdateFormById(1L, TEST_OWNER_ID);
        }

        /**
         * 編集対象のタスクが見つからない場合、
         * 一覧画面へリダイレクトされることを確認します。
         *
         * タスクIDだけでなく匿名ユーザーIDも条件にして検索するため、
         * 他のブラウザのタスクは編集できません。
         */
        @Test
        void showEditForm_redirectsToTaskList_whenTaskNotFound() throws Exception {
                when(anonymousUserService.getOrCreateOwnerId(any(), any()))
                                .thenReturn(TEST_OWNER_ID);

                when(taskService.findUpdateFormById(999L, TEST_OWNER_ID))
                                .thenReturn(Optional.empty());

                mockMvc.perform(get("/tasks/999/edit")
                                .param("filter", "ACTIVE")
                                .param("sort", "DUE_DATE")
                                .param("keyword", "Spring"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("/?filter=ACTIVE&sort=DUE_DATE&keyword=Spring*"));

                verify(anonymousUserService).getOrCreateOwnerId(any(), any());
                verify(taskService).findUpdateFormById(999L, TEST_OWNER_ID);
        }

        /**
         * 正しい入力でタスクを更新すると、ServiceのupdateTaskが呼ばれ、
         * 一覧画面へリダイレクトされることを確認します。
         */
        @Test
        void updateTask_withValidInput_callsServiceAndRedirects() throws Exception {
                when(anonymousUserService.getOrCreateOwnerId(any(), any()))
                                .thenReturn(TEST_OWNER_ID);

                when(taskService.updateTask(any(TaskUpdateForm.class), eq(TEST_OWNER_ID)))
                                .thenReturn(true);

                mockMvc.perform(post("/tasks/1/update")
                                .param("id", "1")
                                .param("title", "更新後タスク")
                                .param("dueDate", "2026-05-12")
                                .param("priority", "MEDIUM")
                                .param("filter", "ACTIVE")
                                .param("sort", "DUE_DATE")
                                .param("keyword", "Spring"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("/?filter=ACTIVE&sort=DUE_DATE&keyword=Spring*"));

                verify(taskService).updateTask(any(TaskUpdateForm.class), eq(TEST_OWNER_ID));
        }

        /**
         * タイトルが空の状態で更新を送信すると、
         * ServiceのupdateTaskは呼ばれず、edit-task.htmlに戻ることを確認します。
         *
         * 入力エラー時も、表示条件、並び替え条件、検索キーワードはModelに保持されます。
         */
        @Test
        void updateTask_withInvalidInput_returnsEditTaskView() throws Exception {
                when(anonymousUserService.getOrCreateOwnerId(any(), any()))
                                .thenReturn(TEST_OWNER_ID);

                mockMvc.perform(post("/tasks/1/update")
                                .param("id", "1")
                                .param("title", "")
                                .param("dueDate", "2026-05-12")
                                .param("priority", "MEDIUM")
                                .param("filter", "ACTIVE")
                                .param("sort", "DUE_DATE")
                                .param("keyword", "Spring"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("edit-task"))
                                .andExpect(model().attributeHasFieldErrors("taskUpdateForm", "title"))
                                .andExpect(model().attribute("selectedFilter", TaskFilterType.ACTIVE))
                                .andExpect(model().attribute("selectedSort", TaskSortType.DUE_DATE))
                                .andExpect(model().attribute("keyword", "Spring"));

                verify(anonymousUserService).getOrCreateOwnerId(any(), any());
                verify(taskService, never()).updateTask(any(TaskUpdateForm.class), eq(TEST_OWNER_ID));
        }

        /**
         * 完了状態切り替えボタンを押すと、
         * ServiceのtoggleTaskDoneが呼ばれ、一覧画面へリダイレクトされることを確認します。
         */
        @Test
        void toggleTaskDone_callsServiceAndRedirects() throws Exception {
                when(anonymousUserService.getOrCreateOwnerId(any(), any()))
                                .thenReturn(TEST_OWNER_ID);

                mockMvc.perform(post("/tasks/1/toggle")
                                .param("filter", "ACTIVE")
                                .param("sort", "DUE_DATE")
                                .param("keyword", "Spring"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("/?filter=ACTIVE&sort=DUE_DATE&keyword=Spring*"));

                verify(taskService).toggleTaskDone(1L, TEST_OWNER_ID);
        }

        /**
         * 削除ボタンを押すと、
         * ServiceのdeleteTaskが呼ばれ、一覧画面へリダイレクトされることを確認します。
         */
        @Test
        void deleteTask_callsServiceAndRedirects() throws Exception {
                when(anonymousUserService.getOrCreateOwnerId(any(), any()))
                                .thenReturn(TEST_OWNER_ID);

                mockMvc.perform(post("/tasks/1/delete")
                                .param("filter", "DONE")
                                .param("sort", "PRIORITY")
                                .param("keyword", "Java"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("/?filter=DONE&sort=PRIORITY&keyword=Java*"));

                verify(taskService).deleteTask(1L, TEST_OWNER_ID);
        }
}