package com.example

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    println("--- SCANNING FOR ALL FILES ---")
    File(".").walkTopDown().forEach { file ->
      if (file.isFile && (file.name.contains("png") || file.name.contains("jpg") || file.name.contains("jpeg") || file.name.contains("model"))) {
        println("FOUND IMAGE OR MODEL FILE: ${file.absolutePath}")
      }
    }
    println("--- END OF SCAN ---")
    assertEquals(4, 2 + 2)
  }
}
