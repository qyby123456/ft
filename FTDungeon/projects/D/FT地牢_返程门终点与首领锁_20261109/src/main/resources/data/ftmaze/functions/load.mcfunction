scoreboard objectives add ftm_state dummy
scoreboard objectives add ftm_mode dummy
scoreboard objectives add ftm_enter trigger
scoreboard objectives add ftm_leave trigger
execute unless score #run ftm_state matches 0.. run scoreboard players set #run ftm_state 0
bossbar add ftmaze:final {"text":"迷城典狱长"}
bossbar set ftmaze:final max 400
bossbar set ftmaze:final visible false
scoreboard objectives add ftm_join trigger
scoreboard objectives add ftm_party dummy
execute unless score #nonce ftm_state matches 0.. run scoreboard players set #nonce ftm_state 0
scoreboard players set #two ftm_state 2
scoreboard objectives add ftm_brick dummy
