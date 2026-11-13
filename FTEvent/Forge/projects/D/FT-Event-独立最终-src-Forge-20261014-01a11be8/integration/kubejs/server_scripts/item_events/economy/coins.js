const FTCOIN_SDM_SHOP = Java.loadClass("net.sixik.sdmshoprework.SDMShopR");

const FTCOIN_BUNDLES = [
  {
    itemId: "farmingtales:gold_bundle",
    coinValue: 100,
    sound: "supplementaries:block.sack.open",
  },
  {
    itemId: "farmingtales:gold_chest",
    coinValue: 1000,
    sound: "block.barrel.open",
  },
  {
    itemId: "farmingtales:gold_bucket",
    coinValue: 10000,
    sound: "minecraft:block.grindstone.use",
  },
];

FTCOIN_BUNDLES.forEach((data) => {
  ItemEvents.rightClicked(data.itemId, (event) => {
    const { player, item } = event;
    let wellGuard = false;
    try {
      if (data.itemId === 'farmingtales:gold_bucket' && global.ftwwGoldUseBlocked) wellGuard = !!global.ftwwGoldUseBlocked(player);
    } catch (error) {
      console.error('[FarmingTales][WishingWell] gold pot use guard failed: ' + error);
      wellGuard = true;
    }
    if (wellGuard) {
      console.info('[FarmingTales][WishingWell] gold pot use blocked by well guard hand=' + String(event.hand) + ' player=' + String(player.username));
      event.cancel();
      return;
    }
    const useCount = player.isShiftKeyDown() ? item.count : 1;
    const depositAmount = useCount * data.coinValue;

    player.runCommandSilent(`playsound ${data.sound} player @s`);
    if (!player.isCreative()) item.shrink(useCount);
    FTCOIN_SDM_SHOP.addMoney(player, depositAmount);
    player.server.runCommandSilent(
      `emberstextapi sendcustom ${player.username} {anchor:"TOP_LEFT",bgColor:1,color:"#FFFFFF",size:1,offsetY:16,offsetX:-60,typewriter:1,align:"TOP_LEFT"} 30 \u00A76+${depositAmount}`
    );
    player.displayClientMessage(Text.of(`\u00A76+${depositAmount}\u00A7f\uAA31`), true);
    player.addItemCooldown(data.itemId, 10);
    player.swing();
  });
});
