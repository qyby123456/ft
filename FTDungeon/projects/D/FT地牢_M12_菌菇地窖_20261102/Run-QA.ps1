param([switch]$Rebuild)

$ErrorActionPreference = 'Stop'
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$savedJavaHome = $env:JAVA_HOME
$savedPath = $env:PATH
$savedEncoding = $env:PYTHONIOENCODING
try {
    $env:JAVA_HOME = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
    $env:PATH = (Join-Path $env:JAVA_HOME 'bin') + ';' + $savedPath
    $env:PYTHONIOENCODING = 'utf-8'
    if ($Rebuild) {
        & $python (Join-Path $PSScriptRoot 'tools\build_m12.py')
        if ($LASTEXITCODE -ne 0) { throw 'M12 generation failed' }
    }
    & $python (Join-Path $PSScriptRoot 'tools\static_qa.py')
    if ($LASTEXITCODE -ne 0) { throw 'M12 static QA failed' }
    $runId = 'm12-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff')
    $log = Join-Path $PSScriptRoot "evidence\forge-gametest-$runId.log"
    Push-Location (Join-Path $PSScriptRoot 'native-test')
    try {
        & $gradle --offline --console plain '-Dnet.minecraftforge.gradle.check.certs=false' "-PqaRun=$runId" runGameTestServer 2>&1 | Tee-Object -FilePath $log
        $code = $LASTEXITCODE
        [ordered]@{run_id=$runId; exit_code=$code; log="evidence/forge-gametest-$runId.log"} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $PSScriptRoot 'evidence\native-run.json') -Encoding utf8
        if ($code -ne 0) { throw 'M12 native Forge QA failed; original log preserved' }
    } finally {
        Pop-Location
    }
    & $python (Join-Path $PSScriptRoot 'tools\render_m12.py')
    if ($LASTEXITCODE -ne 0) { throw 'M12 rendering failed' }
    & $python (Join-Path $PSScriptRoot 'tools\finalize_delivery.py')
    if ($LASTEXITCODE -ne 0) { throw 'M12 finalization failed' }
} finally {
    $env:JAVA_HOME = $savedJavaHome
    $env:PATH = $savedPath
    $env:PYTHONIOENCODING = $savedEncoding
}
