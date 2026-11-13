param([string]$Run='worldqa-natural')
$ErrorActionPreference='Stop'
$destination=Join-Path $PSScriptRoot $Run
if(Test-Path "$destination/exit.json"){throw 'Do not overwrite a completed run'}
$java="$env:APPDATA/.minecraft/runtime/java-runtime-gamma-snapshot"
$launcher=(Get-ChildItem "$env:USERPROFILE/.gradle/wrapper/dists/gradle-8.8-bin/*/gradle-8.8/lib/gradle-launcher-8.8.jar"|Select-Object -First 1).FullName
$arguments=@('-Dfile.encoding=UTF-8','-cp',$launcher,'org.gradle.launcher.GradleMain','runGameTestServer','--offline','--no-daemon','--console=plain','--max-workers=1','-Dnet.minecraftforge.gradle.check.certs=false')
$previousJava=$env:JAVA_HOME
Push-Location "$destination/native-test"
try {
 $env:JAVA_HOME=$java
 & "$java/bin/java.exe" @arguments 2>&1|Tee-Object -FilePath "$destination/console.log"
 $result=$LASTEXITCODE
 [IO.File]::WriteAllText("$destination/exit.json",(@{exit_code=$result;server_heap='1536m';gradle_heap='512m';production_writes=$false}|ConvertTo-Json),(New-Object Text.UTF8Encoding($false)))
 if($result){throw "Natural GameTest exited $result"}
} finally {$env:JAVA_HOME=$previousJava;Pop-Location}
