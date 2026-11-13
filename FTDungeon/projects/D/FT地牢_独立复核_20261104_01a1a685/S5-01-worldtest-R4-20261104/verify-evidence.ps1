$ErrorActionPreference='Stop'
$expected='9450884a348eda61ea55f0f11a8dae87a0f7d5183312b1528d9a519449f6b9a2'
if((Get-FileHash -LiteralPath "$PSScriptRoot\frozen.nbt" -Algorithm SHA256).Hash -ne $expected){throw 'Frozen NBT mismatch'}
[xml]$xml=Get-Content -LiteralPath "$PSScriptRoot\tests.xml" -Raw
if($xml.SelectNodes('//testcase').Count -ne 4 -or $xml.SelectNodes('//failure').Count){throw 'Four GameTests did not pass'}
if((Get-Content -LiteralPath "$PSScriptRoot\exit.json" -Raw | ConvertFrom-Json).exit_code -ne 0){throw 'Gradle nonzero exit'}
$rows=foreach($rotation in @('NONE','CLOCKWISE_90','CLOCKWISE_180','COUNTERCLOCKWISE_90')){
  $report=Get-Content -LiteralPath "$PSScriptRoot\$rotation\report.json" -Raw | ConvertFrom-Json
  if($report.nbt_sha256 -ne $expected -or $report.server_ticks -lt 1200 -or $report.random_tick_speed -ne 3 -or $report.knownShape -or $report.placement_flags -ne 3){throw "Test parameters $rotation"}
  if($report.random_tick_probes.lower_section_grown -le 0 -or $report.random_tick_probes.upper_section_grown -le 0){throw "No natural random tick coverage $rotation"}
  if($report.explicit_neighbor_update_diff.Count -or $report.before_vs_1200_diff.Count -or $report.after_update_vs_1200_diff.Count){throw "Unexpected tank drift $rotation"}
  if($report.settled.dead_coral -or $report.settled.coral_identity_changes -or $report.settled.invalid_aquatic_survival -or $report.settled.original_water_no_longer_source_water -or $report.settled.dropped_items.Count){throw "Aquatic loss $rotation"}
  if($report.placement_expected_vs_actual.Count -ne 14){throw "Unexpected initial drift count $rotation"}
  foreach($diff in $report.placement_expected_vs_actual){
    if($diff.before -notmatch 'minecraft:prismarine_brick_stairs' -or $diff.after -ne $diff.before.Replace('waterlogged=false','waterlogged=true')){throw "Unexpected placement change $rotation"}
  }
  [pscustomobject]@{rotation=$rotation;ticks=$report.server_ticks;random_tick_speed=3;random_probe_lower=$report.random_tick_probes.lower_section_grown;random_probe_upper=$report.random_tick_probes.upper_section_grown;placement_waterlogged_stairs=14;neighbor_update_diff=0;tank_tick_diff=0;dead_coral=0;dropped_items=0;original_source_water_lost=0}
}
[pscustomobject]@{status='WORLD_EVIDENCE_CHECKED_AWAITING_INDEPENDENT_REVIEW';nbt_sha256=$expected;four_cases=$rows;S5_02='LOCKED';scope='Artificial sealed tank, cached Forge47.4.10 isolated world, 1200 ticks per orientation; not formal pack deployment or long-term ocean guarantee'} | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath "$PSScriptRoot\summary.json" -Encoding utf8
$rows | Format-Table
