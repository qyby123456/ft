$ErrorActionPreference='Stop'
$java="$env:APPDATA\.minecraft\runtime\java-runtime-gamma-snapshot"
$launcher=(Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.8-bin\*\gradle-8.8\lib\gradle-launcher-8.8.jar"|Select-Object -First 1).FullName
$arguments=@('-Xmx768m','-Dfile.encoding=UTF-8','-cp',$launcher,'org.gradle.launcher.GradleMain','runGameTestServer','--offline','--no-daemon','--console=plain','--max-workers=1','-Dnet.minecraftforge.gradle.check.certs=false')
$oldJava=$env:JAVA_HOME;$oldPath=$env:PATH
Push-Location "$PSScriptRoot\native-test"
try {
 $env:JAVA_HOME=$java;$env:PATH="$java\bin;$oldPath"
 & "$java\bin\java.exe" @arguments 2>&1 | Tee-Object -FilePath "$PSScriptRoot\console.log"
 $result=$LASTEXITCODE
 @{exit_code=$result;scope='S4 isolated test world only';heap='server1536M; Gradle768M';finished=(Get-Date).ToString('o')}|ConvertTo-Json|Set-Content "$PSScriptRoot\exit.json" -Encoding utf8
 if($result -ne 0){throw "World test failed: $result"}
} finally {$env:JAVA_HOME=$oldJava;$env:PATH=$oldPath;Pop-Location}
