package jp.sanwakuwaku.logshroom

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate

/**
 * クラス宣言の前に`@`LogShroomとつけるとそのクラスの関数の引数をログ出力するためのヘルパ（拡張関数）を自動生成してくれるサンプル
 */
class LogShroomProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {

        val symbols =
            resolver.getSymbolsWithAnnotation("jp.sanwakuwaku.logshroom.LogShroom")
                .filterIsInstance<KSClassDeclaration>() // 他にKSFunctionDeclarationなどがある。

        // multiple round processing
        // 現在処理できないシンボルは後に回し、今回処理できるものだけ処理する
        val (validSymbols, invalidSymbols) = symbols.partition { it.validate() }

        validSymbols.forEach { symbol ->
            if (symbol.classKind == ClassKind.INTERFACE) {
                logger.error("LogShroom can't be applied to interface")
                return@forEach
            }

            generateCode(symbol)
        }

        // 無効なシンボルは次回処理に回す
        return invalidSymbols
    }

    private fun generateCode(symbol: KSClassDeclaration) {
        val packageName = symbol.packageName.asString()
        val className = symbol.simpleName.asString()

        val code = buildString {
            appendLine(
                """
                    package $packageName
                    
                    import android.util.Log
                     
                """.trimIndent()
            )

            // クラス内の関数を取得
            // getAllFunctions()はsuper classの関数も含まれる
            // getDeclaredFunctions()はsuper classの関数は含まれない
            symbol.getDeclaredFunctions().forEach { functionDeclaration ->
                if (functionDeclaration.isConstructor()) {
                    return@forEach
                }
                val funName = functionDeclaration.simpleName.asString()
                val upperCamelFunName = funName.replaceFirstChar { it.uppercase() }

                // 関数のパラメータを取得
                // 関数の引数名と型名、nullableかどうかを取得して、ログ用の関数を自動生成する
                var logmessage = "$funName: "
                var types = ""
                functionDeclaration.parameters.forEach { param ->
                    val paramName: String? = param.name?.asString()
                    val paramTypeName = param.type.resolve().let { type ->
                        var name = type.declaration.qualifiedName?.asString()
                        if (type.isMarkedNullable && !name.isNullOrEmpty()) {
                            name = "$name?"
                        }
                        name
                    }

                    logmessage += "$paramName=\$$paramName "
                    types += "$paramName: $paramTypeName, "
                }
                types = types.removeSuffix(", ")
                // fun jp.co.test.MainActivity.logOnCreate(savedInstanceState: Bundle?) = Log.d("MainActivity", "savedInstanceState=$savedInstanceState"みたいになる想定
                appendLine(
                    """
                        fun $packageName.$className.log${upperCamelFunName}($types) = Log.d("$className", "$logmessage")
                    """.trimIndent()
                )
            }
        }

        // ソースコードファイルを生成する
        // appモジュールで使用し、debugでビルドした場合、生成されたソースコードは
        // app/build/generated/ksp/debug/kotlin/パッケージ名/ファイル名  で生成される
        codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = false,
                // incremental buildで前回生成したコードが消えないようにcontainingFileを渡す
                symbol.containingFile!!),
            packageName = packageName,
            fileName = "LogShroom${className}"
        ).use { outputStream ->
            outputStream.write(code.toByteArray())
        }
    }
}