package com.example.emergencyaid

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileActivityTest {

    // Launches ProfileActivity before each test
    @get:Rule
    val activityRule = ActivityScenarioRule(ProfileActivity::class.java)

    @Test
    fun validateProfileInformationLoaded() {
        // Wait for Firebase async data load (2 seconds)
        Thread.sleep(2000)

        // ✅ Verify key profile details are displayed on the screen
        onView(withText("Personal Info")).check(matches(isDisplayed()))
        onView(withText("Medical Info")).check(matches(isDisplayed()))

        // Example fields (based on what is created dynamically)
        onView(withText("Name:")).check(matches(isDisplayed()))
        onView(withText("Email:")).check(matches(isDisplayed()))
        onView(withText("Contact Number:")).check(matches(isDisplayed()))
        onView(withText("Age:")).check(matches(isDisplayed()))
        onView(withText("Location:")).check(matches(isDisplayed()))

        // ✅ Verify Edit Profile button exists
        onView(withText("Edit Profile")).check(matches(isDisplayed()))

        // ✅ Verify Logout button exists
        onView(withText("Logout")).check(matches(isDisplayed()))
    }
    @Test
    fun validateProfileUpdateFunctionality() {
        // Wait for Firebase to load profile data
        Thread.sleep(2000)

        // ✅ Click on Edit Profile button
        onView(withText("Edit Profile")).perform(click())

        // ✅ Change name and contact info (example)
        onView(withHint("Name")).perform(clearText(), typeText("John Test Updated"))
        onView(withHint("Contact Number")).perform(clearText(), typeText("09123456789"))

        // ✅ Close keyboard and save changes
        closeSoftKeyboard()
        onView(withText("Save Changes")).perform(click())

        // Wait for update to process
        Thread.sleep(1500)

        // ✅ Verify that updated name and contact number are displayed
        onView(withText("Name: John Test Updated")).check(matches(isDisplayed()))
        onView(withText("Contact Number: 09123456789")).check(matches(isDisplayed()))
    }

    @Test
    fun validatePasswordUpdate() {
        // Wait for profile to load
        Thread.sleep(2000)

        // ✅ Click on "Change Password" option
        onView(withText("Change Password")).perform(scrollTo(), click())

        // ✅ Enter current password and new password (example)
        onView(withHint("Current Password")).perform(typeText("oldPassword123"))
        onView(withHint("New Password")).perform(typeText("newPassword123"))
        onView(withHint("Confirm New Password")).perform(typeText("newPassword123"))
        closeSoftKeyboard()

        // ✅ Click on Save/Update button
        onView(withText("Update Password")).perform(click())

        // Wait for password update
        Thread.sleep(1500)

        // ✅ Verify confirmation message or success indicator
        onView(withText("Password changed successfully")).check(matches(isDisplayed()))
    }
    @Test
    fun validateAccountDeletion() {
        // Wait for profile data to load
        Thread.sleep(2000)

        // ✅ Scroll to and click the "Delete Account" option
        onView(withText("Delete Account")).perform(scrollTo(), click())

        // ✅ Confirm that the delete confirmation dialog appears
        onView(withText("Are you sure you want to delete your account?"))
            .check(matches(isDisplayed()))

        // ✅ Click the "Yes" button on the confirmation dialog
        onView(withText("Yes")).perform(click())

        // Wait for Firebase deletion and navigation
        Thread.sleep(2000)

        // ✅ Verify the app navigates to the LoginActivity (check by login screen text)
        onView(withText("Login")).check(matches(isDisplayed()))

        // ✅ Optionally check that a success message appears
        onView(withText("Account deleted successfully"))
            .check(matches(isDisplayed()))
    }
}
