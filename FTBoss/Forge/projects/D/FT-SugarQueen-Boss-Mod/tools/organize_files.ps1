param([switch]$Apply)

$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath (Split-Path -Parent $PSScriptRoot)).Path
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
$archive = Join-Path $project "_dev_backup\organization-$stamp"
$candidates = @()
$candidates += Get-ChildItem -LiteralPath $project -File | Where-Object {
    $_.Name -match '^(build.*\.(log|txt)|compile.*\.log|verify.*\.log|merge.*\.log|_buildlog.*\.txt|build_err\.txt)$'
}
$candidates += Get-ChildItem -LiteralPath $PSScriptRoot -File | Where-Object { $_.Name -match '^_.*\.txt$|^_sounds_from_jar\.json$' }
$candidates += Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'audit') -File | Where-Object { $_.Name -match '^_.*\.txt$|^out_resources\.txt$' }
$retiredScripts = @(
    'cleanup_dead_code.cjs',
    'fix_ascend_insert.cjs', 'fix_bosshandler_lines.cjs', 'fix_bosshandler_strings.cjs',
    'fix_easter_tweaks.cjs', 'fix_remove_egg_summon.cjs', 'fix_tick_cache_and_dandan.cjs',
    'insert_ascend.cjs', 'patch_no_iframes.cjs', 'recover_bosshandler.cjs',
    'recover_bosshandler2.cjs', 'recover_bosshandler3.cjs', 'recover_bosshandler4.cjs',
    'recover_bosshandler5.cjs', 'recover_bosshandler_final.cjs'
)
foreach ($name in $retiredScripts) {
    $script = Join-Path $PSScriptRoot $name
    if (Test-Path -LiteralPath $script) { $candidates += Get-Item -LiteralPath $script }
}
$retiredItems = @('tea_leaf_blade', 'sugar_scepter', 'butcher_cleaver', 'rainbow_fertilizer', 'defect_fragment')
foreach ($itemId in $retiredItems) {
    foreach ($suffix in @('.png', '_e.png')) {
        $texture = Join-Path $project "src\main\resources\assets\ftboss\textures\item\$itemId$suffix"
        if (Test-Path -LiteralPath $texture) { $candidates += Get-Item -LiteralPath $texture }
    }
}
$retiredSourceFiles = @(
    'src/main/java/net/farmingtales/ftboss/integ/IronCast.java',
    'src/main/java/net/farmingtales/ftboss/client/render/SphereMesh.java'
)
foreach ($relative in $retiredSourceFiles) {
    $source = Join-Path $project $relative
    if (-not (Test-Path -LiteralPath $source)) { continue }
    $source = (Get-Item -LiteralPath $source).FullName
    $name = [IO.Path]::GetFileNameWithoutExtension($source)
    $references = Get-ChildItem -LiteralPath (Join-Path $project 'src/main') -Recurse -File |
        Where-Object { $_.Extension -in @('.java', '.json', '.toml', '.cfg') -and $_.FullName -ne $source } |
        Select-String -Pattern ("\b" + [regex]::Escape($name) + "\b")
    if ($references) { throw "Retired class has live references; review required: $name" }
    $candidates += Get-Item -LiteralPath $source
}
foreach ($kind in @('models', 'textures')) {
    $directory = Join-Path $project "src/main/resources/assets/ftboss/$kind/item/boss_scrolls"
    if (Test-Path -LiteralPath $directory) {
        $candidates += Get-ChildItem -LiteralPath $directory -File -Recurse
    }
}
$retiredSounds = @('apple_boss_attack', 'apple_boss_dash', 'apple_boss_death', 'apple_boss_seed')
foreach ($sound in $retiredSounds) {
    $source = Join-Path $project "src/main/resources/assets/ftboss/sounds/effects/$sound.ogg"
    if (-not (Test-Path -LiteralPath $source)) { continue }
    $references = Get-ChildItem -LiteralPath (Join-Path $project 'src/main') -Recurse -File |
        Where-Object { $_.Extension -in @('.java', '.json') } | Select-String -SimpleMatch $sound
    if ($references) { throw "Retired sound has live references; review required: $sound" }
    $candidates += Get-Item -LiteralPath $source
}

function Assert-ProjectPath([string]$Path) {
    $absolute = [IO.Path]::GetFullPath($Path)
    if (-not $absolute.StartsWith($project + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Path escapes project: $absolute"
    }
    $ancestor = $absolute
    while ($ancestor -and $ancestor -ne $project) {
        if (Test-Path -LiteralPath $ancestor) {
            if ((Get-Item -LiteralPath $ancestor -Force).Attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw "Refusing reparse point: $ancestor"
            }
        }
        $ancestor = Split-Path -Parent $ancestor
    }
    return $absolute
}

$manifest = @($candidates | Sort-Object FullName -Unique | ForEach-Object {
    $source = Assert-ProjectPath $_.FullName
    $relative = $source.Substring($project.Length + 1)
    $target = Assert-ProjectPath (Join-Path $archive $relative)
    [pscustomobject]@{ relative = $relative; source = $source; target = $target; bytes = $_.Length; sha256 = (Get-FileHash -LiteralPath $source).Hash }
})
$manifest | Select-Object relative, bytes | Format-Table -AutoSize
Write-Output ("{0} files, {1} bytes; apply={2}" -f $manifest.Count, ($manifest | Measure-Object bytes -Sum).Sum, $Apply)
if (-not $Apply -or $manifest.Count -eq 0) { return }
$null = Assert-ProjectPath $archive
New-Item -ItemType Directory -Path $archive | Out-Null
$manifestPath = Join-Path $archive 'manifest.json'
[IO.File]::WriteAllText($manifestPath, (ConvertTo-Json -InputObject $manifest -Depth 4), [Text.UTF8Encoding]::new($false))
foreach ($entry in $manifest) {
    $null = Assert-ProjectPath $entry.source
    $null = Assert-ProjectPath $entry.target
    if (Test-Path -LiteralPath $entry.target) { throw "Archive collision: $($entry.target)" }
    if ((Get-FileHash -LiteralPath $entry.source).Hash -ne $entry.sha256) { throw "Source changed: $($entry.source)" }
    New-Item -ItemType Directory -Path (Split-Path -Parent $entry.target) -Force | Out-Null
    Move-Item -LiteralPath $entry.source -Destination $entry.target
    if ((Get-FileHash -LiteralPath $entry.target).Hash -ne $entry.sha256) { throw "Archive hash mismatch: $($entry.target)" }
}
Write-Output "Archived without deletion: $manifestPath"
