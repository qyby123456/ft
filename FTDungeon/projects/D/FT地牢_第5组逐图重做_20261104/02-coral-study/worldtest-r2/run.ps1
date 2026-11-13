$ErrorActionPreference='Stop'
if(Test-Path -LiteralPath "$PSScriptRoot\server\s5-02-new-isolated-water-world"){throw 'World exists: do not rerun in an old world; clone source/config into another fresh QA directory'}
$hash=(Get-FileHash -LiteralPath "$PSScriptRoot\frozen.nbt" -Algorithm SHA256).Hash
if($hash -ne '29fb0a6261847b1817cc5edcbb72aa2824c7e2af1e527bb70d041a7fbd80d552'){throw 'NBT drift'}
$java="$env:APPDATA\.minecraft\runtime\java-runtime-gamma-snapshot"
$launcher=(Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.8-bin\*\gradle-8.8\lib\gradle-launcher-8.8.jar" | Select-Object -First 1).FullName
$arguments=@('-Dfile.encoding=UTF-8','-cp',$launcher,'org.gradle.launcher.GradleMain','runGameTestServer','--offline','--no-daemon','--console=plain','--max-workers=1','-Dnet.minecraftforge.gradle.check.certs=false')
[pscustomobject]@{executable="$java\bin\java.exe";arguments=$arguments;cwd="$PSScriptRoot\native-test";expected_nbt_sha256=$hash;requested_ticks=1200;randomTickSpeed=3;knownShape=$false;production_writes=$false} | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath "$PSScriptRoot\invocation.json" -Encoding utf8
$oldJava=$env:JAVA_HOME;$oldPath=$env:PATH
Push-Location "$PSScriptRoot\native-test"
try{
  $env:JAVA_HOME=$java;$env:PATH="$java\bin;$oldPath"
  & "$java\bin\java.exe" @arguments 2>&1 | Tee-Object -FilePath "$PSScriptRoot\console.log"
  $code=$LASTEXITCODE
  [pscustomobject]@{exit_code=$code;finished=(Get-Date).ToString('o')} | ConvertTo-Json | Set-Content -LiteralPath "$PSScriptRoot\exit.json" -Encoding utf8
  if($code -ne 0){throw "GameTest exit $code; see console.log"}
}finally{$env:JAVA_HOME=$oldJava;$env:PATH=$oldPath;Pop-Location}
