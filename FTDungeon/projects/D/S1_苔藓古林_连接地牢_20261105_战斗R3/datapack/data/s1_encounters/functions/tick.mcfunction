scoreboard players add #clock s1_enc 1
execute if score #clock s1_enc matches 20.. in minecraft:overworld run function s1_encounters:pulse
execute if score #clock s1_enc matches 20.. run scoreboard players set #clock s1_enc 0
