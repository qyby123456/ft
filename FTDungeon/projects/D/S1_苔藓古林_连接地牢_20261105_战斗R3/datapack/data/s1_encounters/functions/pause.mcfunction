scoreboard players set #enabled s1_enc 0
bossbar set s1_encounters:king visible false
bossbar set s1_encounters:king players
tellraw @a {"text":"S1 新遭遇已暂停；现有怪物仍会攻击，未被清除。","color":"yellow"}
