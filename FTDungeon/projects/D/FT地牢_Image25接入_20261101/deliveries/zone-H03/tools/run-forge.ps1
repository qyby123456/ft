$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
if ((Split-Path $root -Leaf) -ne 'zone-H03') { throw 'Unexpected QA workspace' }
$javaHome = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
$oldJavaHome = $env:JAVA_HOME
$oldPath = $env:PATH
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $oldPath
    Push-Location (Join-Path $root 'native-test')
    try {
        & (Join-Path $javaHome 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' 'runGameTestServer' '--offline' '--console=plain' '-Dnet.minecraftforge.gradle.check.certs=false' 2>&1 | Tee-Object -FilePath (Join-Path $root 'qa\forge-run.log')
        if ($LASTEXITCODE -ne 0) { throw "H03 isolated Forge QA failed: $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $oldJavaHome
    $env:PATH = $oldPath
}
