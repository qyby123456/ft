param(
    [string]$Instance = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2',
    [string]$Jdk = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
)

$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$cacheRoot = Join-Path $env:USERPROFILE '.gradle\caches\modules-2\files-2.1'
$classes = Join-Path $projectRoot 'build\classes'
$artifactDirectory = Join-Path $projectRoot 'build\libs'
$null = New-Item -ItemType Directory -Force -Path $classes, $artifactDirectory
$tooltipJars = @(Get-ChildItem -LiteralPath (Join-Path $Instance 'mods') -Filter '*obscure_tooltips-forge-1.20.1-3.10.1.jar')
if ($tooltipJars.Count -ne 1) {
    throw 'Expected exactly one Obscure Tooltips 3.10.1 JAR in the instance.'
}
$forgeLanguage = Get-ChildItem -LiteralPath (Join-Path $cacheRoot 'net.minecraftforge\javafmllanguage\1.20.1-47.4.10') -Recurse -Filter 'javafmllanguage-1.20.1-47.4.10.jar' | Select-Object -First 1
$mixin = Get-ChildItem -LiteralPath (Join-Path $cacheRoot 'org.spongepowered\mixin\0.8.5') -Recurse -Filter 'mixin-0.8.5.jar' | Select-Object -First 1
if (!$forgeLanguage -or !$mixin) {
    throw 'Forge 47.4.10 language library and Mixin 0.8.5 must be present in the local Gradle cache.'
}
$classpath = @($tooltipJars[0].FullName, $forgeLanguage.FullName, $mixin.FullName) -join [IO.Path]::PathSeparator
$sources = @(Get-ChildItem -LiteralPath (Join-Path $projectRoot 'src\main\java') -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
& (Join-Path $Jdk 'bin\javac.exe') --release 17 -encoding UTF-8 -proc:none -classpath $classpath -d $classes @sources
if ($LASTEXITCODE -ne 0) {
    throw 'Compilation failed.'
}
$manifestPath = Join-Path $projectRoot 'build\MANIFEST.MF'
$manifest = "Manifest-Version: 1.0`nMixinConfigs: ft_tooltip_autoscroll.mixins.json`nImplementation-Version: 1.0.0`n`n"
[IO.File]::WriteAllText($manifestPath, $manifest, [Text.UTF8Encoding]::new($false))
$artifact = Join-Path $artifactDirectory 'ft_tooltip_autoscroll-1.0.0.jar'
& (Join-Path $Jdk 'bin\jar.exe') --create --file $artifact --manifest $manifestPath -C $classes . -C (Join-Path $projectRoot 'src\main\resources') .
if ($LASTEXITCODE -ne 0) {
    throw 'Packaging failed.'
}
[PSCustomObject]@{
    Path = $artifact
    Bytes = (Get-Item -LiteralPath $artifact).Length
    SHA256 = (Get-FileHash -LiteralPath $artifact -Algorithm SHA256).Hash
} | Format-List
