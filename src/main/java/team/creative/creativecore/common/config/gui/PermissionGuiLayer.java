package team.creative.creativecore.common.config.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.converation.ConfigTypeConveration;
import team.creative.creativecore.common.config.converation.ConfigTypePermission.GuiPermissionConfigButton;
import team.creative.creativecore.common.config.holder.ConfigHolderObject;
import team.creative.creativecore.common.config.key.ConfigKey;
import team.creative.creativecore.common.config.key.ConfigKeyType;
import team.creative.creativecore.common.config.premade.Permission;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.parent.GuiColumn;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.parent.GuiRow;
import team.creative.creativecore.common.gui.controls.parent.GuiTableScrollable;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.dialog.DialogGuiLayer.DialogButton;
import team.creative.creativecore.common.gui.dialog.GuiDialogHandler;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRules;
import team.creative.creativecore.common.util.text.TextBuilder;

public class PermissionGuiLayer extends GuiLayer {
    
    public GuiPermissionConfigButton button;
    protected boolean force;
    protected GuiTableScrollable table;
    protected ConfigTypeConveration converation;
    private List<PermissionGuiGroup> groups;
    private final HashMap<ConfigKeyType, GuiRow> rows = new HashMap<>();
    
    public PermissionGuiLayer() {
        super("permission", 500, 260);
    }
    
    @Override
    public void create() {
        if (button == null)
            return;
        
        flow = GuiFlow.STACK_Y;
        align = Align.STRETCH;
        converation = button.key.converation();
        groups = new ArrayList<>();
        
        GuiParent top = new GuiParent();
        add(top);
        top.add(new GuiButton("add", x -> {
            
            PermissionGuiGroup group;
            if (converation != null) {
                GuiRow row = table.contentRows().iterator().next();
                
                group = new PermissionGuiGroupSimple("", null);
                groups.add(group);
                
                GuiColumn col = new GuiColumn();
                ((PermissionGuiGroupSimple) group).control = col;
                
                converation.createControls(col, button.configParent, button.key, button.side);
                
                row.addColumn(col);
            } else {
                Object newValue = ConfigTypeConveration.createObject(button.key.field());
                ConfigHolderObject holder = ConfigHolderObject.createUnrelated(button.side, newValue, newValue);
                group = new PermissionGuiGroupMulti(provider(), "", holder);
                groups.add(group);
                
                for (Entry<ConfigKeyType, GuiRow> entry : rows.entrySet()) {
                    
                    GuiColumn col = new GuiColumn();
                    GuiConfigControl config = new GuiConfigControl(entry.getKey(), button.side, 100, false) {
                        
                        @Override
                        public void changed() {
                            group.updateResetButton();
                        }
                    };
                    col.add(config);
                    ((PermissionGuiGroupMulti) group).controls.add(config);
                    config.init(null);
                    
                    entry.getValue().addColumn(col);
                }
            }
            
            addGroupHeader(false, group, "");
            
            reflow();
        }).setTranslate("gui.perm.add"));
        
        table = new GuiTableScrollable();
        add(table);
        GuiRow topRow = table.getTopRow();
        topRow.addColumn(new GuiColumn());
        if (converation != null) {
            GuiRow row = new GuiRow();
            
            String caption = translateOrDefault("gui.content", "content");
            GuiColumn labelCol = new GuiColumn();
            labelCol.setVAlign(VAlign.CENTER).add(new GuiLabel("label").setTitle(Component.literal(caption + ":")));
            row.addColumn(labelCol);
            
            table.addRow(row);
            
            for (Entry<String, ?> entry : button.value.entrySet()) {
                var defaultValue = button.defaultValue.getDirect(entry.getKey());
                PermissionGuiGroupSimple group = new PermissionGuiGroupSimple(entry.getKey(), defaultValue);
                groups.add(group);
                
                GuiColumn col = new GuiColumn();
                group.control = col;
                
                converation.createControls(col, button.configParent, button.key, button.side);
                converation.loadValue(entry.getValue(), defaultValue, col, button.configParent, button.key, button.side);
                
                row.addColumn(col);
            }
        } else {
            List<GuiRow> rows = new ArrayList<>();
            
            for (Entry<String, ?> entry : button.value.entrySet()) {
                button.key.forceValue(entry.getValue(), button.side);
                Object copiedEntry = button.key.copy(provider(), button.side);
                Object defaultReference = button.defaultValue.getDirect(entry.getKey());
                if (defaultReference == null)
                    defaultReference = copiedEntry;
                ConfigHolderObject holder = ConfigHolderObject.createUnrelated(button.side, copiedEntry, defaultReference);
                PermissionGuiGroupMulti group = new PermissionGuiGroupMulti(provider(), entry.getKey(), holder);
                groups.add(group);
                
                int i = 0;
                for (ConfigKey key : holder.fields()) {
                    if (key.requiresRestart || key.isFolder())
                        continue;
                    
                    if (rows.size() <= i) {
                        GuiRow row = new GuiRow();
                        String path = "config." + String.join(".", holder.path());
                        if (!path.endsWith("."))
                            path += ".";
                        String caption = translateOrDefault(path + key.name + ".name", key.name);
                        String comment = path + key.name + ".comment";
                        GuiColumn col = new GuiColumn();
                        col.setVAlign(VAlign.CENTER).add(new GuiLabel("label" + i).setTitle(Component.literal(caption + ":")).setTooltip(new TextBuilder().translateIfCan(comment)
                                .build()));
                        row.addColumn(col);
                        table.addRow(row);
                        rows.add(row);
                        this.rows.put((ConfigKeyType) key, row);
                    }
                    
                    GuiColumn col = new GuiColumn();
                    GuiConfigControl config = new GuiConfigControl((ConfigKeyType) key, Side.SERVER, 100, false) {
                        
                        @Override
                        public void changed() {
                            group.updateResetButton();
                        }
                    };
                    col.add(config);
                    group.controls.add(config);
                    config.init(null);
                    
                    rows.get(i).addColumn(col);
                    i++;
                }
            }
        }
        
        int i = 0;
        boolean defaultCol = true;
        for (String name : button.value.keySet()) {
            addGroupHeader(defaultCol, groups.get(i), name);
            defaultCol = false;
            i++;
        }
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom.setUnexpandableX());
        bottom.addLeft(new GuiButton("cancel", x -> closeTopLayer()).setTranslate("gui.cancel"));
        bottom.addRight(new GuiButton("save", x -> {
            button.setNewValue(save());
            force = true;
            closeTopLayer();
        }).setTranslate("gui.save"));
        
