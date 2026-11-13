$ErrorActionPreference = 'Stop'
$delivery = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$project = [IO.Path]::GetFullPath((Join-Path $delivery 'forge'))
$java = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe'
$launcher = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\lib\gradle-launcher-8.8.jar'
if (-not $project.StartsWith($delivery + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'QA project escaped delivery directory' }
Push-Location -LiteralPath $project
try {
    & $java -XX:ActiveProcessorCount=2 -classpath $launcher org.gradle.launcher.GradleMain runGameTestServer --offline --no-daemon --console=plain '-Dnet.minecraftforge.gradle.check.certs=false' 2>&1 | Tee-Object -FilePath (Join-Path $delivery 'qa\forge-run.log')
    if ($LASTEXITCODE -ne 0) { throw "C03 isolated Forge QA failed: $LASTEXITCODE" }
}
finally { Pop-Location }

