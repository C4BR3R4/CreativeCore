package team.creative.creativecore.common.gui.controls.simple;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.mc.ColorUtils.ColorPart;
import team.creative.creativecore.common.util.type.Color;

public class GuiColoredSteppedSlider extends GuiSteppedSlider {
    
    public GuiColorPicker picker;
    public ColorPart part;
    
    public GuiColoredSteppedSlider(String name, GuiColorPicker picker, ColorPart part) {
        super(name, part.get(picker.color), 0, 255);
        this.picker = picker;
        this.part = part;
    }
    
    @Override
    public void setValue(double value) {
        super.setValue((int) value);
        if (part != null) {
            part.set(picker.color, this.getIntValue());
            picker.onColorChanged();
        }
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    protected void renderContent(GuiGraphics graphics, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        if (part == ColorPart.ALPHA) {
            Color startColor = new Color(picker.color);
            startColor.setAlpha(0);
            Color endColor = new Color(picker.color);
            endColor.setAlpha(255);
            GuiRenderHelper.horizontalGradientRect(graphics, 0, 0, (int) rect.getWidth(), (int) rect.getHeight(), startColor.toInt(), endColor.toInt());
        } else
            GuiRenderHelper.horizontalGradientMaskRect(graphics, 0, 0, (int) rect.getWidth(), (int) rect.getHeight(), picker.color.toInt(), part.code);
        super.renderContent(graphics, control, rect, mouseX, mouseY);
    }
}