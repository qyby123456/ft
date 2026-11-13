package cn.farmingtales.sdmshopaudit;

import com.mojang.logging.LogUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.EventPriority;
import org.slf4j.Logger;

@Mod(AuditMod.MOD_ID)
public final class AuditMod {
    public static final String MOD_ID = "sdmshop_audit";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AuditMod(FMLJavaModLoadingContext context) {
        if (!FMLEnvironment.dist.isDedicatedServer()) {
            return;
        }
        context.registerConfig(ModConfig.Type.COMMON, AuditConfig.SPEC, "sdmshop-audit.toml");
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::start);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::stop);
        MinecraftForge.EVENT_BUS.addListener(this::tick);
        MinecraftForge.EVENT_BUS.addListener(this::commands);
    }

    private void start(ServerStartedEvent event) {
        SecurityService.start(event.getServer());
        AuditService.start(event.getServer());
    }

    private void stop(ServerStoppingEvent event) {
        SecurityService.stop();
        AuditService.stop();
    }

    private void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            SecurityService.tick();
            AuditService.tick();
        }
    }

    private void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("sdmaudit").requires(source -> source.hasPermission(2))
                .then(Commands.literal("status").executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal(AuditService.status()), false);
                    return 1;
                }))
                .then(Commands.literal("snapshot").executes(context -> {
                    AuditService.manualSnapshot(context.getSource().getPlayer());
                    context.getSource().sendSuccess(() -> Component.literal("SDMShop 审计快照已提交；没有修改或还原商店。"), false);
                    return 1;
                }))
                .then(Commands.literal("trust-command").requires(source -> source.hasPermission(4))
                        .then(Commands.argument("tab", UuidArgument.uuid())
                                .then(Commands.argument("entry", UuidArgument.uuid()).executes(context -> {
                                    try {
                                        String fingerprint = SecurityService.approve(context.getSource().getPlayer(),
                                                UuidArgument.getUuid(context, "tab"), UuidArgument.getUuid(context, "entry"));
                                        context.getSource().sendSuccess(() -> Component.literal("指令商品已明确批准，指纹=" + fingerprint), true);
                                        return 1;
                                    } catch (Exception exception) {
                                        context.getSource().sendFailure(Component.literal("批准失败：" + exception.getMessage()));
                                        return 0;
                                    }
                                }))))
                .then(Commands.literal("revoke-command").requires(source -> source.hasPermission(4))
                        .then(Commands.argument("fingerprint", StringArgumentType.word()).executes(context -> {
                            try {
                                boolean revoked = SecurityService.revoke(context.getSource().getPlayer(),
                                        StringArgumentType.getString(context, "fingerprint"));
                                context.getSource().sendSuccess(() -> Component.literal(revoked ? "已撤销自定义指令批准。"
                                        : "没有此自定义批准；内置原版指令不在自定义列表内。"), true);
                                return revoked ? 1 : 0;
                            } catch (Exception exception) {
                                context.getSource().sendFailure(Component.literal("撤销失败：" + exception.getMessage()));
                                return 0;
                            }
                        }))));
    }
}
