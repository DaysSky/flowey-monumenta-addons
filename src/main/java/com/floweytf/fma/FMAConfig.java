package com.floweytf.fma;

import com.floweytf.fma.features.chat.SystemChatChannel;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

@Config(name = "fma")
public class FMAConfig implements ConfigData {
    public static class InventoryOverlayToggles {
        public boolean enable = true;
        public boolean enableRarity = true;
        public boolean enableCZCharmRarity = true;
        public boolean enableCooldown = true;
        public boolean enableCZCharmPower = true;
    }

    public static class FeatureToggles {
        public boolean enableChatChannels = false;
        public boolean enableHpIndicators = true;
        public boolean enableTimerAndStats = true;
        public boolean enableSideBar = true;
        public boolean enableVanillaEffectInUMMHud = false;
        public boolean enableVanityDurability = true;
        public boolean enableCustomSplash = true;
        @ConfigEntry.Gui.CollapsibleObject
        public InventoryOverlayToggles inventoryOverlay = new InventoryOverlayToggles();
        public boolean enableDebug = SharedConstants.IS_RUNNING_IN_IDE;
        public boolean suppressDebugWarning = !SharedConstants.IS_RUNNING_IN_IDE;
    }

    public static class Appearance {
        @ConfigEntry.ColorPicker
        public int bracketColor = 0xb7bdf8;
        @ConfigEntry.ColorPicker
        public int tagColor = 0xc6a0f6;

        public String tagText = "MAID";

        @ConfigEntry.ColorPicker
        public int textColor = 0xf4dbd6;
        @ConfigEntry.ColorPicker
        public int numericColor = 0xf38ba8;
        @ConfigEntry.ColorPicker
        public int detailColor = 0x6c6f85;
        @ConfigEntry.ColorPicker
        public int playerNameColor = 0xef9f76;
        @ConfigEntry.ColorPicker
        public int altTextColor = 0xb4befe;

        @ConfigEntry.ColorPicker
        public int errorColor = 0xe64553;
        @ConfigEntry.ColorPicker
        public int warningColor = 0xdf8e1d;
    }

    public static class HpIndicator {
        public boolean enableGlowingPlayer = true;
        public boolean enableHitboxColoring = true;
        public boolean countAbsorptionAsHp = true;

        public boolean smoothColor = false;

        @ConfigEntry.BoundedDiscrete(max = 100)
        public int goodHpPercent = 70;
        @ConfigEntry.BoundedDiscrete(max = 100)
        public int mediumHpPercent = 50;
        @ConfigEntry.BoundedDiscrete(max = 100)
        public int lowHpPercent = 25;

        @ConfigEntry.ColorPicker
        public int goodHpColor = 0x33ef2f;
        @ConfigEntry.ColorPicker
        public int mediumHpColor = 0xd9ef2f;
        @ConfigEntry.ColorPicker
        public int lowHpColor = 0xefa22f;
        @ConfigEntry.ColorPicker
        public int criticalHpColor = 0xef472d;
    }

    public static class Zenith {
        @ConfigEntry.Gui.PrefixText
        public boolean enableCustomCharmInfo = true;
        @ConfigEntry.Gui.Tooltip
        public boolean disableMonumentaLore = true; // This feature is beta
        @ConfigEntry.Gui.Tooltip
        public boolean enableStatBreakdown = false;
        @ConfigEntry.Gui.Tooltip
        public boolean displayRollValue = true;
        @ConfigEntry.Gui.Tooltip
        public boolean displayEffectRarity = false;
        @ConfigEntry.Gui.Tooltip
        public boolean displayUUID = false;
    }

    public static class Portal {
        public boolean enableIotaFix = true;
        public boolean enablePortalButtonIndicator = true;

        public boolean nodeSplit = true;
        public boolean soulsSplit = true;
        public boolean startBossSplit = true;
        public boolean phase1Split = false;
        public boolean phase2Split = false;
        public boolean phase3Split = false;
        public boolean bossSplit = true;
    }

