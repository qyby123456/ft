from pathlib import Path

from deliver import ROOT

BASE = ROOT.parents[1]
SOURCES = [BASE / 'deliveries/nature-b1-S8-crystal', BASE / 'deliveries/nature-b1-S8-crystal-r2', BASE / 'deliveries/nature-S8-sequential', BASE / 'deliveries/nature-batch1-group4-swamp', BASE / 'deliveries/nature-batch1-S6', BASE / 'deliveries/nature-S5-coral-ocean-20261104', Path('D:/ft工程文件/FT地牢_首批第3组_冰原霜穴_20261104'), Path('D:/ft工程文件/FT地牢_自然环境第7组_蘑菇真菌_20261104'), Path('D:/ft工程文件/FT地牢_自然环境第一批第2组_20261104')]


def main():
    from takeover_audit import main as run_audit
    run_audit()


if __name__ == '__main__':
    main()
