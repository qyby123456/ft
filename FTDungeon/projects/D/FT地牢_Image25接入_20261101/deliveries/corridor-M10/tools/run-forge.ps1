$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$oldHome = $env:JAVA_HOME
$oldPath = $env:PATH
try {
    $env:JAVA_HOME = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
    $env:PATH = (Join-Path $env:JAVA_HOME 'bin') + ';' + $oldPath
    Push-Location (Join-Path $root 'forge-qa')
    try {
        $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
        & $gradle '--offline' '--no-daemon' '--console=plain' '-Dnet.minecraftforge.gradle.check.certs=false' "-PqaRunDir=runs/$stamp" 'runGameTestServer' 2>&1 | Tee-Object -FilePath (Join-Path $root 'qa\forge.log')
        if ($LASTEXITCODE -ne 0) { throw "Isolated Forge GameTest failed: $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $oldHome
    $env:PATH = $oldPath
}
