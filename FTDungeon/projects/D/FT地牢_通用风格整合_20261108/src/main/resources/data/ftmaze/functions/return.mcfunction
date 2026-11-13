execute unless score #run ftm_state matches 4 run tellraw @s {"text":"最终Boss尚未击败，折返门未开。","color":"red"}
execute if score #run ftm_state matches 4 if dimension ftmaze:depths if entity @s[x=137,y=30,z=283,dx=6,dy=6,dz=14] run function ftmaze:entry/return
