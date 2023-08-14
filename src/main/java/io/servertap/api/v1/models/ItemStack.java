package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemStack {

    @Expose
    private Integer count = null;

    @Expose
    private Integer slot = null;

    @Expose
    private String id = null;

    @Expose
    private String displayName = null;

    @Expose
    private List<String> lore = null;

    @Expose
    private Map<String, Integer> enchants = new HashMap<>();

    @Expose
    private int customModelData = 0;

    public ItemStack slot(Integer slot) {
        this.slot = slot;
        return this;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }

    public Integer getSlot() {
        return this.slot;
    }

    public ItemStack id(String id) {
        this.id = id;
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public ItemStack count(Integer count) {
        this.count = count;
        return this;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getCount() {
        return this.count;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public Map<String, Integer> getEnchants() {
        return enchants;
    }

    public void setEnchants(Map<Enchantment, Integer> enchants) {
        enchants.forEach((enchantment, i) -> this.enchants.put(enchantment.getKey().toString(), i));
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    public static ItemStack fromBukkitItemStack(org.bukkit.inventory.ItemStack iS) {
        ItemStack itemStack = new ItemStack();
        ItemMeta itemMeta = iS.getItemMeta();
        itemStack.setId("minecraft:" + iS.getType().toString().toLowerCase());
        itemStack.setCount(iS.getAmount());
        itemStack.setDisplayName(itemMeta.getDisplayName());
        itemStack.setLore(itemMeta.getLore());
        itemStack.setEnchants(itemMeta.getEnchants());
        if(itemMeta.hasCustomModelData())
            itemStack.setCustomModelData(itemMeta.getCustomModelData());

        return itemStack;
    }
}