# TaskList

シンプルなタスク管理Webアプリです。
Spring Boot を用いたWebアプリ開発の学習として作成しました。

---

## アプリケーション概要

ブラウザ上でタスクの登録と一覧表示ができるアプリです。
Java + Spring Boot + JPA（O/Rマッピング）を用いて実装しています。

---

## 主な機能

* タスクの一覧表示
* タスクの新規登録
* タスクの削除
* 入力バリデーション(空文字、空白、長文)
* 完了・未完了フラグ

---

## 使用技術

* Java 17
* Spring Boot
* Spring Data JPA（Hibernate）
* Thymeleaf
* H2 Database（開発用）
* Maven

---

## ディレクトリ構成

```
src/main/java/com/mkunori/tasklist
├─ TaskListApplication.java
├─ controller
│  └─ TaskController.java
├─ entity
│  └─ Task.java
├─ form
│  └─ TaskForm.java
└─ repository
   └─ TaskRepository.java

src/main/resources
├─ templates
│  └─ tasks.html
└─ application.properties
```

---

## 実行方法

### ① プロジェクトを取得

```bash
git clone <リポジトリURL>
cd tasklist
```

### ② アプリケーションを起動

```bash
./mvnw spring-boot:run
```

（Windowsの場合）

```bash
mvnw.cmd spring-boot:run
```

または VSCode の Spring Boot Dashboard から実行可能です。

---

### ③ ブラウザでアクセス

```
http://localhost:8080/
```

---

## データベース（H2）

開発用に H2 Database を使用しています。

### H2コンソール

```
http://localhost:8080/h2-console
```

設定：

* JDBC URL: `jdbc:h2:./data/taskdb`
* User: `sa`
* Password: （空）

---

## 今後の改善予定

* 更新機能
* PostgreSQL への移行
* ログイン機能

---

## 学習ポイント

* Spring Boot によるWebアプリケーションの構築
* MVC（Controller / View / Model）の理解
* JPAによるO/Rマッピング
* フォーム送信とデータ保存の流れ

---
