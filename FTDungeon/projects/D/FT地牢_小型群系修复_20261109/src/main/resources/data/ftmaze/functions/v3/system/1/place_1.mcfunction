scoreboard players set #ok ftm_state 0
execute in ftmaze:depths store success score #ok ftm_state run place template ftmaze:v3/system_1_tile_2 256 29 193
execute in ftmaze:depths run forceload remove 256 193 383 218
execute if score #ok ftm_state matches 1 run schedule function ftmaze:v3/system/1/step_2 2t replace
execute unless score #ok ftm_state matches 1 run tellraw @a {"text":"完整体系房接入失败；尚未开放入场。","color":"red"}
execute unless score #ok ftm_state matches 1 run scoreboard players set #run ftm_state -1
