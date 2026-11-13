param([switch]$Rebuild)
$ErrorActionPreference = 'Stop'
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$savedJavaHome = $env:JAVA_HOME
$savedPath = $env:PATH
try {
    $env:JAVA_HOME = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
    $env:PATH = (Join-Path $env:JAVA_HOME 'bin') + ';' + $savedPath
    if ($Rebuild) {
        & node (Join-Path $skill 'scripts\run-build.mjs') (Join-Path $PSScriptRoot 'tools\recipe.mjs') --out-dir $PSScriptRoot --name room --data-version 3465 --json
        if ($LASTEXITCODE -ne 0) { throw 'Minecraft Builder generation failed' }
    }
    & node (Join-Path $PSScriptRoot 'tools\static-qa.mjs')
    if ($LASTEXITCODE -ne 0) { throw 'Static QA failed' }
    $runId = 'corridor-m12-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff')
    $log = Join-Path $PSScriptRoot "qa\forge-$runId.log"
    Push-Location (Join-Path $PSScriptRoot 'native-test')
    try {
        & $gradle --offline --console plain '-Dnet.minecraftforge.gradle.check.certs=false' "-PqaRun=$runId" runGameTestServer 2>&1 | Tee-Object -FilePath $log
        $exitCode = $LASTEXITCODE
        [ordered]@{ run_id=$runId; exit_code=$exitCode; log=$log; production_save_used=$false } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $PSScriptRoot 'qa\native-run.json') -Encoding utf8
        if ($exitCode -ne 0) {
            $hash = (Get-FileHash -LiteralPath (Join-Path $PSScriptRoot 'room.nbt') -Algorithm SHA256).Hash.ToLowerInvariant()
            [ordered]@{ status='FAIL'; sha256=$hash; exit_code=$exitCode; log=$log } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $PSScriptRoot 'qa\native.json') -Encoding utf8
            throw "Forge isolated QA failed; see $log"
        }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $savedJavaHome
    $env:PATH = $savedPath
}
