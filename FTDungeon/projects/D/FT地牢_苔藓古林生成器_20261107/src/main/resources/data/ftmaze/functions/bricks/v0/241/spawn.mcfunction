scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1742 31 71 minecraft:air if block 1742 32 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1742 31 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1759 31 86 minecraft:air if block 1759 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1759 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1758 31 66 minecraft:air if block 1758 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1758 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1739 31 86 minecraft:air if block 1739 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1739 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1749 31 80 minecraft:air if block 1749 32 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1749 31 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1752 31 72 minecraft:air if block 1752 32 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1752 31 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1747 31 66 minecraft:air if block 1747 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1747 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 31 77 minecraft:air if block 1757 32 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1757 31 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1743 31 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1743 31 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b241 ftm_brick 2
