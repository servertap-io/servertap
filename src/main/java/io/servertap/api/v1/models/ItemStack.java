package io.servertap.api.v1.models;

import java.util.Map;
import com.google.gson.annotations.Expose;

public class ItemStack {
    
    @Expose
    private Integer count = null;

    @Expose
    private Integer slot = null;

    @Expose
    private String id = null;

    @Expose
    private Map<String,Object> itemMeta = null;
    
    public ItemStack slot(Integer slot){
        this.slot = slot;
        return this;
    }

    public void setSlot(Integer slot){
        this.slot = slot;
    }

    public Integer getSlot(){
        return this.slot;
    }

    public ItemStack id(String id){
        this.id = id;
        return this;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return this.id;
    }

    public void setItemMeta(Map<String,Object> itemMeta){
        this.itemMeta = itemMeta;
    }


    public ItemStack count(Integer count){
        this.count = count;
        return this;
    }

    public void setCount(Integer count){
        this.count = count;
    }

    public Integer getCount(){
        return this.count;
    }
}
