package com.mkunori.tasklist.form;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mkunori.tasklist.entity.Priority;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * TaskUpdateFormのバリデーションテストです。
 *
 * 画面から送信されるタスク編集フォームの入力値が、
 * Bean Validationのルールに従って検証されることを確認します。
 */
class TaskUpdateFormTest {

    /**
     * Bean Validationを実行するためのValidatorです。
     */
    private Validator validator;

    /**
     * 各テストの前にValidatorを準備します。
     */
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * 正しい入力値の場合、バリデーションエラーが発生しないことを確認します。
     */
    @Test
    void validForm_hasNoValidationErrors() {
        TaskUpdateForm form = new TaskUpdateForm();
        form.setId(1L);
        form.setTitle("更新後タスク");
        form.setDueDate(LocalDate.of(2026, 5, 10));
        form.setPriority(Priority.MEDIUM);

        Set<ConstraintViolation<TaskUpdateForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty());
    }

    /**
     * タイトルが空文字の場合、バリデーションエラーが発生することを確認します。
     */
    @Test
    void blankTitle_hasValidationError() {
        TaskUpdateForm form = new TaskUpdateForm();
        form.setId(1L);
        form.setTitle("");
        form.setDueDate(LocalDate.of(2026, 5, 10));
        form.setPriority(Priority.MEDIUM);

        Set<ConstraintViolation<TaskUpdateForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationForProperty(violations, "title"));
    }

    /**
     * タイトルが空白のみの場合、バリデーションエラーが発生することを確認します。
     */
    @Test
    void whitespaceTitle_hasValidationError() {
        TaskUpdateForm form = new TaskUpdateForm();
        form.setId(1L);
        form.setTitle("   ");
        form.setDueDate(LocalDate.of(2026, 5, 10));
        form.setPriority(Priority.MEDIUM);

        Set<ConstraintViolation<TaskUpdateForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationForProperty(violations, "title"));
    }

    /**
     * タイトルが長すぎる場合、バリデーションエラーが発生することを確認します。
     */
    @Test
    void tooLongTitle_hasValidationError() {
        TaskUpdateForm form = new TaskUpdateForm();
        form.setId(1L);
        form.setTitle("a".repeat(256));
        form.setDueDate(LocalDate.of(2026, 5, 10));
        form.setPriority(Priority.MEDIUM);

        Set<ConstraintViolation<TaskUpdateForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationForProperty(violations, "title"));
    }

    /**
     * 優先度が未設定の場合、バリデーションエラーが発生することを確認します。
     */
    @Test
    void nullPriority_hasValidationError() {
        TaskUpdateForm form = new TaskUpdateForm();
        form.setId(1L);
        form.setTitle("更新後タスク");
        form.setDueDate(LocalDate.of(2026, 5, 10));
        form.setPriority(null);

        Set<ConstraintViolation<TaskUpdateForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationForProperty(violations, "priority"));
    }

    /**
     * 期限日が未設定でも、バリデーションエラーが発生しないことを確認します。
     *
     * 期限なしのタスクを許可しているため、dueDateはnullでもOKです。
     */
    @Test
    void nullDueDate_hasNoValidationError() {
        TaskUpdateForm form = new TaskUpdateForm();
        form.setId(1L);
        form.setTitle("期限なしタスク");
        form.setDueDate(null);
        form.setPriority(Priority.MEDIUM);

        Set<ConstraintViolation<TaskUpdateForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty());
    }

    /**
     * 指定したプロパティに対するバリデーションエラーが含まれているかを確認します。
     *
     * @param violations   バリデーションエラー一覧
     * @param propertyName 確認したいプロパティ名
     * @return 指定したプロパティのエラーがある場合はtrue
     */
    private boolean hasViolationForProperty(
            Set<? extends ConstraintViolation<?>> violations,
            String propertyName) {

        return violations.stream()
                .anyMatch(violation -> propertyName.equals(
                        violation.getPropertyPath().toString()));
    }
}