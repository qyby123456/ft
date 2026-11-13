scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1742 31 112 minecraft:air if block 1742 32 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1742 31 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1759 31 126 minecraft:air if block 1759 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1759 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 31 107 minecraft:air if block 1757 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1757 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1739 31 126 minecraft:air if block 1739 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1739 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1749 31 120 minecraft:air if block 1749 32 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1749 31 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1747 31 107 minecraft:air if block 1747 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1747 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1752 31 112 minecraft:air if block 1752 32 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1752 31 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 31 117 minecraft:air if block 1757 32 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1757 31 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1743 31 112 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1743 31 112 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b239 ftm_brick 2
