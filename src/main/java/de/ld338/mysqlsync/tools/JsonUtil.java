package de.ld338.mysqlsync.tools;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JsonUtil {

    public static String encodeAchievements(Player player) {
        Map<String, Boolean> achievements = new HashMap<>();

        Iterator<Advancement> advancements = Bukkit.advancementIterator();
        while (advancements.hasNext()) {
            Advancement advancement = advancements.next();
            AdvancementProgress progress = player.getAdvancementProgress(advancement);

            achievements.put(advancement.getKey().getKey(), progress.isDone());
        }

        return new Gson().toJson(achievements);
    }

    public static void decodeAchievements(Player player, String jsonAchievements) {
        Map<String, Boolean> achievements = new Gson().fromJson(jsonAchievements, HashMap.class);

        for (Map.Entry<String, Boolean> entry : achievements.entrySet()) {
            if (entry.getValue()) {
                Advancement advancement = Bukkit.getAdvancement(new NamespacedKey("minecraft", entry.getKey()));
                if (advancement != null) {
                    AdvancementProgress progress = player.getAdvancementProgress(advancement);
                    for (String criteria : progress.getRemainingCriteria()) {
                        progress.awardCriteria(criteria);
                    }
                }
            }
        }
    }

    public static String encodeStats(Player player) {
        Map<String, Object> stats = new HashMap<>();

        for (Statistic statistic : Statistic.values()) {
            if (statistic.isSubstatistic()) {
                Map<String, Integer> paramStats = new HashMap<>();
                if (statistic.isBlock()) {
                    for (Material material : Material.values()) {
                        if (material.isBlock()) {
                            int value = player.getStatistic(statistic, material);
                            paramStats.put(material.name(), value);
                        }
                    }
                } else if (statistic.getType() == Statistic.Type.ITEM) {
                    for (Material material : Material.values()) {
                        if (material.isItem()) {
                            int value = player.getStatistic(statistic, material);
                            paramStats.put(material.name(), value);
                        }
                    }
                } else if (statistic.getType() == Statistic.Type.ENTITY) {
                    for (EntityType entityType : EntityType.values()) {
                        if (entityType.isSpawnable()) {
                            int value = player.getStatistic(statistic, entityType);
                            paramStats.put(entityType.name(), value);
                        }
                    }
                }
                if (!paramStats.isEmpty()) {
                    stats.put(statistic.name(), paramStats);
                }
            } else {
                stats.put(statistic.name(), player.getStatistic(statistic));
            }
        }

        return new Gson().toJson(stats);
    }

    public static void decodeStats(Player player, String jsonStats) {
        Map<String, Object> stats = new Gson().fromJson(jsonStats, HashMap.class);

        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            try {
                Statistic statistic = Statistic.valueOf(entry.getKey());

                if (entry.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> paramStats = (Map<String, Object>) entry.getValue();

                    if (statistic.isBlock()) {
                        for (Map.Entry<String, Object> paramEntry : paramStats.entrySet()) {
                            Material material = Material.valueOf(paramEntry.getKey());
                            int value = ((Double) paramEntry.getValue()).intValue();
                            player.setStatistic(statistic, material, value);
                        }
                    } else if (statistic.getType() == Statistic.Type.ITEM) {
                        for (Map.Entry<String, Object> paramEntry : paramStats.entrySet()) {
                            Material material = Material.valueOf(paramEntry.getKey());
                            int value = ((Double) paramEntry.getValue()).intValue();
                            player.setStatistic(statistic, material, value);
                        }
                    } else if (statistic.getType() == Statistic.Type.ENTITY) {
                        for (Map.Entry<String, Object> paramEntry : paramStats.entrySet()) {
                            EntityType entityType = EntityType.valueOf(paramEntry.getKey());
                            int value = ((Double) paramEntry.getValue()).intValue();
                            player.setStatistic(statistic, entityType, value);
                        }
                    }
                } else if (entry.getValue() instanceof Double) {
                    player.setStatistic(statistic, ((Double) entry.getValue()).intValue());
                }
            } catch (IllegalArgumentException | ClassCastException e) {
                System.err.println("Error processing statistic: " + entry.getKey() + " - " + e.getMessage());
            }
        }
    }

    public static String encodeEffects(Player player) {
        Collection<PotionEffect> effects = player.getActivePotionEffects();
        Map<String, Integer[]> effectMap = new HashMap<>();

        for (PotionEffect effect : effects) {
            effectMap.put(effect.getType().getName(), new Integer[]{effect.getDuration(), effect.getAmplifier()});
        }

        return new Gson().toJson(effectMap);
    }

    public static void decodeEffects(Player player, String jsonEffects) {
        Type mapType = new TypeToken<Map<String, Integer[]>>() {}.getType();
        Map<String, Integer[]> effectMap = new Gson().fromJson(jsonEffects, mapType);

        for (Map.Entry<String, Integer[]> entry : effectMap.entrySet()) {
            PotionEffectType effectType = PotionEffectType.getByName(entry.getKey());
            if (effectType != null) {
                int duration = entry.getValue()[0];
                int amplifier = entry.getValue()[1];
                player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
            }
        }
    }

}
