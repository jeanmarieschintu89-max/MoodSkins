package fr.moodcraft.moodskins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MoodSkinsPlugin extends JavaPlugin implements Listener, TabExecutor {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final Map<String, CachedSkin> cache = new ConcurrentHashMap<>();

    private String bedrockPrefix;
    private String apiBase;
    private int applyDelayTicks;
    private int cacheMinutes;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        Bukkit.getPluginManager().registerEvents(this, this);

        if (getCommand("moodskins") != null) {
            getCommand("moodskins").setExecutor(this);
            getCommand("moodskins").setTabCompleter(this);
        }

        getLogger().info("MoodSkins serveur active.");
    }

    private void loadSettings() {
        bedrockPrefix = getConfig().getString("bedrock-prefix", ".");
        apiBase = trimSlash(getConfig().getString("geyser-api-base", "https://api.geysermc.org/v2"));
        applyDelayTicks = Math.max(1, getConfig().getInt("apply-delay-ticks", 60));
        cacheMinutes = Math.max(1, getConfig().getInt("cache-minutes", 1440));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!isBedrock(player.getName())) return;
        Bukkit.getScheduler().runTaskLater(this, () -> applySkin(player.getName(), null), applyDelayTicks);
    }

    private boolean isBedrock(String name) {
        return name != null && name.startsWith(bedrockPrefix);
    }

    private String cleanName(String name) {
        return isBedrock(name) ? name.substring(bedrockPrefix.length()) : name;
    }

    private void applySkin(String playerName, CommandSender feedback) {
        if (!isBedrock(playerName)) {
            send(feedback, "Ce joueur ne semble pas Bedrock : " + playerName);
            return;
        }

        fetchTextureUrl(playerName).thenAccept(texture -> {
            if (texture.isEmpty()) {
                send(feedback, msg("messages.no-texture", "Texture introuvable pour {player}.").replace("{player}", playerName));
                return;
            }

            Bukkit.getScheduler().runTask(this, () -> {
                runApplyCommands(playerName, texture.get());
                send(feedback, msg("messages.applied", "Skin applique pour {player}.").replace("{player}", playerName));
            });
        }).exceptionally(error -> {
            getLogger().warning("Erreur MoodSkins : " + error.getMessage());
            send(feedback, "Erreur pendant la recuperation du skin.");
            return null;
        });
    }

    private CompletableFuture<Optional<String>> fetchTextureUrl(String playerName) {
        String gamertag = cleanName(playerName);
        String key = gamertag.toLowerCase(Locale.ROOT);
        CachedSkin cached = cache.get(key);

        if (cached != null && !cached.expired()) {
            return CompletableFuture.completedFuture(Optional.ofNullable(cached.textureUrl));
        }

        return fetchXuid(gamertag).thenCompose(xuid -> {
            if (xuid.isEmpty()) return CompletableFuture.completedFuture(Optional.empty());
            return fetchSkinTexture(xuid.get());
        }).thenApply(texture -> {
            cache.put(key, new CachedSkin(texture.orElse(null), System.currentTimeMillis() + cacheMinutes * 60_000L));
            return texture;
        });
    }

    private CompletableFuture<Optional<String>> fetchXuid(String gamertag) {
        String encoded = URLEncoder.encode(gamertag, StandardCharsets.UTF_8);
        return get(apiBase + "/xbox/xuid/" + encoded).thenApply(body -> {
            if (body.isEmpty()) return Optional.empty();
            String text = body.get().trim();
            Optional<String> fromJson = findJsonValue(text, "xuid");
            if (fromJson.isPresent()) return fromJson;
            fromJson = findJsonValue(text, "id");
            if (fromJson.isPresent()) return fromJson;
            return Optional.of(text.replace("\"", "").trim());
        });
    }

    private CompletableFuture<Optional<String>> fetchSkinTexture(String xuid) {
        String encoded = URLEncoder.encode(xuid, StandardCharsets.UTF_8);
        return get(apiBase + "/skin/" + encoded).thenApply(body -> {
            if (body.isEmpty()) return Optional.empty();
            String text = body.get();

            for (String key : List.of("texture_id", "textureId", "hash")) {
                Optional<String> value = findJsonValue(text, key);
                if (value.isPresent() && !value.get().isBlank()) {
                    return Optional.of("https://textures.minecraft.net/texture/" + value.get());
                }
            }

            for (String key : List.of("texture_url", "textureUrl", "url")) {
                Optional<String> value = findJsonValue(text, key);
                if (value.isPresent() && !value.get().isBlank()) return value;
            }

            return Optional.empty();
        });
    }

    private CompletableFuture<Optional<String>> get(String url) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).GET().build();
        return http.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() < 200 || response.statusCode() >= 300) return Optional.empty();
            return Optional.ofNullable(response.body());
        });
    }

    private Optional<String> findJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private void runApplyCommands(String playerName, String textureUrl) {
        List<String> commands = getConfig().getStringList("commands.apply");
        if (commands.isEmpty()) commands = List.of("skinsrestorer set {player} {texture_url}");

        for (String command : commands) {
            String parsed = command.replace("{player}", playerName).replace("{texture_url}", textureUrl).replace("{texture}", textureUrl);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            send(sender, "/moodskins reload | debug <joueur> | apply <joueur>");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                reloadConfig();
                loadSettings();
                cache.clear();
                send(sender, msg("messages.reloaded", "Configuration rechargee."));
                return true;
            }
            case "debug" -> {
                if (args.length < 2) {
                    send(sender, "/moodskins debug <joueur>");
                    return true;
                }
                send(sender, "Recherche du skin pour " + args[1] + "...");
                fetchTextureUrl(args[1]).thenAccept(texture -> send(sender, "Texture : " + texture.orElse("introuvable")));
                return true;
            }
            case "apply" -> {
                if (args.length < 2) {
                    send(sender, "/moodskins apply <joueur>");
                    return true;
                }
                applySkin(args[1], sender);
                return true;
            }
            default -> {
                send(sender, "/moodskins reload | debug <joueur> | apply <joueur>");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("reload", "debug", "apply");
        if (args.length == 2 && (args[0].equalsIgnoreCase("debug") || args[0].equalsIgnoreCase("apply"))) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) names.add(player.getName());
            return names;
        }
        return Collections.emptyList();
    }

    private String msg(String path, String fallback) {
        return getConfig().getString(path, fallback);
    }

    private void send(CommandSender sender, String message) {
        if (sender == null) return;
        String prefix = msg("messages.prefix", "&cMoodSkins &8» &f");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    private String trimSlash(String value) {
        if (value == null || value.isBlank()) return "https://api.geysermc.org/v2";
        while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
        return value;
    }

    private static final class CachedSkin {
        private final String textureUrl;
        private final long expiresAt;

        private CachedSkin(String textureUrl, long expiresAt) {
            this.textureUrl = textureUrl;
            this.expiresAt = expiresAt;
        }

        private boolean expired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
