Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$orig = "C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales Forge\_work\tns\orig.jar"
$out  = "C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales Forge\_work\tns\patched.jar"
$patchDir = "C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales Forge\_work\tns\patch\out"

$replacements = @{
    "me/fzzyhmstrs/tridents_n_stuff/BleedingEffect.class"   = Join-Path $patchDir "me\fzzyhmstrs\tridents_n_stuff\BleedingEffect.class"
    "me/fzzyhmstrs/tridents_n_stuff/TridentModifier.class"  = Join-Path $patchDir "me\fzzyhmstrs\tridents_n_stuff\TridentModifier.class"
    "me/fzzyhmstrs/tridents_n_stuff/TnsEvents.class"        = Join-Path $patchDir "me\fzzyhmstrs\tridents_n_stuff\TnsEvents.class"
}

foreach ($k in $replacements.Keys) {
    if (-not (Test-Path $replacements[$k])) { throw "missing patched class: $($replacements[$k])" }
}

Remove-Item $out -Force -ErrorAction SilentlyContinue

$src = [System.IO.Compression.ZipFile]::OpenRead($orig)
$dst = [System.IO.Compression.ZipFile]::Open($out, [System.IO.Compression.ZipArchiveMode]::Create)

$replaced = 0
foreach ($entry in $src.Entries) {
    $new = $dst.CreateEntry($entry.FullName, [System.IO.Compression.CompressionLevel]::Optimal)
    $new.LastWriteTime = $entry.LastWriteTime
    $target = $new.Open()
    if ($replacements.ContainsKey($entry.FullName)) {
        $bytes = [System.IO.File]::ReadAllBytes($replacements[$entry.FullName])
        $target.Write($bytes, 0, $bytes.Length)
        $replaced++
        Write-Host ("replaced  {0,-70} {1,7} -> {2}" -f $entry.FullName, $entry.Length, $bytes.Length)
    } else {
        $in = $entry.Open()
        $in.CopyTo($target)
        $in.Dispose()
    }
    $target.Dispose()
}
$src.Dispose()
$dst.Dispose()

Write-Host "entries replaced: $replaced"
Write-Host ("out size: {0}" -f (Get-Item $out).Length)
Write-Host ("sha256  : {0}" -f (Get-FileHash $out -Algorithm SHA256).Hash)
