$ErrorActionPreference = 'Stop'
# Installs the built 1.8.80 JAR into the FarmingTales 1.5.2 instance.
# The instance folder and the installed JAR both carry non-ASCII names, so both are
# located by pattern instead of by literal: the script stays encoding-independent.
$version = '1.8.80'
$jar = Join-Path $PSScriptRoot "build/libs/farmingtales_drink_anim-$version.jar"
if (!(Test-Path -LiteralPath $jar)) { throw "Missing build output $jar" }
$hash = (Get-FileHash -LiteralPath $jar -Algorithm SHA256).Hash.ToLowerInvariant()

# The release invariants are checked on the exact JAR that is about to be installed.
Add-Type -AssemblyName System.IO.Compression.FileSystem
$release = [System.IO.Compression.ZipFile]::OpenRead($jar)
$meta = New-Object System.IO.StreamReader(($release.GetEntry('META-INF/mods.toml')).Open())
$declared = (($meta.ReadToEnd() -split "`n") | Where-Object { $_ -match '^version=' }) -join ' '
$meta.Close()
if ($declared -notmatch [regex]::Escape($version)) { throw "mods.toml does not declare $version ($declared)" }
$overrides = @($release.Entries | Where-Object { $_.FullName -match '^assets/[^/]+/models/item/.+\.json$' } |
    ForEach-Object { $_.FullName })
# The three long-standing display overrides; the rice cake, the rice cake plate and the
# dark cuisine keep the flat inventory sprite their own mod ships.
$allowed = @('assets/farmingtales_drink_anim/models/item/bamboo_steamed_egg.json',
    'assets/kaleidoscope_chinesefood/models/item/bamboo_steamed_egg.json',
    'assets/papercraft_magic_decoration/models/item/bottle_of_land_no1.json')
$unexpected = @($overrides | Where-Object { $allowed -notcontains $_ })
$missing = @($allowed | Where-Object { $overrides -notcontains $_ })
if ($unexpected.Count -ne 0) { throw "Unexpected inventory icon overrides: $($unexpected -join ', ')" }
if ($missing.Count -ne 0) { throw "Missing known display overrides: $($missing -join ', ')" }
$plate = @($release.Entries | Where-Object { $_.FullName -match 'animation/plates/sticky_rice_cake_plate/level\d\.json' }).Count
$dark = @($release.Entries | Where-Object { $_.FullName -match 'animation/plates/dark_cuisine/level\d\.json' }).Count
$reader = New-Object System.IO.StreamReader(($release.GetEntry('assets/farmingtales_drink_anim/drink_profiles.json')).Open())
$profiles = ($reader.ReadToEnd() | ConvertFrom-Json).Count
$reader.Close()
$release.Dispose()
if ($plate -ne 6) { throw "Expected 6 plate stage models, found $plate" }
if ($dark -ne 4) { throw "Expected 4 dark cuisine stage models, found $dark" }
if ($profiles -ne 311) { throw "Expected 311 profiles, found $profiles" }

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

$backupDir = Join-Path $PSScriptRoot "backups/$($old.BaseName -replace '.*?(\d+\.\d+\.\d+).*','$1')-before-dark-cuisine-and-icon-rule"
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
    plate_stage_models = $plate
    dark_cuisine_stage_models = $dark
    profiles = $profiles
    inventory_icon_overrides = $overrides.Count
    declared_version_in_jar = $declared.Trim()
} | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $PSScriptRoot "installation-$version.json") -Encoding utf8

Write-Output "Installed $target"
Write-Output "SHA256 $hash"
Write-Output ("Profiles: $profiles  plate models: $plate  dark cuisine models: $dark")
Write-Output ("Inventory icon overrides kept: " + ($overrides -join ', '))
