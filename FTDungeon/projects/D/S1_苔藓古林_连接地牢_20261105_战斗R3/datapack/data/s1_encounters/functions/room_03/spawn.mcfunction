scoreboard players set #spawn_ok s1_enc 0
execute store success score #one s1_enc run summon ftdungeonmobs:skeleton_warrior 37.5 -56 108.5 {Tags:["s1_encounter", "s1_room_3", "s1_r3_u0", "s1_regular"],PersistenceRequired:1b,CanPickUpLoot:0b,CustomName:"{\"text\": \"苔林·骷髅战士\", \"color\": \"gray\"}",CustomNameVisible:0b,FTDungeonDifficulty:{Tier:1,Automatic:0b,Overrides:{HealthMultiplier:1.0d,AttackMultiplier:1.0d,ArmorBonus:0.0d,MagicArmorBonus:0.0d,SpellPowerMultiplier:1.0d}}}
scoreboard players operation #spawn_ok s1_enc += #one s1_enc
execute as @e[tag=s1_r3_u0,limit=1] store result entity @s Health float 1 run attribute @s minecraft:generic.max_health get 1
execute store success score #one s1_enc run summon ftdungeonmobs:skeleton_mage 41.5 -56 110.5 {Tags:["s1_encounter", "s1_room_3", "s1_r3_u1", "s1_regular"],PersistenceRequired:1b,CanPickUpLoot:0b,CustomName:"{\"text\": \"苔林·骷髅法师\", \"color\": \"gray\"}",CustomNameVisible:0b,FTDungeonDifficulty:{Tier:1,Automatic:0b,Overrides:{HealthMultiplier:1.0d,AttackMultiplier:1.0d,ArmorBonus:0.0d,MagicArmorBonus:0.0d,SpellPowerMultiplier:1.0d}}}
scoreboard players operation #spawn_ok s1_enc += #one s1_enc
execute as @e[tag=s1_r3_u1,limit=1] store result entity @s Health float 1 run attribute @s minecraft:generic.max_health get 1
execute if score #spawn_ok s1_enc matches 2 run scoreboard players set #r3 s1_enc 1
execute unless score #spawn_ok s1_enc matches 2 run kill @e[tag=s1_room_3]
execute unless score #spawn_ok s1_enc matches 2 run scoreboard players set #r3 s1_enc -1
execute unless score #spawn_ok s1_enc matches 2 run tellraw @a {"text":"S1 刷怪失败，已停止该点；请检查 FT 地牢怪物模组和日志，再运行 reset。","color":"red"}
execute if score #r3 s1_enc matches 1 run tellraw @a[gamemode=!creative,gamemode=!spectator,x=30,y=-58,z=99,dx=18,dy=11,dz=16] {"text":"水景守卫 · 战斗开始","color":"gold"}
