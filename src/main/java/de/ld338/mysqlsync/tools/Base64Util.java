package de.ld338.mysqlsync.tools;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class Base64Util {

    public static String encodeInventory(Player player) throws Exception {
        PlayerInventory inventory = player.getInventory();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(inventory.getSize());

            for (ItemStack item : inventory.getContents()) {
                dataOutput.writeObject(item);
            }

            return java.util.Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
    }

    public static void decodeInventory(Player player, String base64) throws Exception {
        player.getInventory().clear();
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            PlayerInventory inventory = player.getInventory();
            int size = dataInput.readInt();
            ItemStack[] items = new ItemStack[size];

            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            inventory.setContents(items);
        }
    }

    public static void decodeEnderChest(Player player, String base64) throws Exception {
        player.getEnderChest().clear();
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            Inventory enderChest = player.getEnderChest();
            int size = dataInput.readInt();
            ItemStack[] items = new ItemStack[size];

            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            enderChest.setContents(items);
        }
    }

    public static String encodeEnderChest(Player player) throws Exception {
        Inventory enderChest = player.getEnderChest();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(enderChest.getSize());

            for (ItemStack item : enderChest.getContents()) {
                dataOutput.writeObject(item);
            }

            return java.util.Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
    }

    public static void decodeArmor(Player player, String inv) {
        player.getInventory().setArmorContents(null);
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(inv);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            PlayerInventory inventory = player.getInventory();
            int size = dataInput.readInt();
            ItemStack[] items = new ItemStack[size];

            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            inventory.setArmorContents(items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String encodeArmor(Player player) {
        PlayerInventory inventory = player.getInventory();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(inventory.getArmorContents().length);

            for (ItemStack item : inventory.getArmorContents()) {
                dataOutput.writeObject(item);
            }

            return java.util.Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
