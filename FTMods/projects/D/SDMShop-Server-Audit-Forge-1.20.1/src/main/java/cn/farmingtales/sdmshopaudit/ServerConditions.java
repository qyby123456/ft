package cn.farmingtales.sdmshopaudit;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;

public final class ServerConditions {
    private ServerConditions() {
    }

    public static void check(ServerPlayer player, Object entry, Object tab) throws ReflectiveOperationException {
        checkList(player, Bridge.field(tab, "tabConditions"));
        checkList(player, Bridge.field(entry, "entryConditions"));
    }

    private static void checkList(ServerPlayer player, Object conditions) throws ReflectiveOperationException {
        for (Object condition : (List<?>) conditions) {
            checkCondition(player, Bridge.serialize(condition));
        }
    }

    public static void checkCondition(ServerPlayer player, CompoundTag condition) throws ReflectiveOperationException {
        switch (condition.getString("entryConditionID")) {
            case "ftbquestConditions" -> quests(player, condition);
            case "gamestagesConditions" -> stages(player, condition);
            case "mnaTierCondition" -> progression(player, condition.getCompound("mnaTier"), true);
            case "mnaLevelCondition" -> progression(player, condition.getCompound("mnaLevel"), false);
            case "mnaFactionCondition" -> factions(player, condition);
            default -> throw new IllegalArgumentException("UNSUPPORTED_SERVER_CONDITION:" + condition.getString("entryConditionID"));
        }
    }

    private static void quests(ServerPlayer player, CompoundTag condition) throws ReflectiveOperationException {
        var ids = condition.getList("questID", Tag.TAG_STRING);
        if (ids.isEmpty()) {
            return;
        }
        Class<?> teamType = Class.forName("dev.ftb.mods.ftbquests.quest.TeamData");
        Object team = Bridge.invoke(teamType, "get", teamType, null, new Class<?>[]{Player.class}, player);
        Class<?> apiType = Class.forName("dev.ftb.mods.ftbquests.FTBQuestsAPIImpl");
        Class<?> fileType = Class.forName("dev.ftb.mods.ftbquests.quest.BaseQuestFile");
        Object file = Bridge.invoke(apiType, "getQuestFile", fileType, apiType.getField("INSTANCE").get(null),
                new Class<?>[]{boolean.class}, false);
        Class<?> questType = Class.forName("dev.ftb.mods.ftbquests.quest.Quest");
        Class<?> baseType = Class.forName("dev.ftb.mods.ftbquests.quest.QuestObjectBase");
        Class<?> objectType = Class.forName("dev.ftb.mods.ftbquests.quest.QuestObject");
        for (Tag id : ids) {
            long code = (long) Bridge.invoke(baseType, "parseCodeString", long.class, null,
                    new Class<?>[]{String.class}, id.getAsString());
            Object quest = Bridge.invoke(fileType, "getQuest", questType, file, new Class<?>[]{long.class}, code);
            if (quest == null || team == null || !(boolean) Bridge.invoke(teamType, "isCompleted", boolean.class,
                    team, new Class<?>[]{objectType}, quest)) {
                throw new IllegalArgumentException("QUEST_CONDITION_NOT_MET:" + id.getAsString());
            }
        }
    }

    private static void stages(ServerPlayer player, CompoundTag condition) throws ReflectiveOperationException {
        for (Tag stage : condition.getList("gameStages", Tag.TAG_STRING)) {
            if (!stage.getAsString().isEmpty() && !(boolean) Bridge.invoke(
                    Class.forName("net.darkhax.gamestages.GameStageHelper"), "hasStage", boolean.class, null,
                    new Class<?>[]{Player.class, String.class}, player, stage.getAsString())) {
                throw new IllegalArgumentException("GAME_STAGE_CONDITION_NOT_MET:" + stage.getAsString());
            }
        }
    }

    private static Object capability(ServerPlayer player, boolean progression) throws ReflectiveOperationException {
        String provider = progression ? "com.mna.capabilities.playerdata.progression.PlayerProgressionProvider"
                : "com.mna.capabilities.playerdata.magic.PlayerMagicProvider";
        Object value = Class.forName(provider).getField(progression ? "PROGRESSION" : "MAGIC").get(null);
        Object data = player.getCapability((Capability<?>) value).resolve().orElse(null);
        if (data == null) {
            throw new IllegalArgumentException("MNA_CAPABILITY_UNAVAILABLE");
        }
        return data;
    }

    private static void progression(ServerPlayer player, CompoundTag data, boolean tier) throws ReflectiveOperationException {
        if (!data.getBoolean(tier ? "useTierCondition" : "useLevelCondition")) {
            return;
        }
        Class<?> type = Class.forName(tier ? "com.mna.api.capabilities.IPlayerProgression" : "com.mna.api.capabilities.IPlayerMagic");
        int level = ((Number) Bridge.invoke(type, tier ? "getTier" : "getMagicLevel", int.class,
                capability(player, tier), new Class<?>[0])).intValue();
        int minimum = data.getInt("minLevel");
        int maximum = data.getInt("maxLevel");
        if (minimum < 0 || maximum < 0 || maximum != 0 && minimum > maximum
                || level < minimum || maximum != 0 && level > maximum) {
            throw new IllegalArgumentException("MNA_LEVEL_OR_TIER_CONDITION_NOT_MET");
        }
    }

    private static void factions(ServerPlayer player, CompoundTag condition) throws ReflectiveOperationException {
        var ids = condition.getList("factionID", Tag.TAG_STRING);
        if (ids.isEmpty()) {
            return;
        }
        Class<?> progression = Class.forName("com.mna.api.capabilities.IPlayerProgression");
        Class<?> factionType = Class.forName("com.mna.api.faction.IFaction");
        Object faction = Bridge.invoke(progression, "getAlliedFaction", factionType,
                capability(player, true), new Class<?>[0]);
        if (faction != null) {
            for (Tag id : ids) {
                ResourceLocation identifier = ResourceLocation.tryParse(id.getAsString());
                if (identifier == null) {
                    throw new IllegalArgumentException("INVALID_MNA_FACTION_ID");
                }
                if ((boolean) Bridge.invoke(factionType, "is", boolean.class, faction,
                        new Class<?>[]{ResourceLocation.class}, identifier)) {
                    return;
                }
            }
        }
        throw new IllegalArgumentException("MNA_FACTION_CONDITION_NOT_MET");
    }
}
