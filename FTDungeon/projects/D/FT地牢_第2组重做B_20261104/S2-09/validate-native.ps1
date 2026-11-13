$ErrorActionPreference = 'Stop'
Push-Location -LiteralPath $PSScriptRoot
try {
    $mapped = 'D:\ft工程文件\锈铜宝库地牢_20261101\qa\vanilla-1.20.1-local-verification.jar'
    $dependencies = Get-Content -LiteralPath 'D:\ft工程文件\霜雪花园地牢_20261101\build\classpath\runData_minecraftClasspath.txt' | Where-Object { $_ -and $_ -notmatch 'minecraft_user_repo' }
    $classpath = $mapped + ';' + ($dependencies -join ';')
    $reviewer = 'D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685'
    $java = 'C:\Program Files\Common Files\Oracle\Java\javapath\java.exe'
    $javac = 'C:\Program Files\Common Files\Oracle\Java\javapath\javac.exe'
    New-Item -ItemType Directory -Path '.\qa\native-classes' -Force | Out-Null
    & $javac -encoding UTF-8 -cp $classpath -d '.\qa\native-classes' '.\qa\VerifyDeliveries.java'
    if ($LASTEXITCODE -ne 0) { throw 'Native verifier compilation failed' }
    & $java '-Djava.awt.headless=true' -cp ".\qa\native-classes;$classpath" VerifyDeliveries $PSScriptRoot *> '.\qa\native-run.log'
    if ($LASTEXITCODE -ne 0) { throw 'Native verifier failed' }
    & $java '-Djava.awt.headless=true' -cp "$reviewer\classes;$classpath" IndependentNativeAudit (Join-Path $PSScriptRoot 'qa') frozen *> '.\qa\survival-run.log'
    if ($LASTEXITCODE -ne 0) { throw 'Static survival verifier failed' }
    & $java '-Djava.awt.headless=true' -cp "$reviewer\classes;$classpath" IndependentRouteAudit (Join-Path $PSScriptRoot 'qa') *> '.\qa\volume-route-run.log'
    if ($LASTEXITCODE -ne 0) { throw 'Player volume route verifier failed' }
} finally {
    Pop-Location
}
