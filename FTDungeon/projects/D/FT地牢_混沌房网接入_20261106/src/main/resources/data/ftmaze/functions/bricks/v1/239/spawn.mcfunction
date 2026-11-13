scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1742 211 112 minecraft:air if block 1742 212 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1742 211 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1759 211 126 minecraft:air if block 1759 212 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1759 211 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 211 107 minecraft:air if block 1757 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1757 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1739 211 126 minecraft:air if block 1739 212 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1739 211 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1749 211 120 minecraft:air if block 1749 212 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1749 211 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1747 211 107 minecraft:air if block 1747 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1747 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1752 211 112 minecraft:air if block 1752 212 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1752 211 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 211 117 minecraft:air if block 1757 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1757 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1743 211 112 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1743 211 112 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b239 ftm_brick 2
