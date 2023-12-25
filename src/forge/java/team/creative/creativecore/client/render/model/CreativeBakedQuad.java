package team.creative.creativecore.client.render.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.mc.ColorUtils;

public class CreativeBakedQuad extends BakedQuad {
    
    public final RenderBox cube;
    
    public CreativeBakedQuad(BakedQuad quad, RenderBox cube, int tintedColor, boolean shouldOverrideColor) {
        this(quad, cube, tintedColor, shouldOverrideColor, quad.getDirection(), false);
    }
    
    public CreativeBakedQuad(BakedQuad quad, RenderBox cube, int tintedColor, boolean shouldOverrideColor, Direction direction) {
        this(quad, cube, tintedColor, shouldOverrideColor, direction, false);
    }
    
    private CreativeBakedQuad(BakedQuad quad, RenderBox cube, int tintedColor, boolean shouldOverrideColor, Direction facing, boolean something) {
        super(copyArray(quad.getVertices()), shouldOverrideColor ? tintedColor : quad.getTintIndex(), facing, quad.getSprite(), quad.isShade());
        this.cube = cube;
    }
    
    private static int[] copyArray(int[] array) {
        int[] newarray = new int[array.length];
        for (int i = 0; i < array.length; i++)
            newarray[i] = array[i];
        return newarray;
    }
    
    public void updateAlpha() {
        int alpha = ColorUtils.alpha(cube.color);
        if (alpha == 255)
            return;
        for (int k = 0; k < 4; k++) {
            int index = k * IQuadTransformer.STRIDE + IQuadTransformer.COLOR;
            getVertices()[index] = ColorUtils.setAlpha(getVertices()[index], alpha);
        }
    }
    
}
