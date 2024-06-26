package com.floweytf.fma.registry;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.chat.ChatChannel;
import com.floweytf.fma.chat.ChatChannelManager;
import com.floweytf.fma.util.Utils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

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
                Utils.sendCommand(String.format("%s %s", name, arg));
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
                    Utils.sendCommand(target);
                }

                return 0;
            }
        ));
    }

    private static <T> T debug(T arg) {
        if (FMAClient.CONFIG.enableDebug) {
            return arg;
        }
        return null;
    }

    public static void load() {
        CLIENT_DISPATCH = new CommandDispatcher<>();
        COMMAND_INITIATORS = new HashSet<>();

        final var fma = register(lit(
            "fma",
            lit("debug", ignored -> {
                FMAClient.runDebug();
                return 0;
            }),
            debug(lit("debug_test", ignored -> {
                Utils.send(":3");
                return 0;
            })),
            debug(lit("debug_reload", ignored -> {
                load();
                return 0;
            })),
            debug(lit("debug_e", ignored -> {
                FMAClient.entityDebug = !FMAClient.entityDebug;
                Utils.send(
                    Component.literal("Entity Debug: "),
                    Utils.formatEnable(FMAClient.entityDebug)
                );
                return 0;
            })),
            debug(lit("debug_b", ignored -> {
                FMAClient.blockDebug = !FMAClient.blockDebug;
                Utils.send(
                    Component.literal("Block Debug: "),
                    Utils.formatEnable(FMAClient.blockDebug)
                );
                return 0;
            })),
            debug(lit("dump_e", context -> {
                ((ClientLevel) Utils.player().getLevel()).entitiesForRendering().forEach(e -> {
                    if (e.getEyePosition().distanceTo(Utils.player().getEyePosition()) < 10) {
                        FMAClient.dumpEntityInfo(e);
                    }
                });
                return 0;
            })),
            lit("help", ignored -> {
                Utils.send(Component.literal("Command Help").withStyle(ChatFormatting.BOLD));

                Utils.send("/cc - clear chat");
                Utils.send("/fma debug - dumps internal state, don't use this unless something breaks");
                Utils.send("/fma lb [leaderboard] - show your leaderboard position");
                return 0;
            }),
            lit("lb",
                mcArg("lb_name", StringArgumentType.word(), context -> {
                    final var lbName = StringArgumentType.getString(context, "lb_name");
                    FMAClient.LEADERBOARD.beginListen(lbName, (position, count) -> {
                        Utils.send(
                            Component.literal(lbName).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE),
                            Component.literal(": "),
                            Component.literal("" + position).withStyle(ChatFormatting.DARK_GREEN),
                            Component.literal(". "),
                            Component.literal(Utils.player().getScoreboardName())
                                .withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD),
                            Component.literal(" - "),
                            Component.literal("" + count).withStyle(ChatFormatting.DARK_GREEN)
                        );
                    });

                    return 0;
                }).suggests((context, builder) -> SharedSuggestionProvider.suggest(List.of(
                    "Portal"
                ), builder))
            )
        ));

        register(lit("cc", ignored -> {
            Minecraft.getInstance().gui.getChat().clearMessages(false);
            return 0;
        }));

        chatWrapper("wc", ChatChannelManager.WORLD_CHAT);
        chatWrapper("gc", ChatChannelManager.GUILD);
        chatWrapper("g", ChatChannelManager.GLOBAL);
        chatWrapper("l", ChatChannelManager.LOCAL);
        chatWrapper("lfg", ChatChannelManager.LFG);
        chatWrapper("tr", ChatChannelManager.TR);

        register(lit("omw",
            // forward
            context -> {
                Utils.sendCommand("lfg omw");
                return 0;
            },
            arg("text", StringArgumentType.greedyString(), context -> {
                final var arg = StringArgumentType.getString(context, "text");
                Utils.sendCommand(String.format("lfg omw %s", arg));
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
                Utils.sendCommand(String.format("tell %s %s", player, arg));
                ChatChannelManager.getInstance().openDm(player);
                return 0;
            })
        )));

        register(mcLit("lb").redirect(fma.getChild("lb")));
        alias("lbp", "lb Portal");
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
            CLIENT_DISPATCH.execute(str, Utils.player().createCommandSourceStack());
        } catch (CommandSyntaxException e) {
            Utils.sendWarn(e.getMessage());
        }
    }
}
