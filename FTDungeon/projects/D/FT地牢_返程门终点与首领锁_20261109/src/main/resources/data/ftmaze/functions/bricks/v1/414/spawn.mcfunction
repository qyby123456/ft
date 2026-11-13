scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1731 211 198 minecraft:air if block 1731 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1731 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1721 211 202 minecraft:air if block 1721 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1721 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1736 211 202 minecraft:air if block 1736 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1736 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1724 211 198 minecraft:air if block 1724 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1724 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1728 211 202 minecraft:air if block 1728 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1728 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1725 211 201 minecraft:air if block 1725 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1725 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1727 211 199 minecraft:air if block 1727 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1727 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1732 211 201 minecraft:air if block 1732 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1732 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1732 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1732 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b414 ftm_brick 2
