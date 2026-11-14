param()

$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$instance = [IO.Path]::GetFullPath((Join-Path $env:APPDATA '.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2'))
$target = [IO.Path]::GetFullPath((Join-Path $instance 'mods\qys-bosses.jar'))
$taskRoot = Join-Path $project '_work\uncleansable-manor-effects-20261011'
$stamp = [TimeZoneInfo]::ConvertTimeFromUtc([DateTime]::UtcNow, [TimeZoneInfo]::FindSystemTimeZoneById('China Standard Time')).ToString('yyyyMMdd-HHmmss')
$attempt = Join-Path $taskRoot ('deployment-' + $stamp + '-' + [Guid]::NewGuid().ToString('N').Substring(0, 8))
$python = Join-Path $env:APPDATA 'uv\python\cpython-3.12.13-windows-x86_64-none\python.exe'
$java = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe'
$shell = (Get-Command pwsh.exe -ErrorAction Stop).Source
$locks = @()
$installed = $false

function Assert-GameStopped {
    $game = Get-CimInstance Win32_Process -Filter "name='java.exe' OR name='javaw.exe'" |
        Where-Object { $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main' }
    if ($game) { throw "Minecraft is running (PID $($game.ProcessId -join ',')); no deployment permitted" }
}

function Hash([string]$Path) {
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash
}

try {
    Assert-GameStopped
    foreach ($relative in @('.ftboss-152-deploy.lock', 'mods\.qys-bosses-install.lock')) {
        $lockPath = [IO.Path]::GetFullPath((Join-Path $instance $relative))
        if (-not $lockPath.StartsWith($instance + '\', [StringComparison]::OrdinalIgnoreCase)) {
            throw "Lock escapes intended instance: $lockPath"
        }
        $locks += [IO.File]::Open($lockPath, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    }
    Assert-GameStopped
    New-Item -ItemType Directory -Path $attempt -Force | Out-Null
    $baselineHash = Hash $target
    $baseline = Join-Path $attempt ('baseline-' + $baselineHash + '.jar')
    Copy-Item -LiteralPath $target -Destination $baseline
    if ((Hash $target) -ne $baselineHash -or (Hash $baseline) -ne $baselineHash) {
        throw 'Baseline changed during locked snapshot; stop and rebase'
    }
    $sourcePaths = @(
        (Join-Path $project 'src\main\java\net\farmingtales\ftboss\effect\HealBanEffect.java'),
        (Join-Path $project 'tools\runtime_patches\UncleansableEffectsPatch.java'),
        (Join-Path $project 'tools\build_uncleansable_effects.py')
    )
    $sourceHashes = @{}
    foreach ($path in $sourcePaths) { $sourceHashes[$path] = Hash $path }
    $receipt = [ordered]@{
        status = 'BUILDING_FROM_LOCKED_CURRENT_BASELINE'
        clientTimezone = 'Asia/Shanghai'
        target = $target
        baselineBackup = $baseline
        baselineSha256 = $baselineHash
        locksHeld = @('.ftboss-152-deploy.lock', 'mods/.qys-bosses-install.lock')
        sourceHashes = $sourceHashes
        productionJarWrites = 0
        neoForgeWrites = 0
        gameTested = $false
    }
    $receiptPath = Join-Path $attempt 'deployment-receipt.json'
    $receipt | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $receiptPath -Encoding utf8
    $env:PYTHONIOENCODING = 'utf-8'
    & $python (Join-Path $project 'tools\build_uncleansable_effects.py') $baseline $attempt
    if ($LASTEXITCODE -ne 0) { throw 'Latest-baseline generation/verification failed; production unchanged' }
    $verification = Get-Content -LiteralPath (Join-Path $attempt 'verification-report.json') -Raw | ConvertFrom-Json
    if ($verification.status -ne 'PASS_OFFLINE_NOT_DEPLOYED' -or
        $verification.baselineSha256 -ne $baselineHash -or -not $verification.strictClassLinkPassed -or
        -not $verification.zipPassed -or -not $verification.idempotent) {
        throw 'Verification receipt incomplete or mismatched'
    }
    foreach ($path in $sourcePaths) {
        if ((Hash $path) -ne $sourceHashes[$path]) { throw "Source changed during generation; stop: $path" }
    }
    Assert-GameStopped
    if ((Hash $target) -ne $baselineHash) { throw 'Live baseline changed; stop and rebase' }
    $candidate = Join-Path $attempt 'qys-bosses.validation-only.jar'
    if ((Hash $candidate) -ne $verification.validationJarSha256) { throw 'Validated candidate changed' }
    $receipt.status = 'VALIDATED_READY_TO_INSTALL'
    $receipt.candidateSha256 = Hash $candidate
    $receipt.incrementSha256 = $verification.incrementSha256
    $receipt.changedEntries = @($verification.changedEntries)
    $receipt.preservedEntries = $verification.preservedEntries
    $receipt.fixtureAssertionCount = $verification.removalFixtureAssertionCount
    $receipt | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $receiptPath -Encoding utf8
    & $shell -NoProfile -File (Join-Path $project 'tools\deploy_jar.ps1') -NoBuild -ArtifactPath $candidate -TargetPath $target -ExpectedTargetHash $baselineHash 2>&1 |
        Tee-Object -FilePath (Join-Path $attempt 'installation.log')
    $deploymentExit = $LASTEXITCODE
    $installedHash = Hash $target
    $installed = $installedHash -eq $receipt.candidateSha256
    if ($deploymentExit -ne 0 -or -not $installed) {
        throw "Deployment failed or incomplete; exit=$deploymentExit, observed SHA256=$installedHash"
    }
    $receipt.productionJarWrites = 1
    $receipt.installedSha256 = $installedHash
    $receipt.status = 'INSTALLED_POSTCHECK_PENDING'
    $receipt | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $receiptPath -Encoding utf8
    $snapshot = Join-Path $attempt 'qys-bosses.installed-snapshot.jar'
    Copy-Item -LiteralPath $target -Destination $snapshot
    if ((Hash $snapshot) -ne $installedHash) { throw 'Installed snapshot hash mismatch' }
    $encoding = [Text.Encoding]::GetEncoding(936)
    foreach ($name in @('runtime-link.args', 'CheckUncleansableEffects.args', 'CheckBlessingCount.args')) {
        $arguments = [IO.File]::ReadAllText((Join-Path $attempt $name), $encoding)
        $arguments = $arguments.Replace($candidate.Replace('\', '/'), $snapshot.Replace('\', '/'))
        $checkArguments = Join-Path $attempt ('installed-' + $name)
        [IO.File]::WriteAllText($checkArguments, $arguments, $encoding)
        & $java ('@' + $checkArguments) 2>&1 | Tee-Object -FilePath (Join-Path $attempt 'installed-postcheck.log') -Append
        if ($LASTEXITCODE -ne 0) { throw "Installed strict link/fixture check failed: $name" }
    }
    & node (Join-Path $project 'tools\verification\check_jar_zip_rules.cjs') $target 2>&1 |
        Tee-Object -FilePath (Join-Path $attempt 'installed-postcheck.log') -Append
    if ($LASTEXITCODE -ne 0) { throw 'Installed ZIP local-header validation failed' }
    if ((Hash $target) -ne $installedHash) { throw 'Production changed during locked postcheck' }
    $receipt.status = 'DEPLOYED_POSTCHECK_PASS'
    $receipt.installedSnapshot = $snapshot
    $receipt.postInstallStrictClassLinkPassed = $true
    $receipt.postInstallFixtureTestsPassed = $true
    $receipt.postInstallZipPassed = $true
    $receipt.rollbackBaseline = $baseline
    $receipt.pendingGameChecks = @('milk and Iron cleanse cannot remove healing ban or erosion', 'finite effects expire naturally and healing returns', 'other debuffs remain curable')
    $receipt | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $receiptPath -Encoding utf8
    Write-Output "DEPLOYED SHA256=$installedHash"
    Write-Output "RECEIPT=$receiptPath"
} catch {
    if ($receipt -and $receiptPath) {
        $receipt.status = if ($installed) { 'INSTALLED_NEEDS_ATTENTION' } else { 'STOPPED_NOT_DEPLOYED' }
        $receipt.error = $_.Exception.Message
        $receipt.productionJarWrites = if ($installed) { 1 } else { 0 }
        $receipt | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $receiptPath -Encoding utf8
    }
    throw
} finally {
    foreach ($handle in $locks) { $handle.Dispose() }
}
