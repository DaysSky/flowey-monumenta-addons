package com.floweytf.fma.features;

import com.floweytf.fma.FMAClient;
import com.floweytf.fma.FMAConfig;
import com.floweytf.fma.debug.Debug;
import com.floweytf.fma.features.chat.ChatChannel;
import com.floweytf.fma.util.ChatUtil;
import static com.floweytf.fma.util.CommandUtil.*;
import com.floweytf.fma.util.FormatUtil;
import static com.floweytf.fma.util.FormatUtil.join;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

public class Commands {
    private final CommandDispatcher<CommandSourceStack> clientDispatch = new CommandDispatcher<>();
    private final Set<String> commandInitiators = new HashSet<>();
    private final Set<String> suggestionInitiators = new HashSet<>();

    private CommandNode<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> node) {
        commandInitiators.add(node.getLiteral());
        suggestionInitiators.add(node.getLiteral());
        return clientDispatch.register(node);
    }

    private CommandNode<CommandSourceStack> registerHidden(LiteralArgumentBuilder<CommandSourceStack> node) {
        commandInitiators.add(node.getLiteral());
        return clientDispatch.register(node);
    }

    private void chatWrapper(String name, ChatChannel channel) {
        registerHidden(lit(name,
            // forward
            context -> {
                FMAClient.CHAT_CHANNELS.setChannel(channel);
                return 0;
            },
            arg("text", StringArgumentType.greedyString(), context -> {
                final var arg = StringArgumentType.getString(context, "text");
                ChatUtil.sendCommand(channel.buildSendCommand(arg));
                return 0;
            })
        ));
    }

    private void alias(String name, String target) {
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

    public Commands(FMAConfig config) {
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
                FMAClient.reload();
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
            opt(lit("dumpnbt", context -> {
                ChatUtil.send(join(
                    Component.literal("Data: "),
                    NbtUtils.toPrettyComponent(FMAClient.player().getItemInHand(InteractionHand.MAIN_HAND).getTag())
                ));
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
                ChatUtil.send("/fma version - displays version info");
                ChatUtil.send("/lb -> /fma lb");
                return 0;
            }),
            lit("version", ignored -> {
                ChatUtil.send(FMAClient.MOD.getMetadata().getVersion().getFriendlyString());
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

        register(mcLit("lb").redirect(fma.getChild("lb")));
        alias("lbp", "lb Portal");

        // Macro alias, maybe customizable?
        alias("gg", "/g " + FMAClient.config().chat.ggText);

        // register the commands
        if (config.features.enableChatChannels) {
            registerHidden(lit("tell", arg("player", StringArgumentType.word(),
                // forward
                context -> {
                    final var player = StringArgumentType.getString(context, "player");
                    FMAClient.CHAT_CHANNELS.openDm(player);
                    return 0;
                },
                arg("text", StringArgumentType.greedyString(), context -> {
                    final var player = StringArgumentType.getString(context, "player");
                    final var arg = StringArgumentType.getString(context, "text");
                    ChatUtil.sendCommand(String.format("tell %s %s", player, arg));
                    FMAClient.CHAT_CHANNELS.openDm(player);
                    return 0;
                })
            )));

            for (int i = 0; i < config.chat.channels.size(); i++) {
                final var channel = config.chat.channels.get(i);
                if (channel.shorthandCommand.isEmpty())
                    return;

                final var manager = FMAClient.CHAT_CHANNELS;
                int index = i + 1;
                chatWrapper(channel.shorthandCommand, manager.getSystemChannels().get(index));
            }
        }
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return clientDispatch;
    }

    public boolean parseAccepts(String str) {
        final var parts = str.split("\\s");
        if (parts.length == 0)
            return false;
        return commandInitiators.contains(parts[0]);
    }

    public boolean suggestAccepts(String str) {
        final var parts = str.split("\\s");
        if (parts.length == 0)
            return false;
        return suggestionInitiators.contains(parts[0]);
    }

    public void run(String str) {
        try {
            clientDispatch.execute(str, FMAClient.player().createCommandSourceStack());
        } catch (CommandSyntaxException e) {
            ChatUtil.sendWarn(e.getMessage());
        }
    }
}
