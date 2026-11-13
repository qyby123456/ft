execute if score #installed s2_state matches 0 in minecraft:overworld run function s2access:install_world
execute if score #installed s2_state matches 1 run tellraw @s {"text":"S2已安装；重复安装被阻止。坐标1024 128 1024，NBT 33ceb33f。","color":"gold"}
