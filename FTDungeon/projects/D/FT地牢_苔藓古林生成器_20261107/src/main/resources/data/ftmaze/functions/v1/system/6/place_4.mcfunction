scoreboard players set #ok ftm_state 0
execute in ftmaze:depths store success score #ok ftm_state run place template ftmaze:v1/system_6_tile_5 640 149 194
execute in ftmaze:depths run forceload remove 640 194 767 206
execute if score #ok ftm_state matches 1 run schedule function ftmaze:v1/system/6/step_5 2t replace
execute unless score #ok ftm_state matches 1 run tellraw @a {"text":"完整体系房接入失败；尚未开放入场。","color":"red"}
execute unless score #ok ftm_state matches 1 run scoreboard players set #run ftm_state -1
