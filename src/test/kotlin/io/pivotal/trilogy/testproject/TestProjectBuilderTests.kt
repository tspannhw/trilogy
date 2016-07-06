package io.pivotal.trilogy.testproject

import io.pivotal.trilogy.ResourceHelper
import io.pivotal.trilogy.shouldStartWith
import io.pivotal.trilogy.shouldThrow
import io.pivotal.trilogy.testcase.TestCaseHooks
import org.amshove.kluent.AnyException
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import kotlin.test.expect

class TestProjectBuilderTests : Spek({
    it("should throw exception when building project from an empty directory") {
        val projectUrl = ResourceHelper.getResourceUrl("/projects/blank/");

        { TestProjectBuilder.build(UrlTestProjectResourceLocator(projectUrl)) } shouldThrow AnyException
    }

    it("should throw an exception when building a project with no tests") {
        val projectUrl = ResourceHelper.getResourceUrl("/projects/no_tests/");

        { TestProjectBuilder.build(UrlTestProjectResourceLocator(projectUrl)) } shouldThrow AnyException
    }

    it("should create a project with a single test case") {
        val projectUrl = ResourceHelper.getResourceUrl("/projects/single_testcase/")
        val project = TestProjectBuilder.build(UrlTestProjectResourceLocator(projectUrl))

        expect(1) { project.testCases.count() }
        project.testCases.first().apply {
            procedureName shouldEqual "EXAMPLE_PROCEDURE"
            description shouldEqual "Example"
            hooks shouldEqual TestCaseHooks(emptyList(), emptyList(), emptyList(), emptyList())
            tests.count() shouldEqual 2
            tests.first().apply {
                description shouldEqual "Output should echo the input"
                argumentTable.inputArgumentNames shouldEqual listOf("V_IN")
                argumentTable.outputArgumentNames shouldEqual listOf("V_OUT")
                argumentTable.inputArgumentValues.count() shouldEqual 3
                argumentTable.inputArgumentValues.first() shouldEqual listOf("1243")
                argumentTable.outputArgumentValues.first() shouldEqual listOf("1243")
            }
        }

        expect(0) { project.fixtures.setupFixtureCount }
        expect(0) { project.fixtures.teardownFixtureCount }
        expect(emptyList()) { project.sourceScripts }
        expect(null) { project.schema }
    }

    it("should create a project with source scripts") {
        val projectUrl = ResourceHelper.getResourceUrl("/projects/simple/")
        val project = TestProjectBuilder.build(UrlTestProjectResourceLocator(projectUrl))

        expect(2) { project.sourceScripts.count() }
        project.sourceScripts[0] shouldStartWith "CREATE OR REPLACE PROCEDURE EXAMPLE$"
        project.sourceScripts[1] shouldStartWith "CREATE OR REPLACE PROCEDURE EXAMPLE_PROCEDURE"
    }

    it("should create a project with a schema") {
        val projectUrl = ResourceHelper.getResourceUrl("/projects/schema/")
        val project = TestProjectBuilder.build(UrlTestProjectResourceLocator(projectUrl))

        project.schema!!.shouldStartWith("CREATE TABLE CLIENTS")
    }

    it("should create a project with fixtures") {
        val projectUrl = ResourceHelper.getResourceUrl("/projects/setup_teardown/")
        val project = TestProjectBuilder.build(UrlTestProjectResourceLocator(projectUrl))

        val fixtures = project.fixtures
        fixtures.getSetupFixtureByName("Reset client balance") shouldStartWith "UPDATE CLIENTS SET BALANCE=0 WHERE ID=66778899"
        fixtures.getTeardownFixtureByName("Remove transactions") shouldStartWith "DELETE FROM TRANSACTIONS"
    }
})
