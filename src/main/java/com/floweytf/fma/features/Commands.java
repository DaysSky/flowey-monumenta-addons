package com.floweytf.fma.features;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.FMAConfig;
import com.floweytf.fma.chat.ChatChannel;
import com.floweytf.fma.chat.ChatChannelManager;
import com.floweytf.fma.debug.Debug;
import com.floweytf.fma.util.ChatUtil;
import com.floweytf.fma.util.FormatUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.floweytf.fma.util.CommandUtil.*;

public class Commands {
    private static CommandDispatcher<CommandSourceStack> CLIENT_DISPATCH;
    private static Set<String> COMMAND_INITIATORS = new HashSet<>();
    private static final Set<String> SUGGESTION_INITIATORS = new HashSet<>();

    private static CommandNode<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> node) {
        COMMAND_INITIATORS.add(node.getLiteral());
        SUGGESTION_INITIATORS.add(node.getLiteral());
        return CLIENT_DISPATCH.register(node);
    }

    private static CommandNode<CommandSourceStack> registerHidden(LiteralArgumentBuilder<CommandSourceStack> node) {
        COMMAND_INITIATORS.add(node.getLiteral());
        return CLIENT_DISPATCH.register(node);
    }

    private static void chatWrapper(String name, ChatChannel channel) {
        registerHidden(lit(name,
            // forward
            context -> {
                ChatChannelManager.getInstance().setChannel(channel);
                return 0;
            },
            arg("text", StringArgumentType.greedyString(), context -> {
                final var arg = StringArgumentType.getString(context, "text");
                ChatUtil.sendCommand(String.format("%s %s", name, arg));
                return 0;
            })
        ));
    }

    private static void alias(String name, String target) {
        register(lit(name,
            // forward
            context -> {
                if (parseAccepts(target)) {
                    run(target);
                } else {
                    ChatUtil.sendCommand(target);
                }

                return 0;
            }
        ));
    }

    private static <T> T opt(T arg, boolean flag) {
        if (flag) {
            return arg;
        }

        return null;
    }

    private static void registerCommands(FMAConfig config) {
        CLIENT_DISPATCH = new CommandDispatcher<>();
        COMMAND_INITIATORS = new HashSet<>();
        final var debug = config.features.enableDebug;

        final var fma = register(lit(
            "fma",
            lit("debug", ignored -> {
                Debug.runDebug();
                return 0;
            }),
            opt(lit("debug_test", ignored -> {
                ChatUtil.send(":3");
                return 0;
            }), debug),
            opt(lit("debug_reload", ignored -> {
                registerCommands(FMAClient.CONFIG.get());
                return 0;
            }), debug),
            opt(lit("debug_e", ignored -> {
                Debug.entityDebug = !Debug.entityDebug;
                ChatUtil.send("Entity Debug: " + Debug.entityDebug);
                return 0;
            }), debug),
            opt(lit("debug_b", ignored -> {
                Debug.blockDebug = !Debug.blockDebug;
                ChatUtil.send("Block Debug: " + Debug.entityDebug);
                return 0;
            }), debug),
            opt(lit("dump_e", context -> {
                FMAClient.level().entitiesForRendering().forEach(e -> {
                    if (e.getEyePosition().distanceTo(FMAClient.player().getEyePosition()) < 10) {
                        Debug.dumpEntityInfo(e);
                    }
                });
                return 0;
            }), debug),
            lit("help", ignored -> {
                ChatUtil.send(Component.literal("Command Help").withStyle(ChatFormatting.BOLD));

                ChatUtil.send("/cc - clear chat");
                ChatUtil.send("/omw - shorthand for /lfg omw");
                ChatUtil.send("/fma debug - dumps internal state, don't use this unless something breaks");
                ChatUtil.send("/fma lb [leaderboard] - show your leaderboard position");
                ChatUtil.send("/fma config - opens the config");
                ChatUtil.send("/fma help - prints this message");
                ChatUtil.send("/lb -> /fma lb");
                return 0;
            }),
            lit("lb",
                mcArg("lb_name", StringArgumentType.word(), context -> {
                    final var lbName = StringArgumentType.getString(context, "lb_name");
                    FMAClient.LEADERBOARD.beginListen(lbName, (position, count) -> {
                        ChatUtil.send(Component.translatable(
                            "commands.fma.leaderboard",
                            FormatUtil.altText(lbName),
                            FormatUtil.numeric(position),
                            FormatUtil.playerNameText(FMAClient.playerName()),
                            FormatUtil.numeric(count)
                        ));
                    });

                    return 0;
                }).suggests((context, builder) -> SharedSuggestionProvider.suggest(List.of(
                    "Portal"
                ), builder))
            ),
            lit("config", context -> {
                FMAClient.SCHEDULER.schedule(0,
                    minecraft -> minecraft.setScreen(AutoConfig.getConfigScreen(FMAConfig.class, minecraft.screen).get()));
                return 0;
            })
        ));

        register(lit("cc", ignored -> {
            Minecraft.getInstance().gui.getChat().clearMessages(false);
            return 0;
        }));

        register(lit("omw",
            // forward
            context -> {
                ChatUtil.sendCommand("lfg omw");
                return 0;
            },
            arg("text", StringArgumentType.greedyString(), context -> {
                final var arg = StringArgumentType.getString(context, "text");
                ChatUtil.sendCommand(String.format("lfg omw %s", arg));
                return 0;
            })
        ));

        registerHidden(lit("tell", arg("player", StringArgumentType.word(),
            // forward
            context -> {
                final var player = StringArgumentType.getString(context, "player");
                ChatChannelManager.getInstance().openDm(player);
                return 0;
            },
            arg("text", StringArgumentType.greedyString(), context -> {
                final var player = StringArgumentType.getString(context, "player");
                final var arg = StringArgumentType.getString(context, "text");
                ChatUtil.sendCommand(String.format("tell %s %s", player, arg));
                ChatChannelManager.getInstance().openDm(player);
                return 0;
            })
        )));

        register(mcLit("lb").redirect(fma.getChild("lb")));
        alias("lbp", "lb Portal");

        // register the commands
        for (int i = 0; i < config.chatChannels.channels.size(); i++) {
            final var channel = config.chatChannels.channels.get(i);
            if (channel.shorthandCommand.isEmpty())
                return;

            final var manager = ChatChannelManager.getInstance();
            int index = i + 1;
            registerHidden(mcLit(channel.shorthandCommand, context -> {
                manager.setChannel(manager.getSystemChannels().get(index));
                return 0;
            }));
        }
    }

    public static void init() {
        registerCommands(FMAClient.CONFIG.getConfig());
        FMAClient.CONFIG.registerSaveListener((configHolder, fmaConfig) -> {
            registerCommands(fmaConfig);
            return InteractionResult.PASS;
        });
    }

    public static CommandDispatcher<CommandSourceStack> getDispatcher() {
        return CLIENT_DISPATCH;
    }

    public static boolean parseAccepts(String str) {
        final var parts = str.split("\\s");
        if (parts.length == 0)
            return false;
        return COMMAND_INITIATORS.contains(parts[0]);
    }

    public static boolean suggestAccepts(String str) {
        final var parts = str.split("\\s");
        if (parts.length == 0)
            return false;
        return SUGGESTION_INITIATORS.contains(parts[0]);
    }

    public static void run(String str) {
        try {
            CLIENT_DISPATCH.execute(str, FMAClient.player().createCommandSourceStack());
        } catch (CommandSyntaxException e) {
            ChatUtil.sendWarn(e.getMessage());
        }
    }
}
