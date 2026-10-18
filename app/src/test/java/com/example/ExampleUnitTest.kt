package com.example

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.ui.pages.settings.handleCodeChangeWithAutoTab
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testAutoTab_lineWithTab() {
    val initial = TextFieldValue("\tconsole.log('hi');", TextRange(19, 19))
    val afterEnter = TextFieldValue("\tconsole.log('hi');\n", TextRange(20, 20))
    val result = handleCodeChangeWithAutoTab(initial, afterEnter)

    assertEquals("\tconsole.log('hi');\n\t", result.text)
    assertEquals(TextRange(21, 21), result.selection)
  }

  @Test
  fun testAutoTab_lineWithSpaces() {
    val initial = TextFieldValue("    const x = 10;", TextRange(17, 17))
    val afterEnter = TextFieldValue("    const x = 10;\n", TextRange(18, 18))
    val result = handleCodeChangeWithAutoTab(initial, afterEnter)

    assertEquals("    const x = 10;\n    ", result.text)
    assertEquals(TextRange(22, 22), result.selection)
  }

  @Test
  fun testAutoTab_lineEndingWithBrace() {
    val initial = TextFieldValue("function test() {", TextRange(17, 17))
    val afterEnter = TextFieldValue("function test() {\n", TextRange(18, 18))
    val result = handleCodeChangeWithAutoTab(initial, afterEnter)

    assertEquals("function test() {\n    ", result.text)
    assertEquals(TextRange(22, 22), result.selection)
  }

  @Test
  fun testAutoTab_betweenBraces() {
    val initial = TextFieldValue("function test() {}", TextRange(17, 17))
    val afterEnter = TextFieldValue("function test() {\n}", TextRange(18, 18))
    val result = handleCodeChangeWithAutoTab(initial, afterEnter)

    assertEquals("function test() {\n    \n}", result.text)
    assertEquals(TextRange(22, 22), result.selection)
  }

  @Test
  fun testAutoTab_noIndentLine() {
    val initial = TextFieldValue("console.log('hello');", TextRange(21, 21))
    val afterEnter = TextFieldValue("console.log('hello');\n", TextRange(22, 22))
    val result = handleCodeChangeWithAutoTab(initial, afterEnter)

    assertEquals("console.log('hello');\n", result.text)
    assertEquals(TextRange(22, 22), result.selection)
  }

  @Test
  fun testValidateUserscriptSyntax_unclosedBrace() {
    val code = "function test() {\n    console.log('hi');"
    val issues = com.example.ui.pages.settings.validateUserscriptSyntax(code)
    assertTrue("Should detect unclosed brace", issues.any { it.message.contains("Unclosed '{'") })
    assertEquals(1, issues.first { it.message.contains("Unclosed '{'") }.line)
  }

  @Test
  fun testValidateUserscriptSyntax_unterminatedString() {
    val code = "const msg = 'hello world;\nconst y = 10;"
    val issues = com.example.ui.pages.settings.validateUserscriptSyntax(code)
    assertTrue("Should detect unterminated string", issues.any { it.message.contains("Unterminated single-quote") })
    assertEquals(1, issues.first { it.message.contains("Unterminated") }.line)
  }

  @Test
  fun testValidateUserscriptSyntax_missingHeaderClose() {
    val code = "// ==UserScript==\n// @name Test\nconsole.log(1);"
    val issues = com.example.ui.pages.settings.validateUserscriptSyntax(code)
    assertTrue("Should detect missing header closing tag", issues.any { it.message.contains("Missing closing '// ==/UserScript=='") })
  }

  @Test
  fun testValidateUserscriptSyntax_validCode() {
    val code = "// ==UserScript==\n// @name Test\n// @match *://*/*\n// ==/UserScript==\n(function() { console.log('ok'); })();"
    val issues = com.example.ui.pages.settings.validateUserscriptSyntax(code)
    assertTrue("Valid code should have no errors", issues.none { it.isError })
  }

  @Test
  fun testLineAndColumnCalculation() {
    val text = "first line\nsecond line\nthird"
    val (l1, c1) = com.example.ui.pages.settings.getLineAndColumn(text, 0)
    assertEquals(1, l1)
    assertEquals(1, c1)

    // Second line starts at index 11
    val (l2, c2) = com.example.ui.pages.settings.getLineAndColumn(text, 11)
    assertEquals(2, l2)
    assertEquals(1, c2)

    val (l2_c4, c2_c4) = com.example.ui.pages.settings.getLineAndColumn(text, 14)
    assertEquals(2, l2_c4)
    assertEquals(4, c2_c4)
  }

  @Test
  fun testGetCharIndexForLine() {
    val text = "first line\nsecond line\nthird line"
    assertEquals(0, com.example.ui.pages.settings.getCharIndexForLine(text, 1))
    assertEquals(11, com.example.ui.pages.settings.getCharIndexForLine(text, 2))
    assertEquals(23, com.example.ui.pages.settings.getCharIndexForLine(text, 3))
  }

  @Test
  fun testDefaultTemplateNamespaceAndSyntax() {
    val template = com.example.model.UserScript.createDefaultTemplate()
    assertTrue(
        "Default template should use GitHub repository URL as namespace",
        template.code.contains("// @namespace    https://www.github.com/Green0Field/DevBrowser")
    )
    val issues = com.example.ui.pages.settings.validateUserscriptSyntax(template.code)
    assertTrue("Default template should have no syntax issues", issues.none { it.isError })
  }

  @Test
  fun testVersionComparisonLogic() {
    assertTrue("v0.2 should be newer than 0.1", com.example.data.GitHubUpdateApiClient.isVersionNewer("v0.2", "0.1"))
    assertTrue("0.2.0 should be newer than 0.1", com.example.data.GitHubUpdateApiClient.isVersionNewer("0.2.0", "0.1"))
    assertTrue("1.0 should be newer than 0.1", com.example.data.GitHubUpdateApiClient.isVersionNewer("1.0", "0.1"))
    assertTrue("0.1.1 should be newer than 0.1", com.example.data.GitHubUpdateApiClient.isVersionNewer("0.1.1", "0.1"))
    assertFalse("v0.1 should not be newer than 0.1", com.example.data.GitHubUpdateApiClient.isVersionNewer("v0.1", "0.1"))
    assertFalse("0.1 should not be newer than 0.1", com.example.data.GitHubUpdateApiClient.isVersionNewer("0.1", "0.1"))
    assertFalse("0.0.9 should not be newer than 0.1", com.example.data.GitHubUpdateApiClient.isVersionNewer("0.0.9", "0.1"))
  }

  @Test
  fun testAutoUpdateFrequencyAndCategoryOrder() {
    assertEquals(
        com.example.model.AutoUpdateCheckFrequency.EACH_STARTUP,
        com.example.model.AutoUpdateCheckFrequency.fromThreshold(1)
    )
    assertEquals(
        com.example.model.AutoUpdateCheckFrequency.EVERY_2_STARTUPS,
        com.example.model.AutoUpdateCheckFrequency.fromThreshold(2)
    )
    assertEquals(
        com.example.model.AutoUpdateCheckFrequency.MANUAL_ONLY,
        com.example.model.AutoUpdateCheckFrequency.fromThreshold(0)
    )

    // Verify AUTO_UPDATE category exists and is placed above ABOUT
    assertTrue(
        "Auto update must be placed above About category",
        com.example.model.SettingsCategory.AUTO_UPDATE.ordinal < com.example.model.SettingsCategory.ABOUT.ordinal
    )
    assertEquals(
        com.example.model.SettingsCategory.AUTO_UPDATE,
        com.example.model.SettingsCategory.fromUrl("devbrowser:settings/auto-update")
    )
  }
}



