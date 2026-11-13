scoreboard players set #ok ftm_state 0
execute in ftmaze:depths store success score #ok ftm_state run place template ftmaze:v0/system_5_tile_4 512 89 173
execute in ftmaze:depths run forceload remove 512 173 639 206
execute if score #ok ftm_state matches 1 run schedule function ftmaze:v0/system/5/step_4 2t replace
execute unless score #ok ftm_state matches 1 run tellraw @a {"text":"完整体系房接入失败；尚未开放入场。","color":"red"}
execute unless score #ok ftm_state matches 1 run scoreboard players set #run ftm_state -1
