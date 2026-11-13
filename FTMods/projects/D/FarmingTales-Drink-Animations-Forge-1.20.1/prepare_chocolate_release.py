from pathlib import Path

ROOT = Path(__file__).resolve().parent

def replace(path, old, new):
    path = ROOT / path
    text = path.read_text(encoding='utf-8')
    assert old in text, (path, old)
    path.write_text(text.replace(old, new), encoding='utf-8')

for file in ['build.gradle', 'src/main/resources/META-INF/mods.toml']:
    replace(file, '1.8.25', '1.8.26')
for file in ['src/smoke/java/com/farmingtales/drinkanim/smoke/SmokeFixture.java',
             'src/smoke/java/com/farmingtales/drinkanim/smoke/CatalogSmoke.java']:
    replace(file, 'profile.id().endsWith("_jam_1")', 'profile.sweetJam()')
replace('src/smoke/java/com/farmingtales/drinkanim/smoke/CatalogSmoke.java',
        'count==129,"all 129 jar resources loaded"', 'count==130,"all 130 jar resources loaded"')
tests = 'src/test/java/com/farmingtales/drinkanim/math/DrinkProfilesTest.java'
for old, new in [(182,183),(134,135),(129,130)]:
    replace(tests, f'assertEquals({old},', f'assertEquals({new},')
replace(tests, '        assertNull(DrinkCatalog.find("minecraft:potion"));', '''        var chocolate=DrinkCatalog.find("farmingtales:chocolate_sauce");
        assertNotNull(chocolate);
        assertTrue(chocolate.sweetJam());
        assertEquals(AnimationSoundTimeline.drinking(DrinkCatalog.find("farmingtales:ancient_fruit_jam_1"),16),
            AnimationSoundTimeline.drinking(chocolate,16));
        assertNull(DrinkCatalog.find("farmingtales:chocolate_sauce_2"));
        assertNull(DrinkCatalog.find("minecraft:potion"));''')
replace('README.md', '动画 1.8.25', '动画 1.8.26')
replace('README.md', 'Minecraft 1.20.1 / Forge 47.4.10 客户端独立模组。',
        'Minecraft 1.20.1 / Forge 47.4.10 客户端独立模组。\n\n'
        '1.8.26：补齐 farmingtales:chocolate_sauce 巧克力酱，使用原整合包贴图与已确认的酱罐动作、黏稠流体及蜂蜜食用音效。'
        '取出 0.5 秒，放回 0.45 秒。新增 Steve、纤细手臂 Blockbench 补充项目；其余物品沿用 v54。')
installer = ROOT / 'install_release.ps1'
text = installer.read_text(encoding='utf-8')
text = text.replace('1.8.25','1.8.26').replace('v1825','v1826')
text = text.replace('1.8.24-before-switch-tuning','1.8.25-before-chocolate-sauce').replace('1.8.24','1.8.25')
installer.write_text(text,encoding='utf-8')
print('Prepared 1.8.26 chocolate sauce registration and sweet-jar sound classification.')
