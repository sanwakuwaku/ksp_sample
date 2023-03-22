package jp.sanwakuwaku.logshroom

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class LogShroomProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return LogShroomProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}