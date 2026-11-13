scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 885 31 198 minecraft:air if block 885 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 885 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 875 31 202 minecraft:air if block 875 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 875 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 890 31 202 minecraft:air if block 890 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 890 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 878 31 198 minecraft:air if block 878 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 878 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 882 31 202 minecraft:air if block 882 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 882 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 879 31 201 minecraft:air if block 879 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 879 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 881 31 199 minecraft:air if block 881 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 881 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 886 31 201 minecraft:air if block 886 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 886 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 886 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 886 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b364 ftm_brick 2
