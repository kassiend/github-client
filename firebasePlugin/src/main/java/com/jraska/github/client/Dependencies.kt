package com.jraska.github.client

import org.gradle.api.Plugin
import org.gradle.api.Project

class Dependencies : Plugin<Project> {
  override fun apply(project: Project) {
    // Possibly common dependencies or can stay empty
  }

  companion object {
    val FIREBASE_MESSAGING = "com.google.firebase:firebase-messaging:20.1.7"
  }
}