    public enum ChatChannelType {
        LOCAL(new SystemChatChannel("l", "local", ChatFormatting.YELLOW)),
        WORLD_CHAT(new SystemChatChannel("wc", "world-chat", ChatFormatting.BLUE)),
        LFG(new SystemChatChannel("lfg", "looking-for-group", ChatFormatting.GOLD)),
        TR(new SystemChatChannel("tr", "trading", ChatFormatting.DARK_GREEN)),
        MAID(new SystemChatChannel("MAID", "Maids of Monumenta", style -> style.withColor(0xbe74fb))),
        GUILD_CHAT(new SystemChatChannel("gc", "guild-chat", ChatFormatting.GRAY)),
        CUSTOM(cfg -> new SystemChatChannel(cfg.name, cfg.prettyName, style -> style.withColor(cfg.color)));

        private final Function<ChatChannelEntry, SystemChatChannel> builder;

        ChatChannelType(Function<ChatChannelEntry, SystemChatChannel> builder) {
            this.builder = builder;
        }

        ChatChannelType(SystemChatChannel instance) {
            this.builder = x -> instance;
        }

        public SystemChatChannel build(ChatChannelEntry entry) {
            return builder.apply(entry);
        }
    }

    public static class ChatChannelEntry {
        public ChatChannelType type = ChatChannelType.CUSTOM;
        public String shorthandCommand = "";
        public String name = "";
        public String prettyName = "";
        @ConfigEntry.ColorPicker
        public int color = 0xffffff;

        public static ChatChannelEntry of(ChatChannelType type, String shorthand) {
            final var inst = new ChatChannelEntry();
            inst.type = type;
            inst.shorthandCommand = shorthand;
            return inst;
        }

        public List<AbstractConfigListEntry<?>> render(String key, GuiRegistryAccess access) {
            final var list = new ArrayList<AbstractConfigListEntry<?>>();
            final var builder = ConfigEntryBuilder.create();

            list.add(
                builder.startEnumSelector(Component.translatable(key + ".type"), ChatChannelType.class, type)
                    .setSaveConsumer(val -> this.type = val)
                    .build()
            );

            list.add(
                builder.startStrField(Component.translatable(key + ".shorthand"), shorthandCommand)
                    .setDefaultValue("") 
                    .setSaveConsumer(val -> this.shorthandCommand = val)
                    .build()
            );

            final Function<String, Optional<Component>> notEmpty = string -> {
                if (string.isEmpty()) {
                    return Optional.of(Component.translatable("error.fma.no_empty_string"));
                }
                return Optional.empty();
            };

            if (type == ChatChannelType.CUSTOM) {
                list.add(
                    builder.startStrField(Component.translatable(key + ".name"), name)
                        .setDefaultValue("")
                        .setErrorSupplier(notEmpty)
                        .setSaveConsumer(val -> this.name = val)
                        .build()
                );

                list.add(
                    builder.startStrField(Component.translatable(key + ".prettyName"), prettyName)
                        .setDefaultValue("")
                        .setErrorSupplier(notEmpty)
                        .setSaveConsumer(val -> this.prettyName = val)
                        .build()
                );

                list.add(
                    builder.startColorField(Component.translatable(key + ".color"), color)
                        .setDefaultValue(0xffffff)
                        .setSaveConsumer(val -> this.color = val)
                        .build()
                );
            }

            return list;
        }

        public SystemChatChannel build() {
            return type.build(this);
        }
    }

    public static class Chat {
        public String meowingChannel = "wc";
        public String meowingText = "meow";
        public String ggText = "gg";

        public List<ChatChannelEntry> channels = new ArrayList<>(List.of(
            ChatChannelEntry.of(ChatChannelType.LOCAL, "l"),
            ChatChannelEntry.of(ChatChannelType.WORLD_CHAT, "wc"),
            ChatChannelEntry.of(ChatChannelType.LFG, "lfg"),
            ChatChannelEntry.of(ChatChannelType.MAID, "gc")
        ));
    }

