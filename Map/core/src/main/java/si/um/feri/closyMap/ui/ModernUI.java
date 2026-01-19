package si.um.feri.closyMap.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;


public class ModernUI {
    private final BitmapFont font;

    public ModernUI(BitmapFont font) {
        this.font = font;
    }



    public TextButton button(String text) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = ModernTheme.TEXT;
        style.up = UIDrawables.rounded(ModernTheme.ACCENT, 10);
        style.over = UIDrawables.rounded(ModernTheme.ACCENT_LIGHT, 10);
        style.down = UIDrawables.rounded(ModernTheme.ACCENT_DARK, 10);
        style.disabled = UIDrawables.rounded(ModernTheme.withAlpha(ModernTheme.ACCENT, 0.4f), 10);
        style.disabledFontColor = ModernTheme.withAlpha(ModernTheme.TEXT, 0.5f);

        TextButton btn = new TextButton(text, style);
        btn.pad(10, 16, 10, 16);
        return btn;
    }

    public TextButton secondaryButton(String text) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = ModernTheme.TEXT_SECONDARY;
        style.up = UIDrawables.roundedWithBorder(ModernTheme.PANEL_LIGHT, ModernTheme.BORDER, 10, 1);
        style.over = UIDrawables.roundedWithBorder(ModernTheme.PANEL_LIGHT, ModernTheme.ACCENT, 10, 1);
        style.down = UIDrawables.rounded(ModernTheme.PANEL, 10);

        TextButton btn = new TextButton(text, style);
        btn.pad(10, 16, 10, 16);
        return btn;
    }

    public TextButton dangerButton(String text) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = ModernTheme.TEXT;
        style.up = UIDrawables.rounded(ModernTheme.ERROR, 10);
        style.over = UIDrawables.rounded(ModernTheme.lighten(ModernTheme.ERROR, 0.1f), 10);
        style.down = UIDrawables.rounded(ModernTheme.darken(ModernTheme.ERROR, 0.1f), 10);

        TextButton btn = new TextButton(text, style);
        btn.pad(10, 16, 10, 16);
        return btn;
    }

    public TextButton successButton(String text) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = ModernTheme.TEXT;
        style.up = UIDrawables.rounded(ModernTheme.SUCCESS, 10);
        style.over = UIDrawables.rounded(ModernTheme.lighten(ModernTheme.SUCCESS, 0.1f), 10);
        style.down = UIDrawables.rounded(ModernTheme.darken(ModernTheme.SUCCESS, 0.1f), 10);

        TextButton btn = new TextButton(text, style);
        btn.pad(10, 16, 10, 16);
        return btn;
    }



    public Label label(String text) {
        return new Label(text, new Label.LabelStyle(font, ModernTheme.TEXT));
    }

    public Label title(String text) {
        Label lbl = new Label(text, new Label.LabelStyle(font, ModernTheme.TEXT));
        lbl.setFontScale(1.2f);
        return lbl;
    }

    public Label subtitle(String text) {
        Label lbl = new Label(text, new Label.LabelStyle(font, ModernTheme.TEXT_SECONDARY));
        lbl.setFontScale(1.1f);
        return lbl;
    }

    public Label mutedLabel(String text) {
        return new Label(text, new Label.LabelStyle(font, ModernTheme.MUTED));
    }


    public Table panel() {
        Table t = new Table();
        t.setBackground(UIDrawables.rounded(ModernTheme.PANEL, 14));
        t.pad(16);
        return t;
    }


    public TextField textField(String text) {
        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = font;
        style.fontColor = ModernTheme.TEXT;
        style.messageFontColor = ModernTheme.MUTED;
        style.background = UIDrawables.roundedWithBorder(ModernTheme.BG, ModernTheme.BORDER, 8, 1);
        style.focusedBackground = UIDrawables.roundedWithBorder(ModernTheme.BG, ModernTheme.ACCENT, 8, 2);
        style.cursor = UIDrawables.solid(ModernTheme.ACCENT);
        style.selection = UIDrawables.solid(ModernTheme.withAlpha(ModernTheme.ACCENT, 0.3f));

        TextField tf = new TextField(text, style);
        return tf;
    }

    public TextField textField(String text, String placeholder) {
        TextField tf = textField(text);
        tf.setMessageText(placeholder);
        return tf;
    }


    public Slider slider(float min, float max, float step) {
        Slider.SliderStyle style = new Slider.SliderStyle();
        style.background = UIDrawables.pill(ModernTheme.withAlpha(ModernTheme.PANEL_LIGHT, 0.8f));
        style.knob = UIDrawables.circle(ModernTheme.ACCENT, 20);
        style.knobOver = UIDrawables.circle(ModernTheme.ACCENT_LIGHT, 22);
        style.knobDown = UIDrawables.circle(ModernTheme.ACCENT_DARK, 20);

        return new Slider(min, max, step, false, style);
    }


    public Actor divider() {
        Table line = new Table();
        line.setBackground(UIDrawables.solid(ModernTheme.DIVIDER));
        return line;
    }

    public BitmapFont getFont() {
        return font;
    }
}
