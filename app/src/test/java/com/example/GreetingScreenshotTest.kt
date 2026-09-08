package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.HackathonEntry
import com.example.data.HackathonStatus
import com.example.feature.hackathons.HackathonCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sample = HackathonEntry(
      id = 1,
      name = "AI Global Challenge",
      status = HackathonStatus.IN_PROGRESS,
      registrationDate = System.currentTimeMillis(),
      deadlineDate = System.currentTimeMillis() + (2L * 86400000L),
      projectLink = "https://github.com/example/demo"
    )

    composeTestRule.setContent { 
      MyApplicationTheme { 
        HackathonCard(entry = sample, onClick = {})
      } 
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}



