package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;

public class Block {
    @Expose
    private boolean isPowered;

    @Expose
    private int blockPower;

    @Expose
    private boolean isEmpty;

    @Expose
    private boolean isLiquid;

    @Expose
    private double temperature;

    @Expose
    private double humidity;

    @Expose
    private String blockType;

    @Expose
    private byte lightLevel;

    @Expose
    private String biome;


    public boolean isIsPowered() {
        return this.isPowered;
    }

    public boolean getIsPowered() {
        return this.isPowered;
    }

    public void setIsPowered(boolean isPowered) {
        this.isPowered = isPowered;
    }

    public int getBlockPower() {
        return this.blockPower;
    }

    public void setBlockPower(int blockPower) {
        this.blockPower = blockPower;
    }

    public boolean isEmpty() {
        return this.isEmpty;
    }

    public boolean getIsEmpty() {
        return this.isEmpty;
    }

    public void setIsEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

    public boolean isLiquid() {
        return this.isLiquid;
    }

    public boolean getIsLiquid() {
        return this.isLiquid;
    }

    public void setIsLiquid(boolean isLiquid) {
        this.isLiquid = isLiquid;
    }

    public double getTemperature() {
        return this.temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return this.humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public String getBlockType() {
        return this.blockType;
    }

    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

    public byte getLightLevel() {
        return this.lightLevel;
    }

    public void setLightLevel(byte lightLevel) {
        this.lightLevel = lightLevel;
    }

    public String getBiome() {
        return this.biome;
    }

    public void setBiome(String biome) {
        this.biome = biome;
    }


}
