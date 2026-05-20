package com.mkunori.tasklist.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 匿名ユーザーIDを管理するサービスクラスです。
 *
 * このアプリではログイン機能を使わず、ブラウザのCookieに保存したIDを使って、
 * ブラウザごとにタスクを分けます。
 *
 * Cookieに匿名ユーザーIDが存在しない場合は、新しいIDを作成してCookieへ保存します。
 */
@Service
public class AnonymousUserService {

    /**
     * 匿名ユーザーIDを保存するCookie名です。
     */
    private static final String COOKIE_NAME = "tasklistAnonymousUserId";

    /**
     * Cookieの有効期限です。
     *
     * 秒単位で指定します。
     * ここでは約1年間有効にしています。
     */
    private static final int COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 365;

    /**
     * リクエストから匿名ユーザーIDを取得します。
     *
     * Cookieに匿名ユーザーIDが存在する場合は、その値を返します。
     * 存在しない場合は新しいIDを作成し、Cookieへ保存してから返します。
     *
     * @param request ブラウザからのリクエスト
     * @param response ブラウザへのレスポンス
     * @return 匿名ユーザーID
     */
    public String getOrCreateOwnerId(HttpServletRequest request, HttpServletResponse response) {
        String ownerId = findOwnerIdFromCookie(request);

        if (ownerId != null) {
            return ownerId;
        }

        String newOwnerId = UUID.randomUUID().toString();
        addOwnerIdCookie(response, newOwnerId);

        return newOwnerId;
    }

    /**
     * リクエストのCookieから匿名ユーザーIDを探します。
     *
     * Cookie自体が存在しない場合や、対象のCookieが見つからない場合は null を返します。
     *
     * @param request ブラウザからのリクエスト
     * @return 匿名ユーザーID。見つからない場合は null
     */
    private String findOwnerIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    /**
     * 匿名ユーザーIDをCookieへ保存します。
     *
     * @param response ブラウザへのレスポンス
     * @param ownerId 匿名ユーザーID
     */
    private void addOwnerIdCookie(HttpServletResponse response, String ownerId) {
        Cookie cookie = new Cookie(COOKIE_NAME, ownerId);

        // アプリ全体でこのCookieを使えるようにします。
        cookie.setPath("/");

        // JavaScriptからCookieを読み取れないようにします。
        cookie.setHttpOnly(true);

        // Cookieの有効期限を設定します。
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);

        response.addCookie(cookie);
    }
}