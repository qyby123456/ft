scoreboard players set #spawn_ok s1_enc 0
execute store success score #one s1_enc run summon ftdungeonmobs:skeleton_archer 128.5 -56 119.5 {Tags:["s1_encounter", "s1_room_8", "s1_r8_u0", "s1_regular"],PersistenceRequired:1b,CanPickUpLoot:0b,CustomName:"{\"text\": \"苔林·骷髅弓手\", \"color\": \"gray\"}",CustomNameVisible:0b,FTDungeonDifficulty:{Tier:1,Automatic:0b,Overrides:{HealthMultiplier:1.0d,AttackMultiplier:1.0d,ArmorBonus:0.0d,MagicArmorBonus:0.0d,SpellPowerMultiplier:1.0d}}}
scoreboard players operation #spawn_ok s1_enc += #one s1_enc
execute as @e[tag=s1_r8_u0,limit=1] store result entity @s Health float 1 run attribute @s minecraft:generic.max_health get 1
execute store success score #one s1_enc run summon ftdungeonmobs:skeleton_warrior 125.5 -56 119.5 {Tags:["s1_encounter", "s1_room_8", "s1_r8_u1", "s1_regular"],PersistenceRequired:1b,CanPickUpLoot:0b,CustomName:"{\"text\": \"苔林·骷髅战士\", \"color\": \"gray\"}",CustomNameVisible:0b,FTDungeonDifficulty:{Tier:1,Automatic:0b,Overrides:{HealthMultiplier:1.0d,AttackMultiplier:1.0d,ArmorBonus:0.0d,MagicArmorBonus:0.0d,SpellPowerMultiplier:1.0d}}}
scoreboard players operation #spawn_ok s1_enc += #one s1_enc
execute as @e[tag=s1_r8_u1,limit=1] store result entity @s Health float 1 run attribute @s minecraft:generic.max_health get 1
execute store success score #one s1_enc run summon ftdungeonmobs:skeleton_mage 129.5 -56 115.5 {Tags:["s1_encounter", "s1_room_8", "s1_r8_u2", "s1_regular"],PersistenceRequired:1b,CanPickUpLoot:0b,CustomName:"{\"text\": \"苔林·骷髅法师\", \"color\": \"gray\"}",CustomNameVisible:0b,FTDungeonDifficulty:{Tier:1,Automatic:0b,Overrides:{HealthMultiplier:1.0d,AttackMultiplier:1.0d,ArmorBonus:0.0d,MagicArmorBonus:0.0d,SpellPowerMultiplier:1.0d}}}
scoreboard players operation #spawn_ok s1_enc += #one s1_enc
execute as @e[tag=s1_r8_u2,limit=1] store result entity @s Health float 1 run attribute @s minecraft:generic.max_health get 1
execute if score #spawn_ok s1_enc matches 3 run scoreboard players set #r8 s1_enc 1
execute unless score #spawn_ok s1_enc matches 3 run kill @e[tag=s1_room_8]
execute unless score #spawn_ok s1_enc matches 3 run scoreboard players set #r8 s1_enc -1
execute unless score #spawn_ok s1_enc matches 3 run tellraw @a {"text":"S1 刷怪失败，已停止该点；请检查 FT 地牢怪物模组和日志，再运行 reset。","color":"red"}
execute if score #r8 s1_enc matches 1 run tellraw @a[gamemode=!creative,gamemode=!spectator,x=110,y=-58,z=100,dx=34,dy=21,dz=34] {"text":"庭院猎手 · 战斗开始","color":"gold"}
