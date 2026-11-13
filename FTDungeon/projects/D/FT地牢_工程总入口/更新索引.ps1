$ErrorActionPreference = 'Stop'
$portalRoot = [IO.Path]::GetFullPath($PSScriptRoot)
$expectedRoot = [IO.Path]::GetFullPath('D:/ft' + [char]0x5DE5 + [char]0x7A0B + [char]0x6587 + [char]0x4EF6)
if ([IO.Directory]::GetParent($portalRoot).FullName -ne $expectedRoot) { throw 'Unexpected portal root' }
$python = 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe'
if (-not (Test-Path -LiteralPath $python -PathType Leaf)) { throw 'Configured Python runtime was not found' }
$organizer = Join-Path $portalRoot 'organize.py'
& $python -X utf8 $organizer
if ($LASTEXITCODE -ne 0) { throw 'Index generation failed' }
$manifest = Get-Content -LiteralPath (Join-Path $portalRoot 'projects.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$shortcutShell = New-Object -ComObject WScript.Shell
$checked = 0
$problems = [Collections.Generic.List[object]]::new()
foreach ($entry in $manifest.Links) {
    $shortcutPath = [IO.Path]::GetFullPath($entry.Shortcut)
    $targetPath = [IO.Path]::GetFullPath($entry.Target)
    if (-not $shortcutPath.StartsWith($portalRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'Shortcut path escaped portal' }
    if (-not $targetPath.StartsWith($expectedRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'Target path escaped engineering root' }
    if (-not (Test-Path -LiteralPath $targetPath)) { throw ('Missing target: ' + $targetPath) }
    $shortcut = $shortcutShell.CreateShortcut($shortcutPath)
    $shortcut.TargetPath = $targetPath
    $shortcut.Arguments = ''
    $shortcut.WorkingDirectory = if (Test-Path -LiteralPath $targetPath -PathType Container) { $targetPath } else { [IO.Path]::GetDirectoryName($targetPath) }
    $shortcut.Description = $entry.Description
    $shortcut.Save()
    $readback = $shortcutShell.CreateShortcut($shortcutPath)
    if (-not [string]::Equals([IO.Path]::GetFullPath($readback.TargetPath), $targetPath, [StringComparison]::OrdinalIgnoreCase) -or $readback.Arguments -ne '') {
        $problems.Add([pscustomobject]@{Shortcut=$shortcutPath;Expected=$targetPath;Actual=$readback.TargetPath})
    }
    $checked++
}
[Runtime.InteropServices.Marshal]::FinalReleaseComObject($shortcutShell) | Out-Null
$receipt = [ordered]@{Checked=$checked;Problems=@($problems.ToArray());Verified=(Get-Date -Format o);OriginalFilesMoved=0;OriginalFilesDeleted=0}
$receipt | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $portalRoot 'shortcut-check.json') -Encoding UTF8
if ($problems.Count -gt 0) { throw 'Shortcut readback failed' }
& $python -X utf8 $organizer --finish
if ($LASTEXITCODE -ne 0) { throw 'Final verification failed' }
