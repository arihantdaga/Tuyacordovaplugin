const fs = require("fs");

const ANDROID_DIR = "platforms/android/";

fixGradlePropertiesFile();

function fixGradlePropertiesFile() {
  const gradleFilePath = ANDROID_DIR + "gradle.properties";
  let gradlePropertiesFile = fs.readFileSync(
    gradleFilePath,
    "utf-8"
  );
  if (gradlePropertiesFile.indexOf("android.useAndroidX") === -1) {
    gradlePropertiesFile += `\nandroid.useAndroidX=true`
  }
  if (gradlePropertiesFile.indexOf("android.enableJetifier") === -1) {
    gradlePropertiesFile += `\nandroid.enableJetifier=true`
  }
  fs.writeFileSync(gradleFilePath, gradlePropertiesFile, 'utf-8');
}
