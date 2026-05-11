# TaskList

シンプルなタスク管理Webアプリです。  
Spring Boot を用いたWebアプリ開発の学習として作成しました。

Java / Spring Boot / JPA / H2 Database を使い、タスクの登録・一覧表示・更新・削除・完了状態の切り替え、期限・優先度の管理、絞り込み、キーワード検索、並び替え機能を実装しています。

## アプリケーション概要

ブラウザ上でタスクを管理できるWebアプリです。

現在は、以下の基本機能を実装しています。

- タスクの新規登録
- タスクの一覧表示
- タスクの更新
- タスクの削除
- 完了 / 未完了の切り替え
- 期限の設定
- 期限切れ・今日が期限の表示
- 優先度の設定
- 完了状態による絞り込み
  - すべて
  - 未完了
  - 完了済み
- キーワード検索
- 並び替え
  - 登録順
  - 期限が近い順
  - 優先度が高い順
- 入力バリデーション
- 基本的なCSSによる表示改善

Spring Boot の基本的な構成に加えて、Controller / Service / Repository の役割分担を意識して実装しています。

## 主な機能

- タスクの一覧表示
- タスクの新規登録
- タスクの更新
- タスクの削除
- 完了 / 未完了フラグの切り替え
- 期限の登録・表示・更新
- 期限切れ・今日が期限の表示
- 優先度の登録・表示・更新
- 完了状態による絞り込み
  - すべて
  - 未完了
  - 完了済み
- タスクタイトルのキーワード検索
- タスク一覧の並び替え
  - 登録順
  - 期限が近い順
  - 優先度が高い順
- 入力バリデーション
  - 空文字のチェック
  - 空白のみの入力チェック
  - 長文入力のチェック
- 基本的なCSSによる表示改善
  - 完了済みタスクの取り消し線
  - 期限切れタスクの強調表示
  - 今日が期限のタスクの強調表示
  - エラーメッセージの強調表示

## 使用技術

- Java 17
- Spring Boot
- Spring Data JPA（Hibernate）
- Thymeleaf
- H2 Database（開発用）
- Maven
- HTML / CSS

## ディレクトリ構成

```text
src/main/java/com/mkunori/tasklist
├─ TaskListApplication.java        // アプリのエントリーポイント
├─ controller
│  └─ TaskController.java          // 画面表示やフォーム送信などのリクエストを受け取る
├─ entity
│  ├─ Priority.java                // タスクの優先度を表すenum
│  └─ Task.java                    // DBのtasksテーブルに対応するEntity
├─ form
│  ├─ TaskForm.java                // タスク新規登録フォームの入力値を受け取る
│  └─ TaskUpdateForm.java          // タスク編集フォームの入力値を受け取る
├─ repository
│  └─ TaskRepository.java          // Spring Data JPAでTaskをDB操作する
└─ service
   ├─ TaskFilterType.java          // タスク一覧の表示条件を表すenum
   ├─ TaskService.java             // タスク追加・更新・削除・検索・絞り込み・並び替えなどの処理を担当する
   └─ TaskSortType.java            // タスク一覧の並び替え条件を表すenum

src/main/resources
├─ static
│  └─ css
│     └─ style.css                 // 基本的な画面スタイル
├─ templates
│  ├─ tasks.html                   // タスク一覧画面
│  └─ edit-task.html               // タスク編集画面
└─ application.properties          // アプリケーション設定とH2 Database設定
```

## パッケージの役割

| パッケージ | 役割 |
| ---- | ---- |
| controller | ブラウザからのリクエストを受け取り、画面遷移やService呼び出しを行う |
| service | タスクの追加・更新・削除・完了切り替え・検索・絞り込み・並び替えなど、アプリケーションの処理を担当する |
| repository | Spring Data JPAを使ってDB操作を行う |
| entity | DBテーブルに対応するJavaクラスや、タスクの優先度を表すenumを定義する |
| form | 画面から送信された入力値を受け取る |

## クラス図

