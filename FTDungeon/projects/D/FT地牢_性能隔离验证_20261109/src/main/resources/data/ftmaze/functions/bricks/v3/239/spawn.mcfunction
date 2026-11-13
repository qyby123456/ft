scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1742 151 112 minecraft:air if block 1742 152 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1742 151 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1759 151 126 minecraft:air if block 1759 152 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1759 151 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 151 107 minecraft:air if block 1757 152 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1757 151 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1739 151 126 minecraft:air if block 1739 152 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1739 151 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1749 151 120 minecraft:air if block 1749 152 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1749 151 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1747 151 107 minecraft:air if block 1747 152 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1747 151 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1752 151 112 minecraft:air if block 1752 152 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1752 151 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 151 117 minecraft:air if block 1757 152 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1757 151 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1743 151 112 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1743 151 112 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b239 ftm_brick 2
