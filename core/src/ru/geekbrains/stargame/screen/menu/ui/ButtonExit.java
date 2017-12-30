package ru.geekbrains.stargame.screen.menu.ui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import ru.geekbrains.engine.math.Rect;
import ru.geekbrains.engine.ui.ActionListener;
import ru.geekbrains.engine.ui.ScaledTouchUpButton;

public class ButtonExit extends ScaledTouchUpButton {

    public ButtonExit(TextureAtlas atlas, ActionListener actionListener, float pressScale) {
        super(atlas.findRegion("btExit"), actionListener, pressScale);
    }

    @Override
    public void resize(Rect worldBounds) {
        setBottom(worldBounds.getBottom());
        setRight(worldBounds.getRight());
    }
}
