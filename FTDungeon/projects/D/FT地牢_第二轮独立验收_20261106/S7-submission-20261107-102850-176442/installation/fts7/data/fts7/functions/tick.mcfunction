execute if data storage fts7:installation installed in minecraft:overworld positioned 9992.5 85 10028.5 run scoreboard players enable @a[distance=..1.5] fts7_enter
execute if data storage fts7:installation installed in minecraft:overworld positioned 10014.5 85 10028.5 run scoreboard players enable @a[distance=..1.5] fts7_return
execute if data storage fts7:installation installed in minecraft:overworld positioned 9992.5 85 10028.5 as @a[distance=..1.5,scores={fts7_enter=1..}] run function fts7:enter
execute if data storage fts7:installation installed in minecraft:overworld positioned 10014.5 85 10028.5 as @a[distance=..1.5,scores={fts7_return=1..}] run function fts7:return
scoreboard players set @a[scores={fts7_enter=1..}] fts7_enter 0
scoreboard players set @a[scores={fts7_return=1..}] fts7_return 0
