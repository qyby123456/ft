kill @e[tag=s1_encounter]
scoreboard players set #r2 s1_enc 0
scoreboard players set #r3 s1_enc 0
scoreboard players set #r4 s1_enc 0
scoreboard players set #r5 s1_enc 0
scoreboard players set #r6 s1_enc 0
scoreboard players set #r8 s1_enc 0
scoreboard players set #r9 s1_enc 0
scoreboard players set #r1 s1_enc 0
bossbar set s1_encounters:king visible false
bossbar set s1_encounters:king players
tellraw @a {"text":"S1 已重置：清除本包标记怪物，8 处遭遇点恢复待触发。未清理原生 Boss 自行召唤的无标签援军。","color":"yellow"}
