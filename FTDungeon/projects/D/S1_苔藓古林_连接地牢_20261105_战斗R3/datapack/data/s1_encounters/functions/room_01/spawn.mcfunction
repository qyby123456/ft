scoreboard players set #spawn_ok s1_enc 0
execute store success score #one s1_enc run summon ftdungeonmobs:skeleton_king 43.5 -56 19.5 {Tags:["s1_encounter", "s1_room_1", "s1_r1_u0", "s1_boss"],PersistenceRequired:1b,CanPickUpLoot:0b,CustomName:"{\"text\": \"盘根骷髅王\", \"color\": \"dark_red\"}",CustomNameVisible:1b,FTDungeonDifficulty:{Tier:1,Automatic:0b,Overrides:{HealthMultiplier:1.5d,AttackMultiplier:1.3d,ArmorBonus:3.0d,MagicArmorBonus:0.0d,SpellPowerMultiplier:1.3d}}}
scoreboard players operation #spawn_ok s1_enc += #one s1_enc
execute as @e[tag=s1_r1_u0,limit=1] store result entity @s Health float 1 run attribute @s minecraft:generic.max_health get 1
execute if score #spawn_ok s1_enc matches 1 run scoreboard players set #r1 s1_enc 1
execute unless score #spawn_ok s1_enc matches 1 run kill @e[tag=s1_room_1]
execute unless score #spawn_ok s1_enc matches 1 run scoreboard players set #r1 s1_enc -1
execute unless score #spawn_ok s1_enc matches 1 run tellraw @a {"text":"S1 刷怪失败，已停止该点；请检查 FT 地牢怪物模组和日志，再运行 reset。","color":"red"}
execute if score #r1 s1_enc matches 1 run tellraw @a[gamemode=!creative,gamemode=!spectator,x=30,y=-58,z=2,dx=24,dy=14,dz=24] {"text":"盘根王座 · 战斗开始","color":"gold"}
