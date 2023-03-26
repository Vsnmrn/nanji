plugins {
  id("com.android.application") version "7.4.2" apply false
  id("com.android.library") version "7.4.2" apply false
  id("org.jetbrains.kotlin.android") version "1.8.10" apply false
}

tasks.register<Delete>("clean") {
  delete(rootProject.buildDir)
}