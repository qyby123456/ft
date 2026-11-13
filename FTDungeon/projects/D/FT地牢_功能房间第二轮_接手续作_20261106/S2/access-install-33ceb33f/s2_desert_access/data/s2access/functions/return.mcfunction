execute in minecraft:overworld run tp @s 1018.5 128 1045.5 90 0
tag @s remove s2inside
tellraw @s {"text":"已返回S2外部接待台。再次进入：/trigger s2_enter set 1","color":"gold"}
