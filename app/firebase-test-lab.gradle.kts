import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

project.afterEvaluate {
  val setupGCloudProject = tasks.register("setupGCloudProject", Exec::class) {
    commandLine = "gcloud config set project github-client-25b47".split(' ')
    dependsOn(project.tasks.named("assembleDebugAndroidTest"))
  }

  val setupGCloudAccount = tasks.register("setupGCloudAccount", Exec::class) {
    val credentialsPath = createCredentialsFile()
    commandLine = "gcloud auth activate-service-account --key-file $credentialsPath".split(' ')

    dependsOn(setupGCloudProject)
  }

  var resultsFileToPull: String? = null

  val executeTestsInTestLab = tasks.register("executeInstrumentedTestsOnFirebase", Exec::class) {
    val appApk = "${project.buildDir}/outputs/apk/debug/app-debug.apk"
    val testApk = "${project.buildDir}/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
    val deviceName = "flame"
    val androidVersion = "29"
    val device = "model=$deviceName,version=$androidVersion,locale=en,orientation=portrait"
    val resultDir = DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())

    resultsFileToPull = "gs://test-lab-twsawhz0hy5am-h35y3vymzadax/$resultDir/$deviceName-$androidVersion-en-portrait/test_result_1.xml"

    commandLine =
      ("gcloud " +
        "firebase test android run " +
        "--app $appApk " +
        "--test $testApk " +
        "--device $device " +
        "--results-dir $resultDir " +
        "--no-performance-metrics")
        .split(' ')

    dependsOn(project.tasks.named("assembleDebugAndroidTest"))
    dependsOn(project.tasks.named("assembleDebug"))
    dependsOn(setupGCloudAccount)
  }

  val pullResults = tasks.register("pullFirebaseXmlResults", Exec::class) {
    dependsOn(executeTestsInTestLab)

    doFirst {
      commandLine = "gsutil cp $resultsFileToPull $buildDir/test-results/firebase-tests-results.xml".split(' ')
    }
  }

  tasks.register("runInstrumentedTestsOnFirebase") {
    dependsOn(executeTestsInTestLab)
    dependsOn(pullResults)
  }
}

fun createCredentialsFile(): String {
  val credentialsPath = "$projectDir/credentials.json"
  val credentials = System.getenv("GCLOUD_CREDENTIALS")
  if (credentials != null) {
    File(credentialsPath).writeText(credentials)
  }
  return credentialsPath
}

