param([switch]$Rebuild, [ValidateRange(1024, 65535)][int]$PreviewPort = 15314)

$ErrorActionPreference = 'Stop'
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
$javaHome = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
if ($env:JAVA_HOME_17) { $javaHome = $env:JAVA_HOME_17 }
$previousJavaHome = $env:JAVA_HOME
$previousEncoding = $env:PYTHONIOENCODING
$previousPort = $env:S14_PREVIEW_PORT
$previousLocation = Get-Location
$previewProcess = $null
try {
    $env:JAVA_HOME = $javaHome
    $env:PYTHONIOENCODING = 'utf-8'
    $env:S14_PREVIEW_PORT = [string]$PreviewPort
    if ($Rebuild) {
        Set-Location -LiteralPath $skill
        & node 'scripts/run-build.mjs' (Join-Path $PSScriptRoot 'tools/build-s14.mjs') --out-dir $PSScriptRoot --name room --data-version 3465 --json
        if ($LASTEXITCODE -ne 0) { throw 'Minecraft Builder generation failed' }
    }
    Set-Location -LiteralPath $PSScriptRoot
    & node (Join-Path $PSScriptRoot 'tools/audit-s14.mjs')
    if ($LASTEXITCODE -ne 0) { throw 'Static QA failed' }
    Set-Location -LiteralPath (Join-Path $PSScriptRoot 'native-test')
    $nativeLog = Join-Path $PSScriptRoot ('evidence/forge-gametest-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.log')
    & (Join-Path $javaHome 'bin/java.exe') -classpath 'gradle/wrapper/gradle-wrapper.jar' org.gradle.wrapper.GradleWrapperMain runGameTestServer --offline '-Dnet.minecraftforge.gradle.check.certs=false' --console=plain 2>&1 | Tee-Object -FilePath $nativeLog
    if ($LASTEXITCODE -ne 0) { throw 'Native Forge QA failed' }
    Set-Location -LiteralPath $PSScriptRoot
    $serverScript = Join-Path $PSScriptRoot 'tools/preview-server.mjs'
    $previewProcess = Start-Process -FilePath (Get-Command node).Source -ArgumentList @('"' + $serverScript + '"') -WorkingDirectory $skill -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $PSScriptRoot 'evidence/preview-server.log') -RedirectStandardError (Join-Path $PSScriptRoot 'evidence/preview-server-error.log')
    $previewUrl = "http://127.0.0.1:$PreviewPort/viewer.html"
    $ready = $false
    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        if ($previewProcess.HasExited) { throw 'Preview server exited; check evidence/preview-server-error.log' }
        try {
            $response = Invoke-WebRequest -Uri $previewUrl -TimeoutSec 2
            if ($response.StatusCode -eq 200) { $ready = $true; break }
        } catch {}
        Start-Sleep -Seconds 1
    }
    if (!$ready) { throw 'Preview server startup timed out' }
    & node (Join-Path $PSScriptRoot 'tools/capture-preview.cjs') $previewUrl
    if ($LASTEXITCODE -ne 0) { throw 'Preview capture QA failed' }
    & $python (Join-Path $PSScriptRoot 'tools/finalize-delivery.py') --native-log $nativeLog
    if ($LASTEXITCODE -ne 0) { throw 'Delivery finalization failed' }
} finally {
    if ($previewProcess -and !$previewProcess.HasExited) { Stop-Process -Id $previewProcess.Id }
    Set-Location -LiteralPath $previousLocation.Path
    $env:JAVA_HOME = $previousJavaHome
    $env:PYTHONIOENCODING = $previousEncoding
    $env:S14_PREVIEW_PORT = $previousPort
}
