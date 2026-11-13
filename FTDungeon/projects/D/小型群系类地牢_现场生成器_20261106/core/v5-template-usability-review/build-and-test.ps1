$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$java = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe'
$classpath = '..\build\ft-biomedungeon-core-v1.jar;..\envelope-usability-review\build\ft-biomedungeon-envelope-planner-v3-experimental.jar'
& javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp $classpath -d build/test src/V5TemplateUsabilityTests.java
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed' }
& $java -ea -cp "$classpath;build/test" V5TemplateUsabilityTests | Tee-Object logs/java17.log
if ($LASTEXITCODE -ne 0) { throw 'Java tests failed' }
& $python -B verify.py | Tee-Object logs/python-verification.log
if ($LASTEXITCODE -ne 0) { throw 'Independent verification failed' }
& $python -B finalize.py
if ($LASTEXITCODE -ne 0) { throw 'Input drift or seal failure' }