    @ConfigEntry.Category("features")
    @ConfigEntry.Gui.TransitiveObject
    public FeatureToggles features = new FeatureToggles();

    @ConfigEntry.Category("appearance")
    @ConfigEntry.Gui.TransitiveObject
    public Appearance appearance = new Appearance();

    @ConfigEntry.Category("hpIndicator")
    @ConfigEntry.Gui.TransitiveObject
    public HpIndicator hpIndicator = new HpIndicator();

    @ConfigEntry.Category("chat")
    @ConfigEntry.Gui.TransitiveObject
    public Chat chat = new Chat();

    @ConfigEntry.Category("strikes")
    @ConfigEntry.Gui.CollapsibleObject
    public Portal portal = new Portal();

    @ConfigEntry.Category("zenith")
    @ConfigEntry.Gui.TransitiveObject
    public Zenith zenith = new Zenith();

    @Override
    public void validatePostLoad() {
        if (hpIndicator.mediumHpPercent > hpIndicator.goodHpPercent)
            hpIndicator.mediumHpPercent = hpIndicator.goodHpPercent;

        if (hpIndicator.lowHpPercent > hpIndicator.mediumHpPercent)
            hpIndicator.lowHpPercent = hpIndicator.mediumHpPercent;

        final var seen = EnumSet.noneOf(ChatChannelType.class);

        chat.channels.removeIf(entry -> {
            if (entry.type == ChatChannelType.CUSTOM) {
                return entry.name.isEmpty() || entry.prettyName.isEmpty();
            }

            final var contains = seen.contains(entry.type);
            seen.add(entry.type);
            return contains;
        });

        final var seenShorthands = new HashSet<String>();
        chat.channels.forEach(entry -> {
            if (seenShorthands.contains(entry.shorthandCommand)) {
                entry.shorthandCommand = "";
            }
            seenShorthands.add(entry.shorthandCommand);
        });
    }

    public static ConfigHolder<FMAConfig> register() {
        final var holder = AutoConfig.register(FMAConfig.class, GsonConfigSerializer::new);
        final var registry = AutoConfig.getGuiRegistry(FMAConfig.class);

        registry.registerPredicateProvider((key, field, configObject, defaultsObject, guiRegistryAccess) -> {
            final var conf = new NestedListListEntry<ChatChannelEntry, AbstractConfigListEntry<ChatChannelEntry>>(
                Component.translatable(key), Utils.getUnsafely(field, configObject), true, null,
                (newValue) -> Utils.setUnsafely(field, configObject, newValue),
                () -> Utils.getUnsafely(field, defaultsObject),
                ConfigEntryBuilder.create().getResetButtonKey(), true, false,
                (elem, nestedListListEntry) -> {
                    if (elem == null) {
                        elem = new ChatChannelEntry();
                    }

                    return new MultiElementListEntry<>(
                        Component.translatable(key + ".entry"),
                        elem,
                        elem.render(key, guiRegistryAccess),
                        true
                    );
                }
            );

            conf.setErrorSupplier(() -> {
                final List<ChatChannelEntry> value = Utils.getUnsafely(field, defaultsObject);
                if (!value.stream().map(x -> x.shorthandCommand).filter(x -> !x.isEmpty()).allMatch(new HashSet<>()::add)) {
                    return Optional.of(Component.translatable("error.fma.duplicate_shorthand"));
                }

                return Optional.empty();
            });

            return List.of(conf);
        }, isListOfType(ChatChannelEntry.class));

        holder.registerSaveListener((configHolder, config) -> {
            config.validatePostLoad();
            FMAClient.reload();
            return InteractionResult.PASS;
        });

        return holder;
    }

    private static Predicate<Field> isListOfType(Class<?> types) {
        return (field) -> {
            if (List.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType) {
                Type[] args = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                return args.length == 1 && Stream.of(types).anyMatch((type) -> Objects.equals(args[0], type));
            } else {
                return false;
            }
        };
    }
}
