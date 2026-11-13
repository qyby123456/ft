param([string]$Fixture = ('corridor-m07-' + (Get-Date -Format 'yyyyMMdd-HHmmss')))
$ErrorActionPreference = 'Stop'
if ($Fixture -notmatch '^corridor-m07-[0-9]{8}-[0-9]{6}$') { throw 'Invalid isolated fixture name' }
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
        & (Join-Path $javaHome 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' 'runGameTestServer' "-PqaFixture=$Fixture" '--offline' '--console=plain' '-Dnet.minecraftforge.gradle.check.certs=false' 2>&1 | Tee-Object -FilePath $log
        $result = $LASTEXITCODE
        Copy-Item -LiteralPath $log -Destination (Join-Path $root 'qa\forge-run.log')
        if ($result -ne 0) { throw "corridor-M07 isolated Forge QA failed: $result" }
    } finally { Pop-Location }
} finally { $env:JAVA_HOME = $oldJavaHome; $env:PATH = $oldPath }

