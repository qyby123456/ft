scoreboard players set #ok ftm_state 0
execute in ftmaze:depths store success score #ok ftm_state run place template ftmaze:v2/system_7_tile_14 1792 25 4
execute in ftmaze:depths run forceload remove 1792 4 1919 201
execute if score #ok ftm_state matches 1 run schedule function ftmaze:v2/system/slot_8 2t replace
execute unless score #ok ftm_state matches 1 run tellraw @a {"text":"完整体系房接入失败；尚未开放入场。","color":"red"}
execute unless score #ok ftm_state matches 1 run scoreboard players set #run ftm_state -1