```mermaid
classDiagram
    class TaskListApplication
    class TaskController
    class TaskService
    class TaskRepository
    class Task
    class Priority
    class TaskForm
    class TaskUpdateForm
    class TaskFilterType
    class TaskSortType

    TaskListApplication ..> TaskController : scans
    TaskController --> TaskService : uses
    TaskController --> TaskForm : receives
    TaskController --> TaskUpdateForm : receives
    TaskController --> Priority : provides choices
    TaskController --> TaskFilterType : provides choices
    TaskController --> TaskSortType : provides choices
    TaskService --> TaskRepository : uses
    TaskService --> TaskUpdateForm : creates/uses
    TaskService --> TaskFilterType : uses
    TaskService --> TaskSortType : uses
    TaskRepository --> Task : manages
    Task --> Priority : has
    TaskForm --> Priority : has
    TaskUpdateForm --> Priority : has
```

## シーケンス図

### タスク追加

```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant TaskController
    participant TaskService
    participant TaskRepository
    participant H2Database

    User->>Browser: タイトル・期限・優先度を入力して追加
    Browser->>TaskController: POST /tasks
    TaskController->>TaskController: 入力チェック

    alt 入力エラーあり
        TaskController-->>Browser: tasks.html を返す
        Browser-->>User: エラーメッセージを表示
    else 入力エラーなし
        TaskController->>TaskService: addTask(title, dueDate, priority)
        TaskService->>TaskRepository: save(task)
        TaskRepository->>H2Database: INSERT INTO tasks ...
        H2Database-->>TaskRepository: 保存完了
        TaskRepository-->>TaskService: 保存済みTask
        TaskService-->>TaskController: 保存完了
        TaskController-->>Browser: 条件を維持して redirect:/
        Browser-->>User: 一覧画面を再表示
    end
```

### タスク更新

```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant TaskController
    participant TaskService
    participant TaskRepository
    participant H2Database

    User->>Browser: 編集内容を入力して更新
    Browser->>TaskController: POST /tasks/{id}/update
    TaskController->>TaskController: 入力チェック

    alt 入力エラーあり
        TaskController-->>Browser: edit-task.html を返す
        Browser-->>User: エラーメッセージを表示
    else 入力エラーなし
        TaskController->>TaskService: updateTask(taskUpdateForm)
        TaskService->>TaskRepository: findById(id)
        TaskRepository->>H2Database: SELECT * FROM tasks WHERE id = ?
        H2Database-->>TaskRepository: Task
        TaskRepository-->>TaskService: Task
        TaskService->>TaskRepository: save(updatedTask)
        TaskRepository->>H2Database: UPDATE tasks SET ...
        H2Database-->>TaskRepository: 更新完了
        TaskRepository-->>TaskService: 更新済みTask
        TaskService-->>TaskController: 更新結果
        TaskController-->>Browser: 条件を維持して redirect:/
        Browser-->>User: 一覧画面を再表示
    end
```

### タスク検索・絞り込み・並び替え

```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant TaskController
    participant TaskService
    participant TaskRepository
    participant H2Database

    User->>Browser: 表示条件・並び替え・キーワードを指定
    Browser->>TaskController: GET /?filter=...&sort=...&keyword=...
    TaskController->>TaskService: findTasks(filterType, sortType, keyword)
    TaskService->>TaskRepository: findAll()
    TaskRepository->>H2Database: SELECT * FROM tasks
    H2Database-->>TaskRepository: タスク一覧
    TaskRepository-->>TaskService: タスク一覧
    TaskService->>TaskService: 表示条件で絞り込み
    TaskService->>TaskService: キーワードで検索
    TaskService->>TaskService: 条件に応じて並び替え
    TaskService-->>TaskController: 処理済みタスク一覧
    TaskController-->>Browser: tasks.html を返す
    Browser-->>User: 検索・絞り込み・並び替え結果を表示
```

## 処理の流れ

### タスク一覧表示

```text
ブラウザ
  ↓ GET /
TaskController
  ↓
TaskService
  ↓
TaskRepository
  ↓
H2 Database
  ↓
tasks.html
  ↓
ブラウザに表示
```

### タスク追加

```text
ブラウザのフォーム
  ↓ POST /tasks
TaskForm
  ↓
TaskController
  ↓
TaskService
  ↓
TaskRepository
  ↓
H2 Database
```

### タスク更新

```text
編集リンク
  ↓ GET /tasks/{id}/edit
TaskController
  ↓
TaskService
  ↓
TaskRepository
  ↓
edit-task.html

編集フォーム
  ↓ POST /tasks/{id}/update
TaskUpdateForm
  ↓
TaskController
  ↓
TaskService
  ↓
TaskRepository
  ↓
H2 Database
```

