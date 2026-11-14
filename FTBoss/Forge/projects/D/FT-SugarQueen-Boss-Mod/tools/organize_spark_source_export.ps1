param([switch]$Apply)
$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$work = Join-Path $root '_work\spark-YlP6kNivxD-20261027'
$archive = Join-Path $work 'organization-archive'
$tracked = @{}
& git -C $root ls-files | ForEach-Object { $tracked[$_.Replace('/', '\')] = $true }
if ($LASTEXITCODE -ne 0) { throw 'Cannot inspect tracked paths.' }
$candidates = @()
$oldSource = Join-Path $root 'src.7z'
if (Test-Path -LiteralPath $oldSource) { $candidates += Get-Item -LiteralPath $oldSource }
$candidates += @(Get-ChildItem -LiteralPath (Join-Path $root 'tools') -Recurse -File -Filter '*.pyc' |
    Where-Object { $_.DirectoryName -match '[\\/]__pycache__(?:[\\/]|$)' })
$cutoff = [DateTimeOffset]::Parse('2026-10-27T16:35:12+08:00').UtcDateTime
$candidates += @(Get-ChildItem -LiteralPath (Join-Path $root 'logs') -File -Filter '*.log' |
    Where-Object { $_.LastWriteTimeUtc -lt $cutoff })
$plan = @()
foreach ($file in ($candidates | Sort-Object FullName -Unique)) {
    $source = [IO.Path]::GetFullPath($file.FullName)
    if (-not $source.StartsWith($root + '\', [StringComparison]::OrdinalIgnoreCase)) { throw "Source escapes project: $source" }
    $relative = $source.Substring($root.Length + 1)
    if ($tracked.ContainsKey($relative)) { continue }
    $destination = [IO.Path]::GetFullPath((Join-Path $archive $relative))
    if (-not $destination.StartsWith($archive + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Archive destination escapes workspace.' }
    $ancestor = $file.Directory
    while ($ancestor -and $ancestor.FullName -ne $root) {
        if ($ancestor.Attributes -band [IO.FileAttributes]::ReparsePoint) { throw "Refusing redirected source: $source" }
        $ancestor = $ancestor.Parent
    }
    $plan += [pscustomobject]@{ source = $source; destination = $destination; relative = $relative;
        bytes = $file.Length; sha256 = (Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash.ToLowerInvariant() }
}
[IO.Directory]::CreateDirectory($work) | Out-Null
[IO.File]::WriteAllText((Join-Path $work 'organization-plan.json'), (ConvertTo-Json -InputObject $plan -Depth 5) + "`n", [Text.UTF8Encoding]::new($false))
Write-Output "Plan: $($plan.Count) untracked historical/cache files, $(($plan | Measure-Object bytes -Sum).Sum) bytes; apply=$Apply"
if (-not $Apply) { return }
foreach ($entry in $plan) {
    if (Test-Path -LiteralPath $entry.destination) { throw "Archive collision: $($entry.destination)" }
    if ((Get-FileHash -LiteralPath $entry.source -Algorithm SHA256).Hash.ToLowerInvariant() -ne $entry.sha256) { throw "Source changed: $($entry.source)" }
    [IO.Directory]::CreateDirectory((Split-Path -Parent $entry.destination)) | Out-Null
    Move-Item -LiteralPath $entry.source -Destination $entry.destination
    if ((Get-FileHash -LiteralPath $entry.destination -Algorithm SHA256).Hash.ToLowerInvariant() -ne $entry.sha256) { throw "Archive checksum mismatch: $($entry.destination)" }
}
[IO.File]::WriteAllText((Join-Path $work 'organization-receipt.json'),
    (([ordered]@{ status = 'PASS'; movedFiles = $plan.Count; movedBytes = ($plan | Measure-Object bytes -Sum).Sum;
        archive = $archive; sourceTreeTouched = $false; trackedFilesMoved = $false; otherTaskWorkspacesTouched = $false;
        audioTouched = $false; docsTouched = $false; configsTouched = $false; savesTouched = $false }) | ConvertTo-Json -Depth 5) + "`n",
    [Text.UTF8Encoding]::new($false))
Write-Output 'PASS: archived obsolete root source bundle, Python bytecode caches and old project logs with SHA256 verification.'
