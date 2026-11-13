$ErrorActionPreference='Stop'
if(Test-Path -LiteralPath "$PSScriptRoot\server\world\level.dat"){throw 'Preserve existing evidence; use a fresh run directory'}
$java="$env:APPDATA\.minecraft\runtime\java-runtime-gamma-snapshot"
$launcher=(Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.8-bin\*\gradle-8.8\lib\gradle-launcher-8.8.jar" | Select-Object -First 1).FullName
$oldJava=$env:JAVA_HOME;$oldPath=$env:PATH
Push-Location "$PSScriptRoot\native-test"
try {
 $env:JAVA_HOME=$java;$env:PATH="$java\bin;$oldPath"
 & "$java\bin\java.exe" '-Dfile.encoding=UTF-8' '-cp' $launcher 'org.gradle.launcher.GradleMain' 'runGameTestServer' '--offline' '--no-daemon' '--console=plain' '--max-workers=1' '-Dnet.minecraftforge.gradle.check.certs=false' 2>&1 | Tee-Object -FilePath "$PSScriptRoot\console.log"
 $code=$LASTEXITCODE
 @{exit_code=$code;finished=(Get-Date).ToString('o');production_writes=$false} | ConvertTo-Json | Set-Content -LiteralPath "$PSScriptRoot\exit.json" -Encoding utf8NoBOM
 if($code -ne 0){throw "GameTest exit $code"}
} finally {$env:JAVA_HOME=$oldJava;$env:PATH=$oldPath;Pop-Location}
