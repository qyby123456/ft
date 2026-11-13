$ErrorActionPreference = 'Stop'
$deliveryN05 = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if ((Split-Path -Path $deliveryN05 -Leaf) -ne 'zone-N05') { throw 'Wrong N05 workspace.' }
$cachedGradleN05 = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$javaN05 = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$stampN05 = Get-Date -Format 'yyyyMMdd-HHmmss'
$runN05 = 'run/n05-reproduction-' + $stampN05
$logN05 = Join-Path $deliveryN05 ('qa/forge-reproduction-' + $stampN05 + '.log')
$oldJavaN05 = $env:JAVA_HOME
$oldPathN05 = $env:PATH
$locationN05 = Get-Location
try {
    $env:JAVA_HOME = $javaN05
    $env:PATH = "$javaN05\bin;$env:PATH"
    Set-Location -LiteralPath (Join-Path $deliveryN05 'forge')
    & $cachedGradleN05 runGameTestServer --offline --no-daemon --console=plain '-Dnet.minecraftforge.gradle.check.certs=false' "-PrunDirectory=$runN05" *> $logN05
    $exitN05 = $LASTEXITCODE
    Get-Content -LiteralPath $logN05 -Tail 80
    if ($exitN05 -ne 0) { throw "Forge failed with exit code $exitN05. See $logN05" }
    $resultN05 = Get-Content -LiteralPath (Join-Path $deliveryN05 'qa/forge-native.json') -Raw | ConvertFrom-Json
    if ($resultN05.status -ne 'PASS') { throw 'Native report is not PASS.' }
} finally {
    Set-Location -LiteralPath $locationN05.Path
    $env:JAVA_HOME = $oldJavaN05
    $env:PATH = $oldPathN05
}
