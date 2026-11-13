$ErrorActionPreference = 'Stop'
$source = Split-Path -Parent $PSScriptRoot
$audit = 'D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685'
$test = & (Join-Path $PSScriptRoot 'Check-Routes.ps1')
$test = [string]($test | Select-Object -Last 1)
$dependencies = Get-Content -LiteralPath 'D:\ft工程文件\霜雪花园地牢_20261101\build\classpath\runData_minecraftClasspath.txt' | Where-Object { $_.Trim() -and $_ -notmatch 'minecraft_user_repo' }
$classpath = (@('D:\ft工程文件\锈铜宝库地牢_20261101\qa\vanilla-1.20.1-mapped.jar') + $dependencies) -join ';'
$nbt = Join-Path $source 'room.nbt'
$hash = (Get-FileHash -LiteralPath $nbt).Hash.ToLowerInvariant()
Copy-Item -LiteralPath $nbt -Destination (Join-Path $test 'room.nbt')
Copy-Item -LiteralPath (Join-Path $audit 'vanilla-block-tags.json') -Destination (Join-Path $test 'vanilla-block-tags.json')
$inputData = '[{"group":3,"candidate":' + (ConvertTo-Json (Join-Path $test 'room.nbt') -Compress) + ',"sha256":"' + $hash + '"}]'
[IO.File]::WriteAllText((Join-Path $test 'route-input.json'), $inputData)
Push-Location -LiteralPath $test
try {
    & javac -encoding UTF-8 -cp $classpath -d (Join-Path $test 'classes') (Join-Path $source 'NativeCheck.java') (Join-Path $audit 'IndependentNativeAudit.java') 2>&1 | Out-File -LiteralPath (Join-Path $test 'survival-compile.log') -Encoding utf8
    if ($LASTEXITCODE -ne 0) { throw 'Native compilation failed' }
    foreach ($main in @('NativeCheck', 'IndependentNativeAudit')) {
        $arguments = @('-Djava.awt.headless=true', '-cp', ((Join-Path $test 'classes') + ';' + $classpath), $main, $test)
        if ($main -eq 'IndependentNativeAudit') { $arguments += 'frozen' }
        & java @arguments 2>&1 | Out-File -LiteralPath (Join-Path $test ($main + '.log')) -Encoding utf8
        if ($LASTEXITCODE -ne 0) { throw "$main failed" }
    }
    $reports = Get-Content -LiteralPath (Join-Path $test 'independent-native-frozen.json') -Raw | ConvertFrom-Json
    $report = @($reports)[0]
    if ($report.sha256 -ne $hash -or $report.survival_failures.Count -ne 0 -or $report.survival_unsupported.Count -ne 0 -or !$report.vanilla_block_tags_bound) { throw 'Tagged survival check failed' }
    foreach ($name in @('route-collision.json','independent-native-frozen.json','IndependentNativeAudit.log','NativeCheck.log','survival-compile.log')) {
        Copy-Item -LiteralPath (Join-Path $test $name) -Destination (Join-Path $PSScriptRoot $name)
    }
    Copy-Item -LiteralPath (Join-Path $test 'native-check.json') -Destination (Join-Path $source 'native-check.json')
    Write-Output "PASS tagged survival; independent temporary test directory: $test"
} finally { Pop-Location }