### 完了状態の切り替え

```text
完了ボタン
  ↓ POST /tasks/{id}/toggle
TaskController
  ↓
TaskService
  ↓
TaskRepository
  ↓
H2 Database
```

### タスク削除

```text
削除ボタン
  ↓ POST /tasks/{id}/delete
TaskController
  ↓
TaskService
  ↓
TaskRepository
  ↓
H2 Database
```

### タスク検索・絞り込み・並び替え

```text
表示条件・並び替え条件・キーワードを指定
  ↓ GET /?filter=ALL&sort=CREATED&keyword=
     GET /?filter=ACTIVE&sort=DUE_DATE&keyword=Spring
     GET /?filter=DONE&sort=PRIORITY&keyword=Java
TaskController
  ↓
TaskService
  ↓
TaskRepository
  ↓
Java側で絞り込み・検索・並び替え
  ↓
tasks.html
```

## 表示条件

| 条件 | 内容 |
| ---- | ---- |
| すべて | すべてのタスクを表示します |
| 未完了 | 未完了のタスクだけを表示します |
| 完了済み | 完了済みのタスクだけを表示します |

## 並び替え条件

| 条件 | 内容 |
| ---- | ---- |
| 登録順 | IDの昇順で表示します |
| 期限が近い順 | 期限が近いタスクから表示します。期限なしのタスクは最後に表示します |
| 優先度が高い順 | HIGH、MEDIUM、LOW の順に表示します。同じ優先度の中では登録順で表示します |

## 期限表示

| 状態 | 表示例 |
| ---- | ---- |
| 期限なし | 期限なし |
| 期限切れ | 期限: 2026-05-10（期限切れ） |
| 今日が期限 | 期限: 2026-05-12（今日） |
| 通常の期限 | 期限: 2026-05-15 |

## 実行方法

### ① プロジェクトを取得

```bash
git clone <リポジトリURL>
cd tasklist
```

### ② アプリケーションを起動

Windowsの場合：

```bash
mvnw.cmd spring-boot:run
```

または VSCode の Spring Boot Dashboard から実行可能です。

### ③ ブラウザでアクセス

```text
http://localhost:8080/
```

## データベース（H2）

開発用に H2 Database を使用しています。

### H2コンソール

```text
http://localhost:8080/h2-console
```

設定：

- JDBC URL: `jdbc:h2:./data/taskdb`
- User: `sa`
- Password: （空）

## 開発メモ

このアプリでは、開発用DBとしてH2を使用しています。  
`application.properties` では、以下のようにファイル保存型のH2 Databaseを使用しています。

```properties
spring.datasource.url=jdbc:h2:./data/taskdb
```

そのため、実行するとプロジェクト直下に `data` ディレクトリが作成されます。  
このディレクトリはローカルのDBファイルなので、Git管理対象には含めません。

Entityのフィールドを変更したあとにDB構造との不整合が起きた場合、開発初期であれば `data` ディレクトリを削除してDBを作り直すことがあります。

また、現在の検索・絞り込み・並び替え機能は、DBから全件取得したあとにJava側で処理しています。  

## 今後の改善予定

- PostgreSQL への移行
- ログイン機能
- テストコードの追加
- RepositoryやDB側での検索・並び替え
- 画面デザインの改善
  - カード型レイアウト
  - Microsoft To Do のような見やすいタスク表示

## 学習ポイント

- Spring Boot によるWebアプリケーションの構築
- MVC構成の基本
- Controller / Service / Repository の役割分担
- Spring Data JPA によるDB操作
- JPAによるO/Rマッピング
- Thymeleafによる画面表示
- 静的CSSファイルの読み込み
- フォーム送信とバリデーション
- CRUD処理の実装
  - Create: タスク追加
  - Read: タスク一覧表示
  - Update: タスク更新、完了状態の切り替え
  - Delete: タスク削除
- `LocalDate` を使った期限日の管理
- `enum` を使った優先度・表示条件・並び替え条件の管理
- `Comparator` を使ったJava側での並び替え
- Java側でのキーワード検索・絞り込み
- H2 Databaseを使った開発用DBの利用