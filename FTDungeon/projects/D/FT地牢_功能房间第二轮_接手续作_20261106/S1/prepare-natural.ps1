param([string]$Run='worldqa-natural')
$ErrorActionPreference='Stop'
$destination=Join-Path $PSScriptRoot $Run
if(Test-Path -LiteralPath $destination){throw 'Choose a new natural test directory; preserve existing evidence'}
New-Item -ItemType Directory -Path "$destination/native-test/src/main/java/net/farmingtales/s1qa" -Force|Out-Null
Copy-Item "$PSScriptRoot/worldqa-walk/native-test/build.gradle","$PSScriptRoot/worldqa-walk/native-test/settings.gradle","$PSScriptRoot/worldqa-walk/native-test/gradle.properties" "$destination/native-test"
Copy-Item "$PSScriptRoot/worldqa-walk/native-test/src/main/resources" "$destination/native-test/src/main" -Recurse
Copy-Item "$PSScriptRoot/natural-travel-template.java" "$destination/native-test/src/main/java/net/farmingtales/s1qa/ForestTests.java"
New-Item -ItemType Directory -Path "$destination/server/world/datapacks" -Force|Out-Null
Copy-Item "$PSScriptRoot/access-package/s1_access_1_20_1.zip" "$destination/server/world/datapacks"
Write-Output 'Prepared fresh isolated world with actual delivered datapack. Use run-natural.ps1.'
