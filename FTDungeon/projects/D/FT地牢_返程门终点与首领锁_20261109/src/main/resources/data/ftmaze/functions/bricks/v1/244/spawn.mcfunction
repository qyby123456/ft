scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1742 211 31 minecraft:air if block 1742 212 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1742 211 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1759 211 44 minecraft:air if block 1759 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1759 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 211 25 minecraft:air if block 1757 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1757 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1739 211 44 minecraft:air if block 1739 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1739 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1749 211 39 minecraft:air if block 1749 212 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1749 211 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1753 211 32 minecraft:air if block 1753 212 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1753 211 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1746 211 25 minecraft:air if block 1746 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1746 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 211 37 minecraft:air if block 1757 212 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1757 211 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1743 211 31 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1743 211 31 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b244 ftm_brick 2
