package team.creative.creativecore.common.util.ingredient;

import static team.creative.creativecore.CreativeCore.LOGGER;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.converation.ConfigTypeConveration;
import team.creative.creativecore.common.config.gui.GuiInfoStackButton;
import team.creative.creativecore.common.config.gui.IGuiConfigParent;
import team.creative.creativecore.common.config.key.ConfigKey;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.util.registry.NamedTypeRegistry;

public abstract class CreativeIngredient {
    
    public static final NamedTypeRegistry<CreativeIngredient> REGISTRY = new NamedTypeRegistry<CreativeIngredient>().addConstructorPattern();
    private static final List<Function<Object, ? extends CreativeIngredient>> objectParsers = new ArrayList<>();
    private static final CreativeIngredient EMPTY = new CreativeIngredient() {
        
        @Override
        protected void saveExtra(HolderLookup.Provider provider, CompoundTag nbt) {}
        
        @Override
        protected void loadExtra(HolderLookup.Provider provider, CompoundTag nbt) {}
        
        @Override
        public boolean is(CreativeIngredient info) {
            return false;
        }
        
        @Override
        public boolean is(ItemStack stack) {
            return false;
        }
        
        @Override
        public ItemStack getExample() {
            return ItemStack.EMPTY;
        }
        
        @Override
        public boolean equals(CreativeIngredient object) {
            return false;
        }
        
        @Override
        public Component descriptionDetail() {
            return Component.literal("empty");
        }
        
        @Override
        public Component description() {
            return Component.literal("invalid");
        }
        
        @Override
        public CreativeIngredient copy() {
            return EMPTY;
        }
    };
    
    public static <T extends CreativeIngredient> void registerType(String id, Class<T> classType, Function<Object, T> parser) {
        REGISTRY.register(id, classType);
        if (parser != null)
            objectParsers.add(parser);
    }
    
    public static CreativeIngredient parse(Object object) {
        if (object == null)
            return null;
        if (object instanceof CreativeIngredient)
            return (CreativeIngredient) object;
        
        for (int i = 0; i < objectParsers.size(); i++)
            try {
                CreativeIngredient ingredient = objectParsers.get(i).apply(object);
                if (ingredient != null)
                    return ingredient;
            } catch (Exception e) {}
        
        return null;
    }
    
    public static CreativeIngredient load(HolderLookup.Provider provider, CompoundTag nbt) {
        Class<? extends CreativeIngredient> classType = REGISTRY.get(nbt.getString("id"));
        if (classType == null) {
            LOGGER.error(new IllegalArgumentException("'" + nbt.getString("id") + "' is an invalid type"));
            return EMPTY;
        }
        
        try {
            CreativeIngredient ingredient = classType.getConstructor().newInstance();
            ingredient.loadExtra(provider, nbt);
            return ingredient;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
    static {
        // Load default types
        registerType("block", CreativeIngredientBlock.class, (x) -> {
            Block block = null;
            if (x instanceof Block)
                block = (Block) x;
            if (x instanceof BlockItem)
                block = Block.byItem((Item) x);
            
            if (block != null && !(block instanceof AirBlock))
                return new CreativeIngredientBlock(block);
            return null;
        });
        registerType("blocktag", CreativeIngredientBlockTag.class, (x) -> {
            if (x instanceof TagKey key && key.isFor(Registries.BLOCK))
                return new CreativeIngredientBlockTag((TagKey<Block>) x);
            return null;
        });
        
        registerType("item", CreativeIngredientItem.class, (x) -> {
            if (x instanceof Item && !(x instanceof BlockItem))
                return new CreativeIngredientItem((Item) x);
            return null;
        });
        registerType("itemtag", CreativeIngredientItemTag.class, (x) -> {
            if (x instanceof TagKey key && key.isFor(Registries.ITEM))
                return new CreativeIngredientItemTag((TagKey<Item>) x);
            return null;
        });
        
        registerType("itemstack", CreativeIngredientItemStack.class, (x) -> x instanceof ItemStack ? new CreativeIngredientItemStack((ItemStack) x, false) : null);
        registerType("fuel", CreativeIngredientFuel.class, null);
        
        final CreativeIngredient temp = new CreativeIngredientBlock(Blocks.DIRT);
        
        ConfigTypeConveration.registerSpecialType(CreativeIngredient.class::isAssignableFrom, new ConfigTypeConveration<CreativeIngredient>() {
            
            @Override
            public CreativeIngredient readElement(HolderLookup.Provider provider, CreativeIngredient defaultValue, boolean loadDefault, boolean ignoreRestart, JsonElement element, Side side, ConfigKey key) {
                if (element.isJsonPrimitive() && ((JsonPrimitive) element).isString())
                    try {
                        return CreativeIngredient.load(provider, TagParser.parseTag(element.getAsString()));
                    } catch (CommandSyntaxException e) {
                        LOGGER.error(e);
                    }
                return defaultValue;
            }
            
            @Override
            public JsonElement writeElement(HolderLookup.Provider provider, CreativeIngredient value, boolean saveDefault, boolean ignoreRestart, Side side, ConfigKey key) {
                return new JsonPrimitive(value.save(provider).toString());
            }
            
            @Override
            @Environment(EnvType.CLIENT)
            @OnlyIn(Dist.CLIENT)
            public void createControls(GuiParent parent, IGuiConfigParent configParent, ConfigKey key, Side side) {
                parent.add(new GuiInfoStackButton("data", temp).setExpandableX());
            }
            
            @Override
            @Environment(EnvType.CLIENT)
            @OnlyIn(Dist.CLIENT)
            public void loadValue(CreativeIngredient value, CreativeIngredient defaultValue, GuiParent parent, IGuiConfigParent configParent, ConfigKey key, Side side) {
                GuiInfoStackButton button = parent.get("data");
                button.set(value);
            }
            
            @Override
            @Environment(EnvType.CLIENT)
            @OnlyIn(Dist.CLIENT)
            protected CreativeIngredient saveValue(GuiParent parent, IGuiConfigParent configParent, ConfigKey key, Side side) {
                GuiInfoStackButton button = parent.get("data");
                return button.get();
            }
            
            @Override
            public CreativeIngredient set(ConfigKey key, CreativeIngredient value) {
                return value;
            }
        });
    }
    
    public CreativeIngredient() {
        
    }
    
    public CompoundTag save(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", REGISTRY.getIdOrDefault(this, "empty"));
        saveExtra(provider, nbt);
        return nbt;
    }
    
    protected abstract void saveExtra(HolderLookup.Provider provider, CompoundTag nbt);
    
    protected abstract void loadExtra(HolderLookup.Provider provider, CompoundTag nbt);
    
    public abstract boolean is(ItemStack stack);
    
    public abstract boolean is(CreativeIngredient info);
    
    public abstract ItemStack getExample();
    
    public abstract CreativeIngredient copy();
    
    @Override
    public boolean equals(Object object) {
        return object instanceof CreativeIngredient && equals((CreativeIngredient) object);
    }
    
    public abstract boolean equals(CreativeIngredient object);
    
    public abstract Component description();
    
    public abstract Component descriptionDetail();
    
}
