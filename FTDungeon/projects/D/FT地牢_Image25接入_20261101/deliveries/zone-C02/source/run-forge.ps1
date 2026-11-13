$ErrorActionPreference = 'Stop'
$deliveryRoot = Split-Path $PSScriptRoot -Parent
$javaRoot = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
try {
    $env:JAVA_HOME = $javaRoot
    $env:PATH = (Join-Path $javaRoot 'bin') + ';' + $previousPath
    Push-Location (Join-Path $deliveryRoot 'forge')
    try {
        $stamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
        $log = Join-Path $deliveryRoot "qa\forge-$stamp.log"
        & (Join-Path $javaRoot 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' 'runGameTestServer' '--offline' '--console=plain' '-Dnet.minecraftforge.gradle.check.certs=false' "-Pc02RunDir=run-$stamp" 2>&1 | Tee-Object -FilePath $log
        $exitCode = $LASTEXITCODE
        if ($exitCode -ne 0) { throw "C02 isolated Forge QA failed: $exitCode; evidence: $log" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
}

