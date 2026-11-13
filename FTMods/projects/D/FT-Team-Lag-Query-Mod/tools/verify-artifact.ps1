param([string]$Jar = (Join-Path $PSScriptRoot '..\build\libs\qys-team-lag-query.jar'))
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $Jar).Path)
try {
    $entry = $archive.GetEntry('META-INF/mods.toml')
    if (-not $entry) { throw 'Missing mods.toml' }
    $reader = [IO.StreamReader]::new($entry.Open())
    try { $toml = $reader.ReadToEnd() } finally { $reader.Dispose() }
    $mods = @([regex]::Matches($toml, '(?s)\[\[mods\]\](.*?)(?=\r?\n\s*\[|\z)'))
    if ($mods.Count -ne 1 -or $mods[0].Groups[1].Value -notmatch 'modId="ftteamlag"') { throw 'Artifact must provide only ftteamlag' }
    $forbidden = '^net/farmingtales/(lagcontrol|ftecon|ftboss)/|GameTests|QueryGameTests|StorageGameTests|OptionalGameTests|arena\.nbt|\.jar$|^observable/|^com/refinedmods/|^dev/|^net/minecraft/'
    if ($archive.Entries.FullName -match $forbidden) { throw 'Artifact contains legacy features, tests or bundled dependencies' }
    foreach ($required in @('pack.mcmeta', 'ftteamlag.mixins.json', 'net/farmingtales/teamlag/LagQuery.class', 'net/farmingtales/teamlag/QueryCommands.class', 'net/farmingtales/teamlag/RefinedStorageTiming.class', 'net/farmingtales/teamlag/mixin/RefinedStorageMixin.class')) {
        if (-not $archive.GetEntry($required)) { throw "Missing artifact entry: $required" }
    }
    foreach ($class in $archive.Entries | Where-Object FullName -Like '*.class') {
        $stream = $class.Open()
        try { $bytes = [byte[]]::new(8); if ($stream.Read($bytes, 0, 8) -ne 8 -or $bytes[7] -ne 61) { throw "Expected Java 17 class: $($class.FullName)" } }
        finally { $stream.Dispose() }
    }
} finally { $archive.Dispose() }
$sourceRoot = Join-Path $PSScriptRoot '..\src\main\java'
$forbiddenSource = 'cancellable\s*=\s*true|\.cancel\(|\.setReturnValue\(|\.discard\(|\.kill\(|SavedData|EntityJoinLevelEvent|BlockEvent|ProductionGuard|net\.farmingtales\.(lagcontrol|ftecon)|sdmshop'
$violations = @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -Filter '*.java' -File | Select-String -Pattern $forbiddenSource)
if ($violations.Count) { $violations | Format-Table; throw 'Found a non-read-only API in production source' }
Write-Output 'READ-ONLY ARTIFACT CHECK PASSED: one mod ID, Java 17, no punishment/economy handlers, tests or embedded dependencies'
Get-FileHash -LiteralPath $Jar -Algorithm SHA256 | Format-List
