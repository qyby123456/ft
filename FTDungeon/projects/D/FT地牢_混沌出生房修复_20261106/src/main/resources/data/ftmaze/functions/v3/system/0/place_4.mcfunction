scoreboard players set #ok ftm_state 0
execute in ftmaze:depths store success score #ok ftm_state run place template ftmaze:v3/system_0_tile_4 512 205 11
execute in ftmaze:depths run forceload remove 512 11 639 173
execute if score #ok ftm_state matches 1 run schedule function ftmaze:v3/system/slot_1 2t replace
execute unless score #ok ftm_state matches 1 run tellraw @a {"text":"完整体系房接入失败；尚未开放入场。","color":"red"}
execute unless score #ok ftm_state matches 1 run scoreboard players set #run ftm_state -1
