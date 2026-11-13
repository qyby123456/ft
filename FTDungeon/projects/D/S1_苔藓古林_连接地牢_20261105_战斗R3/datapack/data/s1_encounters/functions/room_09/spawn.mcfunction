scoreboard players set #spawn_ok s1_enc 0
execute store success score #one s1_enc run summon ftdungeonmobs:skeleton_warrior 120.5 -54 70.5 {Tags:["s1_encounter", "s1_room_9", "s1_r9_u0", "s1_elite"],PersistenceRequired:1b,CanPickUpLoot:0b,CustomName:"{\"text\": \"苔林精英·骷髅战士\", \"color\": \"gold\"}",CustomNameVisible:1b,FTDungeonDifficulty:{Tier:1,Automatic:0b,Overrides:{HealthMultiplier:2.0d,AttackMultiplier:1.25d,ArmorBonus:2.0d,MagicArmorBonus:0.0d,SpellPowerMultiplier:1.25d}}}
scoreboard players operation #spawn_ok s1_enc += #one s1_enc
execute as @e[tag=s1_r9_u0,limit=1] store result entity @s Health float 1 run attribute @s minecraft:generic.max_health get 1
execute store success score #one s1_enc run summon ftdungeonmobs:skeleton_archer 122.5 -55 72.5 {Tags:["s1_encounter", "s1_room_9", "s1_r9_u1", "s1_elite"],PersistenceRequired:1b,CanPickUpLoot:0b,CustomName:"{\"text\": \"苔林精英·骷髅弓手\", \"color\": \"gold\"}",CustomNameVisible:1b,FTDungeonDifficulty:{Tier:1,Automatic:0b,Overrides:{HealthMultiplier:2.0d,AttackMultiplier:1.25d,ArmorBonus:2.0d,MagicArmorBonus:0.0d,SpellPowerMultiplier:1.25d}}}
scoreboard players operation #spawn_ok s1_enc += #one s1_enc
execute as @e[tag=s1_r9_u1,limit=1] store result entity @s Health float 1 run attribute @s minecraft:generic.max_health get 1
execute if score #spawn_ok s1_enc matches 2 run scoreboard players set #r9 s1_enc 1
execute unless score #spawn_ok s1_enc matches 2 run kill @e[tag=s1_room_9]
execute unless score #spawn_ok s1_enc matches 2 run scoreboard players set #r9 s1_enc -1
execute unless score #spawn_ok s1_enc matches 2 run tellraw @a {"text":"S1 刷怪失败，已停止该点；请检查 FT 地牢怪物模组和日志，再运行 reset。","color":"red"}
execute if score #r9 s1_enc matches 1 run tellraw @a[gamemode=!creative,gamemode=!spectator,x=106,y=-58,z=56,dx=28,dy=16,dz=28] {"text":"双守卫精英 · 战斗开始","color":"gold"}
