$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
if(Test-Path -LiteralPath "$PSScriptRoot\server\world"){throw 'Preserve prior world; use a fresh run directory'}
$progress=Get-Content -Raw -LiteralPath "$root\progress.json" | ConvertFrom-Json
$inputs=11..20 | ForEach-Object {
 $id="s3-$_"
 [pscustomobject]@{id=$id;candidate="$root\rooms\$id\$id.nbt";design="$root\rooms\$id\design.json";sha256=$progress.rooms.$id.sha256}
}
$inputs+= [pscustomobject]@{id='s3-merged';candidate="$root\rooms\s3-merged\s3-merged.nbt";design="$root\rooms\s3-merged\design.json";sha256=$progress.rooms.'s3-merged'.sha256}
$inputs | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath "$PSScriptRoot\inputs.json" -Encoding utf8NoBOM
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