        for (PermissionGuiGroup group : groups)
            group.updateResetButton();
    }
    
    protected void addGroupHeader(boolean defaultCol, PermissionGuiGroup group, String name) {
        GuiColumn col = new GuiColumn();
        col.setVAlign(VAlign.CENTER).setDim(new GuiSizeRules().maxWidth(100));
        if (defaultCol)
            col.add(new GuiLabel("name").setTitle(Component.literal(name)));
        else
            col.add(group.textfield = new GuiTextfield("name", name).setDim(100));
        
        if (!name.isEmpty() && button.defaultValue.containsKey(name))
            col.add(group.resetButton = (GuiButton) new GuiButton("r", x -> group.reset()).setTranslate("gui.reset").setAlign(Align.CENTER));
        
        if (!defaultCol)
            col.add(new GuiButton("x", x -> {
                int index = groups.indexOf(group);
                groups.remove(index);
                table.removeContentCol(index);
                reflow();
            }).setTranslate("gui.del").setAlign(Align.CENTER));
        
        table.getTopRow().addColumn(col);
    }
    
    @Override
    public void closeTopLayer() {
        Permission newValue = save();
        if (force || button.configTypePerm.areEqual(button.value, newValue, button.key, button.side))
            super.closeTopLayer();
        else
            GuiDialogHandler.openDialog(getIntegratedParent(), "savechanges", (x, y) -> {
                if (y == DialogButton.YES)
                    button.setNewValue(newValue);
                else if (y != DialogButton.CANCEL) {
                    force = true;
                    closeTopLayer();
                }
            }, DialogButton.YES, DialogButton.NO, DialogButton.CANCEL);
    }
    
    public Permission save() {
        Permission perm = new Permission<>(groups.get(0).save());
        for (int i = 1; i < groups.size(); i++)
            perm.add(groups.get(i).getTitle(), groups.get(i).save());
        return perm;
    }
    
    public static abstract class PermissionGuiGroup {
        
        public final String originalGroup;
        public GuiTextfield textfield;
        public GuiButton resetButton;
        
        public PermissionGuiGroup(String group) {
            this.originalGroup = group;
        }
        
        public String getTitle() {
            if (isDefaultGroup())
                return "default";
            return textfield.getText();
        }
        
        public void updateResetButton() {
            if (resetButton != null)
                resetButton.enabled = !isDefault();
        }
        
        public abstract Object save();
        
        public void reset() {
            if (!isDefaultGroup())
                textfield.setText(originalGroup);
            resetInternal();
            updateResetButton();
        }
        
        protected abstract void resetInternal();
        
        public abstract boolean isDefault();
        
        public boolean isDefaultGroup() {
            return textfield == null;
        }
        
    }
    
    public class PermissionGuiGroupSimple extends PermissionGuiGroup {
        
        public GuiParent control;
        public final Object defaultValue;
        
        public PermissionGuiGroupSimple(String group, Object defaultValue) {
            super(group);
            this.defaultValue = defaultValue;
        }
        
        @Override
        protected void resetInternal() {
            converation.loadValue(defaultValue, defaultValue, control, button.configParent, null, button.side);
        }
        
        @Override
        public boolean isDefault() {
            if (defaultValue == null)
                return false;
            return converation.areEqual(defaultValue, save(), null, button.side);
        }
        
        @Override
        public Object save() {
            return converation.save(control, button.configParent, button.key, button.side);
        }
        
    }
    
    public static class PermissionGuiGroupMulti extends PermissionGuiGroup {
        
        public final HolderLookup.Provider provider;
        public final ConfigHolderObject holder;
        public final List<GuiConfigControl> controls = new ArrayList<>();
        public GuiTextfield textfield;
        
        public PermissionGuiGroupMulti(HolderLookup.Provider provider, String group, ConfigHolderObject holder) {
            super(group);
            this.provider = provider;
            this.holder = holder;
        }
        
        @Override
        public Object save() {
            JsonObject json = new JsonObject();
            
            for (GuiConfigControl control : controls) {
                JsonElement element = control.save();
                if (element != null)
                    json.add(control.field.name, element);
            }
            
            holder.load(provider, false, true, json, Side.SERVER);
            return holder.object;
        }
        
        @Override
        protected void resetInternal() {
            for (GuiConfigControl control : controls)
                control.reset();
        }
        
        @Override
        public boolean isDefault() {
            save();
            return holder.isDefault(Side.SERVER);
        }
        
    }
    
}
