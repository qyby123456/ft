$ErrorActionPreference = 'Stop'
# Installs the built 1.8.79 JAR into the FarmingTales 1.5.2 instance.
# The instance folder and the installed JAR both carry non-ASCII names, so both are
# located by pattern instead of by literal: the script stays encoding-independent.
$version = '1.8.79'
$jar = Join-Path $PSScriptRoot "build/libs/farmingtales_drink_anim-$version.jar"
if (!(Test-Path -LiteralPath $jar)) { throw "Missing build output $jar" }
$hash = (Get-FileHash -LiteralPath $jar -Algorithm SHA256).Hash.ToLowerInvariant()

$versions = Join-Path $env:APPDATA '.minecraft/versions'
$instance = @(Get-ChildItem -LiteralPath $versions -Directory |
    Where-Object { $_.Name -like 'FarmingTales_Forge*1.5.2' })
if ($instance.Count -ne 1) { throw "Expected one FarmingTales 1.5.2 instance, found $($instance.Count)." }
$mods = Join-Path $instance[0].FullName 'mods'
if (!(Test-Path -LiteralPath $mods)) { throw "No mods folder in $($instance[0].FullName)" }

$installed = @(Get-ChildItem -LiteralPath $mods -File | Where-Object { $_.Name -like '*drink_anim*.jar' })
if ($installed.Count -ne 1) { throw "Expected a single installed animation JAR, found $($installed.Count)." }
$old = $installed[0]
$previous = (Get-FileHash -LiteralPath $old.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
if ($previous -eq $hash) { throw 'The requested build is already installed.' }

# Keep the user's own filename prefix and only move the version.
$target = Join-Path $mods ($old.Name -replace '1\.8\.\d+', $version)
$pending = "$target.pending"
if ((Test-Path -LiteralPath $target) -or (Test-Path -LiteralPath $pending)) { throw 'Target already exists.' }

$backupDir = Join-Path $PSScriptRoot "backups/$($old.BaseName -replace '.*?(\d+\.\d+\.\d+).*','$1')-before-rice-cake-and-plate"
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
$backup = Join-Path $backupDir $old.Name
Copy-Item -LiteralPath $old.FullName -Destination $backup
if ((Get-FileHash -LiteralPath $backup -Algorithm SHA256).Hash.ToLowerInvariant() -ne $previous) {
    throw 'Backup hash mismatch.'
}

Copy-Item -LiteralPath $jar -Destination $pending
if ((Get-FileHash -LiteralPath $pending -Algorithm SHA256).Hash.ToLowerInvariant() -ne $hash) {
    throw 'Pending copy hash mismatch.'
}
try {
    Remove-Item -LiteralPath $old.FullName
    Move-Item -LiteralPath $pending -Destination $target
    $now = @(Get-ChildItem -LiteralPath $mods -File | Where-Object { $_.Name -like '*drink_anim*.jar' })
    if ($now.Count -ne 1 -or $now[0].FullName -ne $target) { throw 'Unexpected duplicate animation JAR.' }
    if ((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash.ToLowerInvariant() -ne $hash) {
        throw 'Installed JAR hash mismatch.'
    }
} catch {
    if (Test-Path -LiteralPath $target) { Remove-Item -LiteralPath $target }
    if (!(Test-Path -LiteralPath $old.FullName)) { Copy-Item -LiteralPath $backup -Destination $old.FullName }
    throw
}

# The adapted Senluo Cookery assets must be identical in the instance's cookery build.
$cookery = @(Get-ChildItem -LiteralPath $mods -File | Where-Object { $_.Name -like '*cookery*.jar' })
Add-Type -AssemblyName System.IO.Compression.FileSystem
$check = [System.IO.Compression.ZipFile]::OpenRead($target)
$models = @($check.Entries | Where-Object { $_.FullName -like '*plates/sticky_rice_cake_plate/*.json' }).Count
$profile = $check.Entries | Where-Object { $_.FullName -eq 'assets/farmingtales_drink_anim/drink_profiles.json' }
$reader = New-Object System.IO.StreamReader($profile.Open()); $profiles = ($reader.ReadToEnd() | ConvertFrom-Json).Count; $reader.Close()
$check.Dispose()

[ordered]@{
    installed_at = (Get-Date).ToString('o')
    version = $version
    instance = $instance[0].FullName
    installed = $target
    sha256 = $hash
    backup = $backup
    previous = $old.Name
    previous_sha256 = $previous
    cookery_jar = if ($cookery.Count -eq 1) { $cookery[0].Name } else { @($cookery.Name) }
    plate_stage_models = $models
    profiles = $profiles
} | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $PSScriptRoot "installation-$version.json") -Encoding utf8

Write-Output "Installed $target"
Write-Output "SHA256 $hash"
Write-Output ("Cookery: " + ($cookery.Name -join ', '))
