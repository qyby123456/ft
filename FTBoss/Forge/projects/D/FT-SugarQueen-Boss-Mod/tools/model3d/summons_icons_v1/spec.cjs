const summons = [
  ['sugar_queen_crystal', '糖霜水晶', 'crown', 'pink', 'pinkLight', 'gold', '王冠糖晶'],
  ['meat_heart', '屠夫之心', 'heart', 'red', 'coral', 'cream', '缝合血心'],
  ['tea_seed', '茶树种子', 'leaf', 'green', 'mint', 'gold', '萌芽茶种'],
  ['wine_charm', '酒引', 'glass', 'purple', 'lilac', 'gold', '封蜡酒瓶'],
  ['rat_bait', '奶酪诱惑', 'cheese', 'yellow', 'lemon', 'goldDark', '诱饵奶酪'],
  ['defect_core', '故障机器人', 'chip', 'steel', 'ice', 'cyan', '失控机芯'],
  ['compost_summon', '炫彩堆肥桶', 'sprout', 'wood', 'woodLight', 'green', '虹彩堆肥桶'],
  ['forbidden_apple', '禁果', 'apple', 'red', 'coral', 'gold', '封印禁果'],
  ['benger_whistle', '牧羊哨', 'paw', 'steel', 'silver', 'gold', '犬爪牧羊哨'],
  ['benger_spawn_egg', 'benger 刷怪蛋', 'paw', 'dark', 'silver', 'gold', '边牧纹章蛋'],
  ['easter_egg_youzi', '刷怪蛋·柚子', 'apple', 'yellow', 'lemon', 'green', '柚瓣灵蛋'],
  ['easter_egg_dandan', '刷怪蛋·蛋蛋', 'moon', 'cream', 'white', 'purple', '月翼灵蛋'],
  ['easter_egg_duola', '刷怪蛋·朵拉', 'star', 'pink', 'pinkLight', 'gold', '星芒灵蛋'],
  ['easter_egg_gongdi', '刷怪蛋·功底', 'axe', 'dark', 'slate', 'red', '血斧灵蛋'],
  ['easter_egg_ningmengmao', '刷怪蛋·柠檬猫', 'cat', 'yellow', 'lemon', 'green', '猫耳柠蛋'],
  ['easter_egg_qingyi', '刷怪蛋·青衣', 'trident', 'greenDark', 'cyan', 'ice', '潮汐灵蛋'],
  ['easter_egg_wenxuanlan', '刷怪蛋·文玄蓝', 'wheel', 'blue', 'steel', 'silver', '飞轮灵蛋'],
  ['easter_egg_zheergen', '刷怪蛋·折耳根', 'root', 'purpleDark', 'purple', 'lime', '虚空根蛋'],
  ['easter_egg_zicai', '刷怪蛋·紫菜', 'fish', 'purple', 'lilac', 'pink', '河豚灵蛋'],
  ['easter_egg_xueli', '刷怪蛋·雪黎', 'chef', 'ice', 'white', 'blue', '厨冠灵蛋']
].map(([id, name, motif, base, light, trim, concept]) => ({ id, name, motif, base, light, trim, concept }));
const discs = [
  ['manor_cue', '庄园 · 序', 'tower', 'gold', 'goldLight'], ['manor_battle', '庄园 · 战', 'swords', 'red', 'rose'],
  ['manor_dual', '庄园 · 双轨', 'dual', 'blue', 'ice'], ['manor_guignol', '庄园 · 鏖战', 'mask', 'purple', 'lilac'],
  ['apple', '苹果爵士', 'apple', 'red', 'rose'], ['apple_p2', '苹果爵士 · 二阶段', 'apple', 'green', 'lime'],
  ['gingerbread', '糖霜女皇', 'crown', 'pink', 'pinkLight'], ['butcher', '屠夫', 'heart', 'redDark', 'coral'],
  ['saya', '沙耶', 'leaf', 'green', 'mint'], ['tavern', '酒桶王', 'glass', 'purple', 'goldLight'],
  ['rat', '鼠群', 'cheese', 'gold', 'lemon'], ['defect', '故障机器人', 'chip', 'blueDark', 'cyan'],
  ['compost', '炫彩答辩超人', 'sprout', 'wood', 'lime'], ['compost_p2', '炫彩答辩超人 · 二阶段', 'sprout', 'purple', 'pinkLight'],
  ['benger', '边牧犬王', 'paw', 'slate', 'silver']
].map(([suffix, name, motif, base, light]) => ({ id: `music_disc_${suffix}`, name, motif, base, light }));
const effects = [
  ['infection', '感染'], ['poop_confusion', '屎？！！'], ['shadow_erosion', '虚空侵蚀'], ['heal_ban', '禁疗'],
  ['blood_rage', '血怒'], ['blue_fire', '蓝火'], ['qingyi_charge', '充能'], ['bleed', '流血'],
  ['shadow_blind', '暗影笼罩'], ['tremor', '震颤'], ['blood_rage_fake', '血怒·伪'], ['armor_shred', '护甲撕裂']
].map(([id, name]) => ({ id, name }));
module.exports = { summons, discs, effects };
