$ErrorActionPreference = 'Stop'
$deliveryRoot = Split-Path $PSScriptRoot -Parent
$taskJavaHome = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
try {
    $env:JAVA_HOME = $taskJavaHome
    $env:PATH = (Join-Path $taskJavaHome 'bin') + ';' + $previousPath
    Push-Location (Join-Path $deliveryRoot 'forge')
    try {
        $stamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
        $logPath = Join-Path $deliveryRoot "qa\forge-run-$stamp.log"
        & (Join-Path $taskJavaHome 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' 'runGameTestServer' '--offline' '--console=plain' '-Dnet.minecraftforge.gradle.check.certs=false' "-PzoneR01RunDir=run-$stamp" 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
        Copy-Item -LiteralPath $logPath -Destination (Join-Path $deliveryRoot 'qa\forge-run.log')
        if ($exitCode -ne 0) { throw "zone-R01 isolated Forge QA failed: $exitCode" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
}
