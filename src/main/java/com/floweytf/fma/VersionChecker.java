package com.floweytf.fma;

import com.floweytf.fma.events.ClientJoinServerEvent;
import static com.floweytf.fma.util.ChatUtil.send;
import static com.floweytf.fma.util.ChatUtil.sendWarn;
import com.floweytf.fma.util.FormatUtil;
import static com.floweytf.fma.util.FormatUtil.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import static net.minecraft.network.chat.Component.translatable;

public class VersionChecker {
    private static class VersionChangelogInfo {
        private final Version version;
        private final List<Component> info;

        private VersionChangelogInfo(String version, List<Component> info) {
            this.version = parseOrThrow(version);
            this.info = info;
        }

        public void addIfRelevant(Version currentVersion, Consumer<Component> target) {
            if (currentVersion.compareTo(version) < 0) {
                info.forEach(target);
            }
        }
    }

    private static final Version VER_DEFAULT = parseOrThrow("1.0.0");
    private static final List<VersionChangelogInfo> VERSION_INFO_TABLE = List.of(
        new VersionChangelogInfo("1.6.0", List.of(
            withHover(
                translatable("text.fma.version.1_6_0.update_checker"),
                translatable("text.fma.version.1_6_0.update_checker.hover")
            ),
            translatable("text.fma.version.1_6_0.chest_tracker")
        )),
        new VersionChangelogInfo("1.6.2", List.of(
            translatable("text.fma.version.1_6_2.situationals")
        ))
    );

    private static final String VERSION_URL =
        "https://raw.githubusercontent.com/Floweynt/flowey-monumenta-addons/refs/heads/master/versions.json";
    private final Version previousVersion;
    private final Version currentVersion;
    private final CompletableFuture<Version> latestVersion;

    public VersionChecker(FMAConfig config) {
        currentVersion = FMAClient.MOD.getMetadata().getVersion();
        previousVersion = getPreviousVersion(currentVersion);
        if (config.features.versionCheck) {
            latestVersion = makeVersionCheckRequestAsync();
        } else {
            latestVersion = null;
        }
    }

    private static Version parseOrThrow(String ver) {
        try {
            return SemanticVersion.parse(ver);
        } catch (VersionParsingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Version getPreviousVersion(Version currentVersion) {
        final var f = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(FMAClient.MOD.getMetadata().getId() + "_version.txt");

        Version res;

        try {
            res = parseOrThrow(Files.readString(f));
        } catch (IOException e) {
            res = VER_DEFAULT;
        }

        try {
            Files.writeString(f, currentVersion.getFriendlyString());
        } catch (IOException e) {
            FMAClient.LOGGER.error(e);
        }

        return res;
    }

    private static CompletableFuture<Version> makeVersionCheckRequestAsync() {
        return HttpClient.newBuilder().connectTimeout(Duration.of(10, ChronoUnit.SECONDS)).build()
            .sendAsync(HttpRequest.newBuilder()
                    .uri(URI.create(VERSION_URL))
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            )
            .thenApply(HttpResponse::body)
            .thenApply(s -> {
                final var versionStr = new Gson()
                    .fromJson(s, JsonElement.class)
                    .getAsJsonObject()
                    .get("latest")
                    .getAsString();

                try {
                    return Version.parse(versionStr);
                } catch (VersionParsingException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    public void registerEvent() {
        ClientJoinServerEvent.EVENT.register(() -> {
            if (latestVersion != null) {
                if (!latestVersion.isDone()) {
                    sendWarn(translatable("text.fma.version.common.update_check_timeout"));
                } else {
                    latestVersion.whenComplete((version, throwable) -> {
                        if (throwable != null) {
                            FMAClient.LOGGER.error(throwable);
                            sendWarn(translatable(
                                "text.fma.version.common.update_check_fail", throwable.getMessage())
                            );
                        }

                        if (version.compareTo(currentVersion) != 0) {
                            send(translatable(
                                "text.fma.version.common.new_version",
                                FormatUtil.altText(version.getFriendlyString())
                            ));
                        }
                    }).join();
                }
            }

            if (previousVersion.compareTo(currentVersion) != 0) {
                send(translatable(
                    "text.fma.version.common.updated",
                    withColor(currentVersion.getFriendlyString(), FMAClient.config().appearance.altTextColor)
                ));

                send(translatable("text.fma.version.common.header.0"));
                send(translatable("text.fma.version.common.header.1").withStyle(ChatFormatting.UNDERLINE));

                VERSION_INFO_TABLE.forEach(x -> x.addIfRelevant(previousVersion, s -> send(literal("- ").append(s))));
            }
        });
    }
}
