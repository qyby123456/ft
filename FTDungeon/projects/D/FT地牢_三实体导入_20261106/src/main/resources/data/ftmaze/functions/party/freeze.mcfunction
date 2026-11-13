scoreboard players add #nonce ftm_state 1
tag @a remove ftmaze.roster
scoreboard players set #players ftm_state 0
execute as @a[tag=ftmaze.queued,distance=..8] run function ftmaze:party/enroll
scoreboard players operation #systems ftm_state = #players ftm_state
scoreboard players operation #systems ftm_state /= #two ftm_state
execute if score #systems ftm_state matches ..0 run scoreboard players set #systems ftm_state 1
execute if score #players ftm_state matches 1..16 run function ftmaze:build/roll
execute if score #players ftm_state matches 0 run tellraw @s {"text":"没有有效报名者，不开局。先 /trigger ftm_join。","color":"red"}
execute if score #players ftm_state matches 17.. run tellraw @s {"text":"超过本候选已烘焙的人数上限16；拒绝开局，不会静默少放体系房。","color":"red"}
