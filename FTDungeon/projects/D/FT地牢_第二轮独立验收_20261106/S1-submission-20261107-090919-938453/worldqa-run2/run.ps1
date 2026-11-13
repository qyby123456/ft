$ErrorActionPreference='Stop'
if(Test-Path -LiteralPath "$PSScriptRoot\server\world"){throw 'Preserve existing QA world; choose a new QA folder for reruns'}
$root=Split-Path $PSScriptRoot -Parent
$inputs=Get-ChildItem -LiteralPath $root -Directory|Where-Object Name -match '^\d\d-'
$records=@($inputs|ForEach-Object {
 $candidate=(Get-ChildItem -LiteralPath $_.FullName -Filter '*.nbt'|Select-Object -First 1).FullName
 [pscustomobject]@{id=$_.Name;candidate=$candidate;sha256=(Get-FileHash -LiteralPath $candidate -Algorithm SHA256).Hash.ToLower()}
})
$merged=Join-Path $root 'merged\s1-mossy-functional-11-20.nbt'
$records+= [pscustomobject]@{id='merged';candidate=$merged;sha256=(Get-FileHash -LiteralPath $merged -Algorithm SHA256).Hash.ToLower()}
$json=$records|ConvertTo-Json -Depth 5
[IO.File]::WriteAllText("$PSScriptRoot\inputs.json",$json,(New-Object Text.UTF8Encoding($false)))
$java="$env:APPDATA\.minecraft\runtime\java-runtime-gamma-snapshot"
$launcher=(Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.8-bin\*\gradle-8.8\lib\gradle-launcher-8.8.jar"|Select-Object -First 1).FullName
$arguments=@('-Dfile.encoding=UTF-8','-cp',$launcher,'org.gradle.launcher.GradleMain','runGameTestServer','--offline','--no-daemon','--console=plain','--max-workers=1','-Dnet.minecraftforge.gradle.check.certs=false')
$previousJava=$env:JAVA_HOME
Push-Location "$PSScriptRoot\native-test"
try {
 $env:JAVA_HOME=$java
 & "$java\bin\java.exe" @arguments 2>&1|Tee-Object -FilePath "$PSScriptRoot\console.log"
 $result=$LASTEXITCODE
 [IO.File]::WriteAllText("$PSScriptRoot\exit.json",(@{exit_code=$result;server_heap='1536m';gradle_heap='512m';production_writes=$false}|ConvertTo-Json),(New-Object Text.UTF8Encoding($false)))
 if($result){throw "GameTest exited $result; inspect console.log and world-report.json"}
} finally {$env:JAVA_HOME=$previousJava;Pop-Location}
