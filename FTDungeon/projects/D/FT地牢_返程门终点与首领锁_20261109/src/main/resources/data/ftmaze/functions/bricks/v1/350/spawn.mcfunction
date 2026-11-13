scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 727 31 198 minecraft:air if block 727 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 727 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 717 31 202 minecraft:air if block 717 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 717 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 732 31 202 minecraft:air if block 732 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 732 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 720 31 198 minecraft:air if block 720 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 720 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 724 31 202 minecraft:air if block 724 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 724 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 721 31 201 minecraft:air if block 721 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 721 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 723 31 199 minecraft:air if block 723 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 723 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 728 31 201 minecraft:air if block 728 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 728 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 728 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 728 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b350 ftm_brick 2
