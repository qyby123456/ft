scoreboard players set #reset_ready s1_enc 0
execute in minecraft:overworld if loaded 75 -56 27 if loaded 78 -56 25 if loaded 37 -56 108 if loaded 41 -56 110 if loaded 76 -56 106 if loaded 73 -56 106 if loaded 75 -56 109 if loaded 75 -56 62 if loaded 75 -56 65 if loaded 75 -56 59 if loaded 13 -56 136 if loaded 17 -56 131 if loaded 128 -56 119 if loaded 125 -56 119 if loaded 129 -56 115 if loaded 120 -54 70 if loaded 122 -55 72 if loaded 43 -56 19 run scoreboard players set #reset_ready s1_enc 1
execute in minecraft:overworld if score #reset_ready s1_enc matches 1 run function s1_encounters:reset_loaded
execute unless score #reset_ready s1_enc matches 1 run tellraw @a {"text":"重置需加载整个地牢。先在入口使用 forceload add 0 0 144 172，等待加载后再 reset；结束后只移除本次新增的强加载。","color":"yellow"}
