# ksp_sample

KSP（Kotlin Symbol Processing）を使ったコード生成のデモです。

クラス宣言の前に `@LogShroom` を付けると、そのクラスの関数の引数をログ出力するためのヘルパ（拡張関数）を自動生成します。

## 使い方

```kotlin
@LogShroom
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // ...
    }
}
```

ビルドすると、次の拡張関数が生成されます（`app/build/generated/ksp/debug/kotlin/com/example/kspdemo/LogShroomMainActivity.kt`）。

```kotlin
fun com.example.kspdemo.MainActivity.logOnCreate(savedInstanceState: android.os.Bundle?) = Log.d("MainActivity", "onCreate: savedInstanceState=$savedInstanceState ")
```

## 構成

| パス | 内容 |
|---|---|
| `logshroom/src/main/java/jp/sanwakuwaku/logshroom/LogShroom.kt` | アノテーション |
| `logshroom/src/main/java/jp/sanwakuwaku/logshroom/LogShroomProcessor.kt` | コードを生成するプロセッサ |
| `logshroom/src/main/java/jp/sanwakuwaku/logshroom/LogShroomProvider.kt` | プロセッサを作るプロバイダ |
| `app` | 使用例（`MainActivity` に `@LogShroom` を付けている） |

## 環境

2023年3月時点の構成です（Kotlin 1.8.0、KSP 1.8.0-1.0.8、Android Gradle Plugin 7.4.0）。
