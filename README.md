# veriffTask

It demonstrate SDK with camera library:

You can recognize text from a document like ID Card using back camera of the device or detect face of a single individual and provide it's picture using front camera

Installation
./gradlew clean publishToMavenLocal
Gradle Kotlin
implementation("com.veriff.sdk:sdk:1.0.0-SNAPSHOT")
Gradle Groovy
implementation 'com.veriff.sdk:sdk:1.0.0-SNAPSHOT'
Maven
<dependency>
    <groupId>com.veriff.sdk</groupId>
    <artifactId>sdk</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

Usage examples
Activity Result API (see https://developer.android.com/training/basics/intents/result)

Detect Face/Document
class MainActivity : AppCompatActivity() {

    private fun launchSDKActivity(action: String) {

        resultLauncher.launch(Intent(this, SDKActivity::class.java).apply {
            putExtra(BundleConstants.ACTION, action)
        })
    }
    
     private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                when {
                    data?.extras?.containsKey(BundleConstants.RESULT_URI) == true -> {
                        data.extras?.getString(BundleConstants.RESULT)?.let { updateData(it) }
                        data.extras?.getString(BundleConstants.RESULT_URI)?.let { updateImage(it) }
                    }
                }
            }
        }
}
