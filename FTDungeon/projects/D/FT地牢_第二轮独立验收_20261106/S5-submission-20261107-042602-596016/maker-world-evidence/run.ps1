$ErrorActionPreference='Stop'
if(Test-Path -LiteralPath "$PSScriptRoot\server\world"){throw 'Existing QA world: use a new isolated QA folder'}
$javaRoot="$env:APPDATA\.minecraft\runtime\java-runtime-gamma-snapshot"
$launcher=(Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.8-bin\*\gradle-8.8\lib\gradle-launcher-8.8.jar" | Select-Object -First 1).FullName
$arguments=@('-Dfile.encoding=UTF-8','-cp',$launcher,'org.gradle.launcher.GradleMain','runGameTestServer','--offline','--no-daemon','--console=plain','--max-workers=1','-Dnet.minecraftforge.gradle.check.certs=false')
$oldJava=$env:JAVA_HOME;$oldPath=$env:PATH
Push-Location "$PSScriptRoot\native-test"
try{
  $env:JAVA_HOME=$javaRoot;$env:PATH="$javaRoot\bin;$oldPath"
  & "$javaRoot\bin\java.exe" @arguments 2>&1 | Tee-Object -FilePath "$PSScriptRoot\console.log"
  [pscustomobject]@{exit_code=$LASTEXITCODE;finished=(Get-Date).ToString('o')} | ConvertTo-Json | Set-Content -LiteralPath "$PSScriptRoot\exit.json" -Encoding utf8
}finally{$env:JAVA_HOME=$oldJava;$env:PATH=$oldPath;Pop-Location}
