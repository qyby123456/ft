scoreboard players set #ok ftm_state 0
execute in ftmaze:depths store success score #ok ftm_state run place template ftmaze:v1/system_0_tile_3 384 25 4
execute in ftmaze:depths run forceload remove 384 4 511 207
execute if score #ok ftm_state matches 1 run schedule function ftmaze:v1/system/0/step_3 2t replace
execute unless score #ok ftm_state matches 1 run tellraw @a {"text":"完整体系房接入失败；尚未开放入场。","color":"red"}
execute unless score #ok ftm_state matches 1 run scoreboard players set #run ftm_state -1
