package com.mkunori.tasklist.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * AnonymousUserServiceの単体テストです。
 *
 * Cookieに保存された匿名ユーザーIDを取得できること、
 * Cookieが存在しない場合は新しい匿名ユーザーIDを作成してCookieへ保存することを確認します。
 */
class AnonymousUserServiceTest {

    /**
     * AnonymousUserServiceで使っているCookie名です。
     *
     * 本番コードのCOOKIE_NAMEはprivate定数のため、
     * テスト側でも同じ名前を定義しています。
     */
    private static final String COOKIE_NAME = "tasklistAnonymousUserId";

    /**
     * テスト対象のサービスです。
     */
    private final AnonymousUserService anonymousUserService = new AnonymousUserService();

    /**
     * Cookieに匿名ユーザーIDが存在する場合、そのIDが返されることを確認します。
     */
    @Test
    void getOrCreateOwnerId_returnsExistingOwnerId_whenCookieExists() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setCookies(new Cookie(COOKIE_NAME, "existing-owner-id"));

        String ownerId = anonymousUserService.getOrCreateOwnerId(request, response);

        assertEquals("existing-owner-id", ownerId);
        assertEquals(0, response.getCookies().length);
    }

    /**
     * Cookieに匿名ユーザーIDが存在しない場合、
     * 新しいIDが作成され、Cookieへ保存されることを確認します。
     */
    @Test
    void getOrCreateOwnerId_createsOwnerIdAndCookie_whenCookieDoesNotExist() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String ownerId = anonymousUserService.getOrCreateOwnerId(request, response);

        Cookie cookie = response.getCookie(COOKIE_NAME);

        assertNotNull(ownerId);
        assertNotNull(cookie);
        assertEquals(ownerId, cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.isHttpOnly());
    }
}