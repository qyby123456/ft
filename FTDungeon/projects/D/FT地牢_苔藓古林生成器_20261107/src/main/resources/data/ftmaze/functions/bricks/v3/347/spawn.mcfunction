scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 727 211 198 minecraft:air if block 727 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 727 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 717 211 202 minecraft:air if block 717 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 717 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 732 211 202 minecraft:air if block 732 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 732 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 720 211 198 minecraft:air if block 720 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 720 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 724 211 202 minecraft:air if block 724 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 724 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 721 211 201 minecraft:air if block 721 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 721 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 723 211 199 minecraft:air if block 723 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 723 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 728 211 201 minecraft:air if block 728 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 728 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 728 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 728 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b347 ftm_brick 2
