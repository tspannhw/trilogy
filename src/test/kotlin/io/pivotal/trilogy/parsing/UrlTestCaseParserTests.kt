package io.pivotal.trilogy.parsing

import io.pivotal.trilogy.Fixtures
import io.pivotal.trilogy.ResourceHelper
import org.jetbrains.spek.api.Spek
import kotlin.test.assertFails
import kotlin.test.expect

class UrlTestCaseParserTests : Spek({

    it("reads an existing file into a test case") {
        val testCase = Fixtures.getTestCase("degenerate")
        expect (testCase) { UrlTestCaseParser(ResourceHelper.getResourceUrl("/testcases/degenerate.stt")).getTestCase() }
    }

    it("fails for non-readable files") {
        assertFails { UrlTestCaseParser(ResourceHelper.getResourceUrl("/foo/bar.stt")).getTestCase() }
    }

})
