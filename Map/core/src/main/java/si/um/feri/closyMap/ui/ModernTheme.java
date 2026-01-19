package si.um.feri.closyMap.ui;

import com.badlogic.gdx.graphics.Color;


public class ModernTheme {
    public static final Color BG = new Color(0.08f, 0.10f, 0.18f, 0.95f);
    public static final Color PANEL = new Color(0.10f, 0.13f, 0.22f, 0.95f);
    public static final Color PANEL_LIGHT = new Color(0.12f, 0.16f, 0.26f, 0.95f);

    public static final Color ACCENT = new Color(0.25f, 0.52f, 0.96f, 1f);
    public static final Color ACCENT_LIGHT = new Color(0.40f, 0.65f, 1f, 1f);
    public static final Color ACCENT_DARK = new Color(0.18f, 0.40f, 0.80f, 1f);

    public static final Color SUCCESS = new Color(0.0f, 0.6f, 0.1f, 1f);

    public static final Color ERROR = new Color(0.95f, 0.35f, 0.40f, 1f);

    public static final Color TEXT = new Color(0.95f, 0.96f, 0.98f, 1f);
    public static final Color TEXT_SECONDARY = new Color(0.75f, 0.80f, 0.88f, 1f);
    public static final Color MUTED = new Color(0.50f, 0.55f, 0.65f, 1f);

    public static final Color BORDER = new Color(0.20f, 0.25f, 0.35f, 0.6f);
    public static final Color DIVIDER = new Color(1f, 1f, 1f, 0.08f);



    public static Color darken(Color color, float amount) {
        return new Color(
            Math.max(0, color.r - amount),
            Math.max(0, color.g - amount),
            Math.max(0, color.b - amount),
            color.a
        );
    }

    public static Color lighten(Color color, float amount) {
        return new Color(
            Math.min(1, color.r + amount),
            Math.min(1, color.g + amount),
            Math.min(1, color.b + amount),
            color.a
        );
    }

    public static Color withAlpha(Color color, float alpha) {
        return new Color(color.r, color.g, color.b, alpha);
    }
}
