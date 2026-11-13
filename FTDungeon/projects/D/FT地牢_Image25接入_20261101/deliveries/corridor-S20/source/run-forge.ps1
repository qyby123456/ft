$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$javaHome = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
$oldJavaHome = $env:JAVA_HOME
$oldPath = $env:PATH
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $oldPath
    Push-Location (Join-Path $root 'forge')
    try {
        $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
        $log = Join-Path $root "qa\forge-run-$stamp.log"
        & (Join-Path $javaHome 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' 'runGameTestServer' '--offline' '--console=plain' '-Dnet.minecraftforge.gradle.check.certs=false' "-Ps20RunDir=run-$stamp" 2>&1 | Tee-Object -FilePath $log
        Copy-Item -LiteralPath $log -Destination (Join-Path $root 'qa\forge-run.log')
        if ($LASTEXITCODE -ne 0) { throw "S20 isolated Forge QA failed: $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $oldJavaHome
    $env:PATH = $oldPath
}
