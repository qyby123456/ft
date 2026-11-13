$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$path = Join-Path $PSScriptRoot 'build\libs\ft-biome-dungeon-generator-forge-1.20.1-0.1.0-candidate.jar'
$snapshot = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'catalog-snapshot.json') -Raw | ConvertFrom-Json
$zip = [IO.Compression.ZipFile]::OpenRead($path)
try {
    if (@($zip.Entries | Group-Object FullName | Where-Object Count -gt 1).Count -gt 0) { throw 'Duplicate JAR entries' }
    function EntryBytes([string]$name) {
        $entry = $zip.GetEntry($name)
        if ($null -eq $entry) { throw "Missing JAR entry $name" }
        $stream = $entry.Open()
        $memory = [IO.MemoryStream]::new()
        try { $stream.CopyTo($memory); return ,$memory.ToArray() } finally { $stream.Dispose(); $memory.Dispose() }
    }
    $manifest = EntryBytes 'data/biomedungeon/manifest.json'
    $manifestHash = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($manifest)).ToLowerInvariant()
    if ($manifestHash -ne $snapshot.manifestSha256) { throw 'Packaged manifest mismatch' }
    $records = @()
    foreach ($template in $snapshot.templates) {
        $name = 'data/biomedungeon/structures/' + $template.id + '.nbt'
        $hash = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData((EntryBytes $name))).ToLowerInvariant()
        if ($hash -ne $template.sha256) { throw "Packaged NBT mismatch: $name" }
        $records += @{ path = $name; sha256 = $hash }
    }
    if (@($zip.Entries | Where-Object { $_.FullName -like 'data/biomedungeon/structures/*.nbt' }).Count -ne $snapshot.templates.Count) { throw 'Extra unapproved NBT in JAR' }
    foreach ($name in @('cn/ft/biomedungeon/core/DungeonApi.class','cn/ft/biomedungeon/core/DungeonPlanner.class','cn/ft/biomedungeon/core/DungeonGeometry.class','cn/ft/biomedungeon/runtime/DungeonMod.class')) {
        $bytes = EntryBytes $name
        if (($bytes[6] * 256 + $bytes[7]) -ne 61) { throw "Not Java17 class: $name" }
    }
    if (@($zip.Entries | Where-Object { $_.FullName -match '(RuntimeGameTests|RuntimeRestartGameTests|RuntimeValidation|ftbd_restarttest)' }).Count -gt 0) { throw 'Test code or restart fixture leaked into production JAR' }
    @{ status = 'PASS'; artifact = $path; sha256 = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant(); manifestSha256 = $manifestHash; templates = $records; javaMajor = 61; duplicateEntries = 0; testCodeBundled = $false; scope = 'Archive content validation; release JAR not installed into separate server' } | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $PSScriptRoot 'jar-audit.json') -Encoding utf8
} finally { $zip.Dispose() }
