param([string]$Root = (Split-Path -Parent $PSScriptRoot))
$ErrorActionPreference = 'Stop'
$resolvedRoot = (Resolve-Path -LiteralPath $Root).Path
$javaRoot = Join-Path $resolvedRoot 'src/main/java'
$dataRoot = Join-Path $resolvedRoot 'src/main/resources/data'
$coreNames = @('Engine.java','Planner.java','Catalog.java','Gameplay.java','Store.java')
$styleCores = @(Get-ChildItem -LiteralPath $javaRoot -Recurse -File | Where-Object {
    $_.Name -in $coreNames -and $_.FullName -match '[\\/](desert|frost|obsidian)[\\/]'
})
$clonedNbt = @(Get-ChildItem -LiteralPath $dataRoot -Recurse -File -Filter '*.nbt' | Where-Object {
    $_.FullName -match '[\\/](ftdesertgen|ftfrostgen|obsidian|desert|frost)[\\/]'
})
foreach ($name in $coreNames) {
    if (-not (Test-Path -LiteralPath (Join-Path $javaRoot ('moss/generator/' + $name)))) { throw "Missing shared core: $name" }
}
if ($styleCores.Count -gt 0 -or $clonedNbt.Count -gt 0) { throw 'Style-specific Java engine or cloned NBT tree detected' }
$profileFiles = @(Get-ChildItem -LiteralPath (Join-Path $dataRoot 'ftmossgen/styles') -File -Filter '*.json')
foreach ($id in @('forest','desert','frost')) {
    if (-not ($profileFiles.Name -contains ($id + '.json'))) { throw "Missing builtin profile JSON: $id" }
}
$result = [ordered]@{
    scope='Static source/resource audit only, not a Forge test'
    root=$resolvedRoot
    singleSharedCore='moss/generator'
    newStyleJavaEngineCopies=$styleCores.Count
    newStyleNbtCopies=$clonedNbt.Count
    profileFiles=@($profileFiles | ForEach-Object { [ordered]@{path=$_.FullName;sha256=(Get-FileHash -LiteralPath $_.FullName).Hash.ToLowerInvariant()} })
    status='PASS_STATIC_AUDIT'
}
$result | ConvertTo-Json -Depth 8
