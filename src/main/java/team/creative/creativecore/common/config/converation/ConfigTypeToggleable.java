package team.creative.creativecore.common.config.converation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.gui.IGuiConfigParent;
import team.creative.creativecore.common.config.key.ConfigKey;
import team.creative.creativecore.common.config.premade.ToggleableConfig;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.flow.GuiFlow;

public class ConfigTypeToggleable extends ConfigTypeConveration<ToggleableConfig> {
    
    @Override
    public ToggleableConfig readElement(HolderLookup.Provider provider, ToggleableConfig defaultValue, boolean loadDefault, boolean ignoreRestart, JsonElement element, Side side, ConfigKey key) {
        ConfigKey configKey = ConfigKey.ofGenericType(key, side);
        
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            configKey.read(provider, true, ignoreRestart, object.get("content"), side);
            return new ToggleableConfig(configKey.copy(provider, side), object.get("enabled").getAsBoolean());
        }
        configKey.forceValue(defaultValue.value, side);
        return new ToggleableConfig(configKey.copy(provider, side), defaultValue.isEnabled());
    }
    
    @Override
    public JsonElement writeElement(HolderLookup.Provider provider, ToggleableConfig value, boolean saveDefault, boolean ignoreRestart, Side side, ConfigKey key) {
        ConfigKey configKey = ConfigKey.ofGenericType(key, side);
        JsonObject object = new JsonObject();
        object.addProperty("enabled", value.isEnabled());
        configKey.forceValue(value.value, side);
        object.add("content", configKey.write(provider, true, ignoreRestart, side));
        return object;
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    public void createControls(GuiParent parent, IGuiConfigParent configParent, ConfigKey key, Side side) {
        parent.flow = GuiFlow.STACK_Y;
        parent.add(new GuiCheckBox("enabled", true).setTranslate("gui.config.enabled"));
        ConfigKey configKey = ConfigKey.ofGenericType(key, side);
        parent.add(configKey.create(configParent, "content", side));
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    public void loadValue(ToggleableConfig value, ToggleableConfig defaultValue, GuiParent parent, IGuiConfigParent configParent, ConfigKey key, Side side) {
        parent.get("enabled", GuiCheckBox.class).value = value.isEnabled();
        
        ConfigKey configKey = ConfigKey.ofGenericType(key, side);
        configKey.forceValue(value.value, side);
        configKey.load(configParent, parent.get("content"), side);
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    protected ToggleableConfig saveValue(GuiParent parent, IGuiConfigParent configParent, ConfigKey key, Side side) {
        ConfigKey configKey = ConfigKey.ofGenericType(key, side);
        configKey.save(parent.get("content"), configParent, side);
        return new ToggleableConfig(configKey.copy(configParent.provider(), side), parent.get("enabled", GuiCheckBox.class).value);
    }
    
    @Override
    public ToggleableConfig set(ConfigKey key, ToggleableConfig value) {
        return value;
    }
    
    @Override
    public boolean areEqual(ToggleableConfig one, ToggleableConfig two, ConfigKey key, Side side) {
        if (one.isEnabled() != two.isEnabled())
            return false;
        
        ConfigKey configKey = ConfigKey.ofGenericType(key, side);
        ConfigTypeConveration converation = configKey.converation();
        
        if (converation != null && !converation.areEqual(one.value, two.value, configKey, side))
            return false;
        
        return converation != null || one.value.equals(two.value);
    }
}
