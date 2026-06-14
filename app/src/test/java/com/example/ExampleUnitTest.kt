package com.example

import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.runBlocking
import com.example.api.RetrofitClient
import com.example.api.GenerateContentRequest
import com.example.api.Content
import com.example.api.Part

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun check_gemini_api_key() {
    val key = BuildConfig.GEMINI_API_KEY
    System.out.println("--- UNIT TEST: GEMINI API KEY IS: '$key' (length: ${key.length}) ---")
    assertNotEquals("MY_GEMINI_API_KEY", key)
    assertFalse(key.isBlank())
  }

  @Test
  fun test_live_gemini_api_call() = runBlocking {
    val key = BuildConfig.GEMINI_API_KEY
    val service = RetrofitClient.service
    val request = GenerateContentRequest(
        contents = listOf(Content(parts = listOf(Part(text = "Hello, reply with exactly the word: 'SUCCESS'"))))
    )
    try {
        val result = service.generateContent(key, request)
        val text = result.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        System.out.println("--- LIVE API CALL RESULT: '$text' ---")
        assertNotNull(text)
    } catch (e: retrofit2.HttpException) {
        System.out.println("--- LIVE API CALL FAILED WITH HTTP ${e.code()} ---")
        val errorBody = e.response()?.errorBody()?.string()
        System.out.println("--- ERROR BODY: $errorBody ---")
        fail("API call failed with HTTP ${e.code()}: $errorBody")
    } catch (e: Exception) {
        System.out.println("--- LIVE API CALL FAILED ---")
        e.printStackTrace()
        fail("API call failed with: ${e.message}")
    }
  }
}
