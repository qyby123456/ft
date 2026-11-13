$ErrorActionPreference='Stop'
if(Test-Path -LiteralPath "$PSScriptRoot\server\s2-new-isolated-qa-world"){throw 'Use a fresh attempt directory; existing QA world must be preserved'}
$taskJava="$env:APPDATA\.minecraft\runtime\java-runtime-gamma-snapshot"
$launcher=(Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.8-bin\*\gradle-8.8\lib\gradle-launcher-8.8.jar" | Select-Object -First 1).FullName
$arguments=@('-Dfile.encoding=UTF-8','-cp',$launcher,'org.gradle.launcher.GradleMain','runGameTestServer','--offline','--no-daemon','--console=plain','--max-workers=1','-Dnet.minecraftforge.gradle.check.certs=false')
[pscustomobject]@{executable="$taskJava\bin\java.exe";arguments=$arguments;cwd="$PSScriptRoot\native-test";production_writes=$false} | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath "$PSScriptRoot\invocation.json" -Encoding utf8
$oldJava=$env:JAVA_HOME;$oldPath=$env:PATH
Push-Location "$PSScriptRoot\native-test"
try{
  $env:JAVA_HOME=$taskJava;$env:PATH="$taskJava\bin;$oldPath"
  & "$taskJava\bin\java.exe" @arguments 2>&1 | Tee-Object -FilePath "$PSScriptRoot\console.log"
  $resultCode=$LASTEXITCODE
  [pscustomobject]@{exit_code=$resultCode;finished=(Get-Date).ToString('o')} | ConvertTo-Json | Set-Content -LiteralPath "$PSScriptRoot\exit.json" -Encoding utf8
  if($resultCode -ne 0){throw "GameTest exit $resultCode"}
}finally{$env:JAVA_HOME=$oldJava;$env:PATH=$oldPath;Pop-Location}
