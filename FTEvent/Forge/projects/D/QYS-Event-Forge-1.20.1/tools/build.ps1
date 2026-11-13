param(
    [Parameter(Mandatory = $true)][string]$JavaHome,
    [Parameter(Mandatory = $true)][string]$GradleHome,
    [string]$GradleUserHome = $env:GRADLE_USER_HOME,
    [switch]$Online
)

$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
$resources = Join-Path $project 'src\main\resources\docs'
New-Item -ItemType Directory -Path $resources -Force | Out-Null
$readme = [IO.File]::ReadAllText((Join-Path $project 'README.md')).Replace('(docs/', '(')
[IO.File]::WriteAllText((Join-Path $resources 'README.md'), $readme, [Text.UTF8Encoding]::new($false))
foreach ($name in @('许愿池奖励说明.md', '文件分布与修改指南.md', '模组功能说明.md')) {
    Copy-Item -LiteralPath (Join-Path $project "docs\$name") -Destination $resources -Force
}
$savedJava = $env:JAVA_HOME
$savedCache = $env:GRADLE_USER_HOME
try {
    $env:JAVA_HOME = $JavaHome
    if ($GradleUserHome) { $env:GRADLE_USER_HOME = $GradleUserHome }
    $arguments = @('--no-daemon', '--console=plain', '--project-dir', $project)
    if (!$Online) { $arguments += '--offline' }
    $arguments += @('build', 'reobfJar', 'verifyQysEvent')
    & (Join-Path $GradleHome 'bin\gradle.bat') @arguments
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed: $LASTEXITCODE" }
} finally {
    $env:JAVA_HOME = $savedJava
    $env:GRADLE_USER_HOME = $savedCache
}
