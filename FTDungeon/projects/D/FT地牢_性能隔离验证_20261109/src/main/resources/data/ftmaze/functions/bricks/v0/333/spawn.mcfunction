scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 750 31 198 minecraft:air if block 750 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 750 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 740 31 202 minecraft:air if block 740 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 740 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 755 31 202 minecraft:air if block 755 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 755 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 743 31 198 minecraft:air if block 743 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 743 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 747 31 202 minecraft:air if block 747 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 747 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 744 31 201 minecraft:air if block 744 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 744 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 746 31 199 minecraft:air if block 746 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 746 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 751 31 201 minecraft:air if block 751 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 751 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 751 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 751 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b333 ftm_brick 2
