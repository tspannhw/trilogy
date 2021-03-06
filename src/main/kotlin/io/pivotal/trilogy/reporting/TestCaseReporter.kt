package io.pivotal.trilogy.reporting

import io.pivotal.trilogy.i18n.MessageCreator.getI18nMessage
import io.pivotal.trilogy.testproject.TestProjectResult

object TestCaseReporter {
    fun generateReport(result: TestProjectResult): String {
        if (result.didFail or result.fatalFailure)
            return listOf("[FAIL] ${result.failureMessage}", result.fatalFailureMessage, "FAILED").filterNotNull().joinToString("\n")

        return if (result.testCaseResults.all { it.didPass }) reportSuccess(result.testCaseResults) else reportFailure(result)
    }

    private fun reportFailure(result: TestProjectResult): String {
        return listOf(result.testCaseResults.failures, "FAILED", result.testCaseResults.digest).joinToString("\n")
    }

    private fun reportSuccess(result: List<TestCaseResult>) = "SUCCEEDED\n${result.digest}"

    private val List<TestCaseResult>.total: Int get() = this.fold(0) { accumulated, result -> accumulated + result.total }
    private val List<TestCaseResult>.passed: Int get() = this.fold(0) { accumulated, result -> accumulated + result.passed }
    private val List<TestCaseResult>.failed: Int get() = this.fold(0) { accumulated, result -> accumulated + result.failed }
    private val List<TestCaseResult>.digest: String get() = "Total: ${this.total}, Passed: ${this.passed}, Failed: ${this.failed}"
    private val List<TestCaseResult>.failures: String get() = this.map { it.failureDigest }.joinToString("\n")

    private val TestCaseResult.failureDigest: String get() {
        return this.testCaseFailure + this.failedTests.map { "[FAIL] ${this.testCaseName} - ${it.testName}:\n${it.displayMessage}" }.joinToString("\n")
    }
    private val TestResult.displayMessage: String get() = this.errorMessage!!.prependIndent("    ")

    private val TestProjectResult.fatalFailureMessage: String? get() {
        return if (this.fatalFailure) getI18nMessage("fatalFailure") else null
    }

    private val TestCaseResult.testCaseFailure: String get() {
        if (this.errorMessage == null) return ""
        return "[FAIL] ${this.testCaseName}:\n" + this.errorMessage.prependIndent("    ")
    }
}

