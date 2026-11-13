$ErrorActionPreference='Stop'
if(Test-Path -LiteralPath "$PSScriptRoot\server\world"){throw 'Preserve existing test world; use a fresh directory'}
$java="$env:APPDATA\.minecraft\runtime\java-runtime-gamma-snapshot"
$launcher=(Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.8-bin\*\gradle-8.8\lib\gradle-launcher-8.8.jar" | Select-Object -First 1).FullName
$arguments=@('-Dfile.encoding=UTF-8','-cp',$launcher,'org.gradle.launcher.GradleMain','runGameTestServer','--offline','--no-daemon','--console=plain','--max-workers=1','-Dnet.minecraftforge.gradle.check.certs=false')
$oldJava=$env:JAVA_HOME;$oldPath=$env:PATH
Push-Location "$PSScriptRoot\native-test"
try{
 $env:JAVA_HOME=$java;$env:PATH="$java\bin;$oldPath"
 & "$java\bin\java.exe" @arguments 2>&1 | Tee-Object -FilePath "$PSScriptRoot\console.log"
 $code=$LASTEXITCODE
 [pscustomobject]@{exit_code=$code;finished=(Get-Date).ToString('o');production_writes=$false;server_heap='1536m';gradle_heap='512m'} | ConvertTo-Json | Set-Content -LiteralPath "$PSScriptRoot\exit.json" -Encoding utf8NoBOM
 if($code -ne 0){throw "GameTest exit $code; see console.log"}
}finally{$env:JAVA_HOME=$oldJava;$env:PATH=$oldPath;Pop-Location}
