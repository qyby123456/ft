param([string]$RunId = ('a01-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff')))
$ErrorActionPreference = 'Stop'
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$savedJavaHome = $env:JAVA_HOME
$savedPath = $env:PATH
try {
    $env:JAVA_HOME = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
    $env:PATH = (Join-Path $env:JAVA_HOME 'bin') + ';' + $savedPath
    $evidence = Join-Path $PSScriptRoot 'evidence'
    New-Item -ItemType Directory -Path $evidence -Force | Out-Null
    $log = Join-Path $evidence "forge-gametest-$RunId.log"
    Push-Location (Join-Path $PSScriptRoot 'native-test')
    try {
        & $gradle --offline --console plain '--max-workers=2' '-Dnet.minecraftforge.gradle.check.certs=false' "-PqaRun=$RunId" runGameTestServer 2>&1 | Tee-Object -FilePath $log
        $code = $LASTEXITCODE
        [ordered]@{run_id=$RunId; exit_code=$code; log="evidence/forge-gametest-$RunId.log"; production_save_used=$false} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $evidence 'native-run.json') -Encoding utf8
        if ($code -ne 0) { throw "A01 Forge QA failed ($code); evidence retained" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $savedJavaHome
    $env:PATH = $savedPath
}
