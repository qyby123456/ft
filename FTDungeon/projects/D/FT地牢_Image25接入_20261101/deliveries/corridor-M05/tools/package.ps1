$ErrorActionPreference = 'Stop'
$root = (Resolve-Path -LiteralPath (Split-Path $PSScriptRoot -Parent)).Path
$manifestPath = Join-Path $root 'delivery-manifest.json'
$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$archivePath = Join-Path $root 'corridor-M05-delivery.zip'
if (Test-Path -LiteralPath $archivePath) { throw "Archive already exists: $archivePath" }
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::Open($archivePath, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    $paths = @($manifest.files | ForEach-Object { $_.path }) + @('delivery-manifest.json')
    foreach ($relative in $paths) {
        $absolute = [System.IO.Path]::GetFullPath((Join-Path $root $relative))
        if (-not $absolute.StartsWith($root + [System.IO.Path]::DirectorySeparatorChar, [System.StringComparison]::OrdinalIgnoreCase)) { throw 'Archive entry escapes M05 root' }
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, $absolute, 'corridor-M05/' + $relative.Replace('\', '/'), [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null
    }
} finally {
    $archive.Dispose()
}
$checksum = (Get-FileHash -LiteralPath $archivePath -Algorithm SHA256).Hash.ToLowerInvariant()
[System.IO.File]::WriteAllText((Join-Path $root 'corridor-M05-delivery.zip.sha256'), "$checksum  corridor-M05-delivery.zip`n", [System.Text.UTF8Encoding]::new($false))
Write-Output "M05_PORTABLE_BUNDLE_PASS $archivePath SHA256=$checksum"
