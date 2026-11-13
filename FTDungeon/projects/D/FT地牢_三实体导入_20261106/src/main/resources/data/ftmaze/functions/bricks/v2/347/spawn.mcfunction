scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 727 91 198 minecraft:air if block 727 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 727 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 717 91 202 minecraft:air if block 717 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 717 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 732 91 202 minecraft:air if block 732 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 732 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 720 91 198 minecraft:air if block 720 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 720 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 724 91 202 minecraft:air if block 724 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 724 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 721 91 201 minecraft:air if block 721 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 721 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 723 91 199 minecraft:air if block 723 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 723 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 728 91 201 minecraft:air if block 728 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 728 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 728 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 728 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b347 ftm_brick 2
