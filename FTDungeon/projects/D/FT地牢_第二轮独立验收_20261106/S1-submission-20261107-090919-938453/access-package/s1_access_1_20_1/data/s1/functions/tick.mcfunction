execute if score #installed s1_sys matches 1 in minecraft:overworld as @a[x=9992,y=62,z=10123,dx=6,dy=11,dz=9] run scoreboard players enable @s s1gate
execute if score #installed s1_sys matches 1 in minecraft:overworld as @a[x=10000,y=64,z=10000,dx=491,dy=39,dz=137] run scoreboard players enable @s s1gate
execute if score #installed s1_sys matches 1 as @a[scores={s1gate=1}] at @s if dimension minecraft:overworld if entity @s[x=9992,y=62,z=10123,dx=6,dy=11,dz=9] run function s1:enter
execute if score #installed s1_sys matches 1 as @a[scores={s1gate=2}] at @s if dimension minecraft:overworld if entity @s[x=10000,y=64,z=10000,dx=491,dy=39,dz=137] run function s1:return
scoreboard players set @a[scores={s1gate=1..}] s1gate 0
