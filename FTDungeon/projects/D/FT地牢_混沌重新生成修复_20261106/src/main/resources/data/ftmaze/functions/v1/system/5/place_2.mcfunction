scoreboard players set #ok ftm_state 0
execute in ftmaze:depths store success score #ok ftm_state run place template ftmaze:v1/system_5_tile_3 384 89 194
execute in ftmaze:depths run forceload remove 384 194 511 206
execute if score #ok ftm_state matches 1 run schedule function ftmaze:v1/system/5/step_3 2t replace
execute unless score #ok ftm_state matches 1 run tellraw @a {"text":"完整体系房接入失败；尚未开放入场。","color":"red"}
execute unless score #ok ftm_state matches 1 run scoreboard players set #run ftm_state -1
