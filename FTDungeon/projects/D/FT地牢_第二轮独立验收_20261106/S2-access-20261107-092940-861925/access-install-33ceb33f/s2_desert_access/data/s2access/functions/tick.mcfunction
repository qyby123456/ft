scoreboard players add #ticks s2_state 1
execute if score #installed s2_state matches 1 as @a[tag=!s2inside] at @s if dimension minecraft:overworld run scoreboard players enable @s s2_enter
scoreboard players enable @a[tag=s2inside] s2_return
execute if score #installed s2_state matches 1 as @a[tag=!s2inside,scores={s2_enter=1..}] at @s if dimension minecraft:overworld run function s2access:enter
execute as @a[tag=s2inside,scores={s2_return=1..}] run function s2access:return
scoreboard players set @a[scores={s2_enter=1..}] s2_enter 0
scoreboard players set @a[scores={s2_return=1..}] s2_return 0
