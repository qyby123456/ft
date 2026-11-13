param(
    [string]$GameDirectory = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2'
)
$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
$game = [IO.Path]::GetFullPath($GameDirectory).TrimEnd('\')
$expectedGame = [IO.Path]::GetFullPath('C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2').TrimEnd('\')
if ($game -ne $expectedGame) { throw 'This installer only targets the explicitly configured Forge 1.20.1 instance.' }
$source = Join-Path $project 'out\ft-frost-garden-1.20.1.zip'
$verification = Get-Content -LiteralPath (Join-Path $project 'qa\minecraft-format-verification.json') -Raw | ConvertFrom-Json
if ($verification.status -ne 'PASS') { throw 'Minecraft-format verification did not pass.' }
$verifiedHash = (Get-Content -LiteralPath (Join-Path $project 'qa\verified-package-sha256.txt') -Raw).Trim()
$sourceHash = (Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash
if ($sourceHash -ne $verifiedHash) { throw 'Package changed after verification. Re-run verification before installing.' }
$loaderConfig = Join-Path $game 'config\moonlight-common.toml'
if (-not (Test-Path -LiteralPath $loaderConfig) -or (Get-Content -LiteralPath $loaderConfig -Raw) -notmatch 'global_datapacks_folder\s*=\s*"moonlight-global-datapacks"') {
    throw 'Moonlight global datapack loader is not configured as expected.'
}
$modFiles = @(Get-ChildItem -LiteralPath (Join-Path $game 'mods') -Filter '*.jar' -File | Where-Object { $_.Name -like 'FT地牢怪物*' })
if ($modFiles.Count -ne 1) { throw 'Expected exactly one installed FT Dungeon Mobs JAR.' }
$running = @(Get-CimInstance Win32_Process | Where-Object {
    $_.Name -match '^(java|javaw)\.exe$' -and ($_.CommandLine -match 'forgeclient|minecraft.client.main|forgeserver' -or $_.CommandLine -like ('*' + $game + '*'))
})
if ($running.Count -gt 0) { throw 'Exit the Minecraft client/server before installing this pack.' }
$destinationDirectory = Join-Path $game 'moonlight-global-datapacks'
$destination = Join-Path $destinationDirectory 'ft-frost-garden-1.20.1.zip'
$fullDestination = [IO.Path]::GetFullPath($destination)
if (-not $fullDestination.StartsWith($game + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Unsafe destination.' }
New-Item -ItemType Directory -Path $destinationDirectory -Force | Out-Null
if (Test-Path -LiteralPath $destination) {
    if ((Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash -ne $sourceHash) {
        throw 'A different frost garden pack is already installed. Preserve it before replacing.'
    }
} else {
    Copy-Item -LiteralPath $source -Destination $destination
}
if ((Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash -ne $sourceHash) { throw 'Installed hash mismatch.' }
$receipt = [ordered]@{
    status = 'PASS'
    installedAt = ([TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTimeOffset]::UtcNow, 'China Standard Time').ToString('o'))
    source = $source
    destination = $destination
    sha256 = $sourceHash.ToLowerInvariant()
    unchangedMod = $modFiles[0].FullName
    unchangedModSha256 = (Get-FileHash -LiteralPath $modFiles[0].FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    playerSavesTouched = $false
    automaticConstruction = $false
    uninstall = 'Remove only this ZIP after using /function ftfrostgarden:reset. Existing garden blocks are retained.'
}
$receipt | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $project 'qa\deployment.json') -Encoding utf8
$receipt | ConvertTo-Json -Depth 4
