$ErrorActionPreference = 'Stop'
$p05Root = Split-Path $PSScriptRoot -Parent
$p05Java = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
$previousJava = $env:JAVA_HOME
$previousPath = $env:PATH
try {
    $env:JAVA_HOME = $p05Java
    $env:PATH = (Join-Path $p05Java 'bin') + ';' + $previousPath
    Push-Location (Join-Path $p05Root 'forge')
    try {
        $runStamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
        $runLog = Join-Path $p05Root "qa\forge-run-$runStamp.log"
        & (Join-Path $p05Java 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' 'runGameTestServer' '--offline' '--console=plain' '-Dnet.minecraftforge.gradle.check.certs=false' "-Pp05RunDir=run-$runStamp" 2>&1 | Tee-Object -FilePath $runLog
        Copy-Item -LiteralPath $runLog -Destination (Join-Path $p05Root 'qa\forge-run.log')
        if ($LASTEXITCODE -ne 0) { throw "P05 isolated Forge QA failed: $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $previousJava
    $env:PATH = $previousPath
}
