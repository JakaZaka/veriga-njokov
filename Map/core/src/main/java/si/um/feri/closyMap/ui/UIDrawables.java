package si.um.feri.closyMap.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;


public class UIDrawables {


    public static Drawable rounded(Color color, int radius) {
        int size = radius * 2 + 4;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        drawRoundedRect(pm, 0, 0, size, size, radius);

        Texture tex = new Texture(pm);
        pm.dispose();


        int inset = radius + 1;
        NinePatch np = new NinePatch(tex, inset, inset, inset, inset);
        return new NinePatchDrawable(np);
    }


    public static Drawable roundedWithBorder(Color fillColor, Color borderColor, int radius, int borderWidth) {
        int size = radius * 2 + 6;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        // Draw border
        pm.setColor(borderColor);
        drawRoundedRect(pm, 0, 0, size, size, radius);

        // Draw fill
        pm.setColor(fillColor);
        drawRoundedRect(pm, borderWidth, borderWidth,
            size - borderWidth * 2, size - borderWidth * 2,
            Math.max(1, radius - borderWidth));

        Texture tex = new Texture(pm);
        pm.dispose();

        int inset = radius + 2;
        NinePatch np = new NinePatch(tex, inset, inset, inset, inset);
        return new NinePatchDrawable(np);
    }

    // slider
    public static Drawable pill(Color color) {
        int height = 12;
        int width = 24;
        Pixmap pm = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pm.setColor(color);

        int radius = height / 2;
        // Left circle
        pm.fillCircle(radius, radius, radius - 1);
        // Right circle
        pm.fillCircle(width - radius, radius, radius - 1);
        // Middle rectangle
        pm.fillRectangle(radius, 0, width - radius * 2, height);

        Texture tex = new Texture(pm);
        pm.dispose();

        NinePatch np = new NinePatch(tex, radius, radius, 1, 1);
        return new NinePatchDrawable(np);
    }

    //slider knob
    public static Drawable circle(Color color, int diameter) {
        Pixmap pm = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fillCircle(diameter / 2, diameter / 2, diameter / 2 - 1);

        Texture tex = new Texture(pm);
        pm.dispose();

        return new TextureRegionDrawable(tex);
    }


    public static Drawable solid(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();

        Texture tex = new Texture(pm);
        pm.dispose();

        return new TextureRegionDrawable(tex);
    }

    //rounded rectangle
    private static void drawRoundedRect(Pixmap pm, int x, int y, int width, int height, int radius) {
        if (radius <= 0) {
            pm.fillRectangle(x, y, width, height);
            return;
        }

        radius = Math.min(radius, Math.min(width, height) / 2);

        // Center cross
        pm.fillRectangle(x + radius, y, width - radius * 2, height);
        pm.fillRectangle(x, y + radius, width, height - radius * 2);

        // Corners
        pm.fillCircle(x + radius, y + radius, radius);
        pm.fillCircle(x + width - radius - 1, y + radius, radius);
        pm.fillCircle(x + radius, y + height - radius - 1, radius);
        pm.fillCircle(x + width - radius - 1, y + height - radius - 1, radius);
    }
}
