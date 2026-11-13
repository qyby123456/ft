param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$GradleHome,
    [switch]$Online,
    [switch]$Clean
)

$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath($PSScriptRoot)
$exportRoot = Split-Path -Parent $project
$work = Join-Path $exportRoot '_work'
if (!$JavaHome) { throw 'Provide -JavaHome pointing to a Java 17 JDK.' }
$JavaHome = [IO.Path]::GetFullPath($JavaHome)
if (!(Test-Path -LiteralPath (Join-Path $JavaHome 'bin\javac.exe'))) {
    throw 'JavaHome must contain a JDK compiler.'
}
if (!$GradleHome) { $GradleHome = Join-Path $work 'gradle-8.8' }
$launcher = Join-Path ([IO.Path]::GetFullPath($GradleHome)) 'bin\gradle.bat'
if (!(Test-Path -LiteralPath $launcher)) { throw 'Provide -GradleHome pointing to Gradle 8.8.' }
$temporary = Join-Path $work 'temp'
$userHome = Join-Path $work 'home'
$cache = Join-Path $work 'gradle-home'
foreach ($directory in @($temporary, $userHome, $cache)) {
    New-Item -ItemType Directory -Path $directory -Force | Out-Null
}
$saved = @{}
foreach ($name in @('JAVA_HOME', 'GRADLE_USER_HOME', 'JAVA_OPTS', 'TEMP', 'TMP')) {
    $saved[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
}
try {
    $env:JAVA_HOME = $JavaHome
    $env:GRADLE_USER_HOME = $cache
    $env:TEMP = $temporary
    $env:TMP = $temporary
    $env:JAVA_OPTS = '-Djava.io.tmpdir="' + $temporary + '" -Duser.home="' + $userHome + '"'
    $arguments = @('--no-daemon', '--console=plain', '--project-dir', $project,
            '--gradle-user-home', $cache,
            '-Dorg.gradle.java.installations.auto-detect=false',
            '-Dorg.gradle.java.installations.auto-download=false',
            ('-Dorg.gradle.java.installations.paths=' + $JavaHome),
            ('-Dorg.gradle.jvmargs=-Xmx2G -Dfile.encoding=UTF-8 -Djava.io.tmpdir="' + $temporary + '" -Duser.home="' + $userHome + '"'))
    if (!$Online) { $arguments += '--offline' }
    if ($Clean) { $arguments += 'clean' }
    $arguments += @('build', 'reobfJar', 'verifyQysEvent')
    & $launcher @arguments
    if ($LASTEXITCODE -ne 0) { throw ('Gradle failed: ' + $LASTEXITCODE) }
} finally {
    foreach ($name in $saved.Keys) {
        [Environment]::SetEnvironmentVariable($name, $saved[$name], 'Process')
    }
}
