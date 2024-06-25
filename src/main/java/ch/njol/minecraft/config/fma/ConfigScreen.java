package ch.njol.minecraft.config.fma;

import ch.njol.minecraft.config.fma.annotations.Category;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.function.Supplier;

/**
 * This fake screen is a workaround for a bug when using both modmenu and cloth-config - cloth config expects a new
 * screen each time, but modmenu caches the screen.
 */
public class ConfigScreen<T extends Options> extends Screen {
    private final Screen parent;
    private final String translateRoot;
    private final Supplier<T> optionsSupplier;
    private final T defaultOptions;

    protected ConfigScreen(Screen parent, String translateRoot, Supplier<T> optionsSupplier, T defaultOptions) {
        super(Component.translatable(translateRoot + ".title"));
        this.parent = parent;
        this.translateRoot = translateRoot;
        this.optionsSupplier = optionsSupplier;
        this.defaultOptions = defaultOptions;
    }

    @Override
    protected void init() {
        ConfigBuilder config = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable(translateRoot + ".title"));

        T options = optionsSupplier.get();

        for (Field field : options.getClass().getDeclaredFields()) {
            Category categoryAnnotation = field.getAnnotation(Category.class);
            if (categoryAnnotation == null || !options.categoryVisible(categoryAnnotation.value())) {
                continue;
            }
            ConfigCategory category =
                config.getOrCreateCategory(Component.translatable(translateRoot + ".category." + categoryAnnotation.value()));
            category.addEntry(Config.buildConfigEntry(options, defaultOptions, field, translateRoot + ".option"));
        }

        config.setSavingRunnable(options::onUpdate);

        if (minecraft != null) {
            minecraft.setScreen(config.build());
        }
    }

    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }
}
