scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1742 211 71 minecraft:air if block 1742 212 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1742 211 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1759 211 86 minecraft:air if block 1759 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1759 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1758 211 66 minecraft:air if block 1758 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1758 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1739 211 86 minecraft:air if block 1739 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1739 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1749 211 80 minecraft:air if block 1749 212 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1749 211 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1752 211 72 minecraft:air if block 1752 212 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1752 211 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1747 211 66 minecraft:air if block 1747 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1747 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 211 77 minecraft:air if block 1757 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1757 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1743 211 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1743 211 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b241 ftm_brick 2
