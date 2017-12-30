package ru.geekbrains.stargame.screen.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

import ru.geekbrains.engine.Base2DScreen;
import ru.geekbrains.engine.Sprite2DTexture;
import ru.geekbrains.engine.font.Font;
import ru.geekbrains.engine.math.Rect;
import ru.geekbrains.engine.math.Rnd;
import ru.geekbrains.engine.ui.ActionListener;
import ru.geekbrains.stargame.Background;
import ru.geekbrains.stargame.bullet.Bullet;
import ru.geekbrains.stargame.common.EnemiesEmitter;
import ru.geekbrains.stargame.explosion.Explosion;
import ru.geekbrains.stargame.pool.BulletPool;
import ru.geekbrains.stargame.pool.EnemyPool;
import ru.geekbrains.stargame.pool.ExplosionPool;
import ru.geekbrains.stargame.screen.game.ui.ButtonNewGame;
import ru.geekbrains.stargame.screen.game.ui.MessageGameOver;
import ru.geekbrains.stargame.ship.Enemy;
import ru.geekbrains.stargame.ship.MainShip;
import ru.geekbrains.stargame.star.TrackingStar;

import static com.badlogic.gdx.utils.Align.*;

public class GameScreen extends Base2DScreen implements ActionListener {

    private static final int STAR_COUNT = 56;
    private static final float STAR_WIDTH = 0.01f;
    private static final float FONT_SIZE = 0.02f;

    private static final String FRAGS = "Frags:";
    private static final String HP = "HP:";
    private static final String STAGE = "Stage:";

    private Sprite2DTexture textureBackground;
    private Background background;

    private TextureAtlas atlas;
    private MainShip mainShip;

    private TrackingStar[] trackingStars;

    private BulletPool bulletPool;
    private ExplosionPool explosionPool;
    private EnemyPool enemyPool;

    private Sound soundExplosion;
    private Sound soundBullet;
    private Sound soundLaser;
    private Music music;

    private EnemiesEmitter enemiesEmitter;

    private int frags; // количество убитых врагов

    private enum State {PLAYING, GAME_OVER}
    private State state;

    private MessageGameOver messageGameOver;
    private ButtonNewGame buttonNewGame;

    private Font font;
    private StringBuilder sbFrags = new StringBuilder();
    private StringBuilder sbHP = new StringBuilder();
    private StringBuilder sbStage = new StringBuilder();

    public GameScreen(Game game) {
        super(game);
    }

    @Override
    public void show() {
        super.show();

        this.soundLaser = Gdx.audio.newSound(Gdx.files.internal("sounds/laser.wav"));
        this.soundBullet = Gdx.audio.newSound(Gdx.files.internal("sounds/bullet.wav"));
        this.soundExplosion = Gdx.audio.newSound(Gdx.files.internal("sounds/explosion.wav"));
        this.music = Gdx.audio.newMusic(Gdx.files.internal("sounds/music.mp3"));
        this.music.setLooping(true);
        this.music.play();

        textureBackground = new Sprite2DTexture("textures/bg.png");
        background = new Background(new TextureRegion(textureBackground));
        atlas = new TextureAtlas("textures/mainAtlas.tpack");

        this.bulletPool = new BulletPool();
        this.explosionPool = new ExplosionPool(atlas, soundExplosion);

        this.mainShip = new MainShip(atlas, bulletPool, explosionPool, worldBounds, soundLaser);
        trackingStars = new TrackingStar[STAR_COUNT];
        for (int i = 0; i < trackingStars.length; i++) {
            trackingStars[i] = new TrackingStar(atlas, Rnd.nextFloat(-0.005f, 0.005f), Rnd.nextFloat(-0.5f, -0.1f), STAR_WIDTH, mainShip.getV());
        }

        this.enemyPool = new EnemyPool(bulletPool, explosionPool, worldBounds, mainShip);
        this.enemiesEmitter = new EnemiesEmitter(enemyPool, worldBounds, atlas, soundBullet);

        this.messageGameOver = new MessageGameOver(atlas);
        this.buttonNewGame = new ButtonNewGame(atlas, this);

        this.font = new Font("font/font.fnt", "font/font.png");
        this.font.setWorldSize(FONT_SIZE);

        startNewGame();
    }

    public void printInfo() {
        sbFrags.setLength(0);
        sbHP.setLength(0);
        sbStage.setLength(0);
        font.draw(batch, sbFrags.append(FRAGS).append(frags), worldBounds.getLeft(), worldBounds.getTop());
        font.draw(batch, sbHP.append(HP).append(mainShip.getHp()), worldBounds.pos.x, worldBounds.getTop(), center);
        font.draw(batch, sbStage.append(STAGE).append(enemiesEmitter.getStage()), worldBounds.getRight(), worldBounds.getTop(), right);
    }

    @Override
    public void render(float delta) {
        update(delta);
        if (state == State.PLAYING) {
            checkCollisions();
        }
        deleteAllDestroyed();
        draw();
    }

    /**
     * Метод обновления информации об объектах
     * @param delta
     */
    public void update(float delta) {
        for (int i = 0; i < trackingStars.length; i++) {
            trackingStars[i].update(delta);
        }
        explosionPool.updateActiveSprites(delta);

        switch (state) {
            case PLAYING:
                bulletPool.updateActiveSprites(delta);
                enemyPool.updateActiveSprites(delta);
                enemiesEmitter.generateEnemies(delta, frags);
                mainShip.update(delta);
                if (mainShip.isDestroyed()) {
                    state = State.GAME_OVER;
                }
                break;
            case GAME_OVER:
                break;
        }
    }

    /**
     * Проверка коллизий
     */
    public void checkCollisions() {

        // Столкновение кораблей
        List<Enemy> enemyList = enemyPool.getActiveObjects();
        for (Enemy enemy : enemyList) {
            if (enemy.isDestroyed()) {
                continue;
            }
            float minDist = enemy.getHalfWidth() + mainShip.getHalfWidth();
            if (enemy.pos.dst2(mainShip.pos) < minDist * minDist) {
                enemy.boom();
                enemy.destroy();
                mainShip.boom();
                mainShip.destroy();
                return;
            }
        }

        // Попадание пуль в корабль
        List<Bullet> bulletList = bulletPool.getActiveObjects();
        for (Enemy enemy : enemyList) {
            if (enemy.isDestroyed()) {
                continue;
            }
            for (Bullet bullet : bulletList) {
                if (bullet.getOwner() != mainShip || bullet.isDestroyed()) {
                    continue;
                }
                if (enemy.isBulletCollision(bullet)) {
                    enemy.damage(bullet.getDamage());
                    bullet.destroy();
                    if (enemy.isDestroyed()) {
                        frags++;
                        break;
                    }
                }
            }
        }

        // Попадание пуль в игровой корабль
        for (Bullet bullet : bulletList) {
            if (bullet.isDestroyed() || bullet.getOwner() == mainShip) {
                continue;
            }
            if (mainShip.isBulletCollision(bullet)) {
                mainShip.damage(bullet.getDamage());
                bullet.destroy();
            }
        }
    }

    /**
     * Удаление помеченных объектов
     */
    public void deleteAllDestroyed() {
        bulletPool.freeAllDestroyedActiveObjects();
        explosionPool.freeAllDestroyedActiveObjects();
        enemyPool.freeAllDestroyedActiveObjects();
    }

    /**
     * Метод отрисовки
     */
    public void draw() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        background.draw(batch);
        for (int i = 0; i < trackingStars.length; i++) {
            trackingStars[i].draw(batch);
        }
        if (!mainShip.isDestroyed()) {
            mainShip.draw(batch);
        }
        bulletPool.drawActiveObjects(batch);
        explosionPool.drawActiveObjects(batch);
        enemyPool.drawActiveObjects(batch);
        if (state == State.GAME_OVER) {
            messageGameOver.draw(batch);
            buttonNewGame.draw(batch);
        }
        printInfo();
        batch.end();
    }

    @Override
    public void dispose(){
        textureBackground.dispose();
        atlas.dispose();
        bulletPool.dispose();
        explosionPool.dispose();
        enemyPool.dispose();

        soundLaser.dispose();
        soundBullet.dispose();
        soundExplosion.dispose();
        music.dispose();

        font.dispose();

        super.dispose();
    }

    @Override
    protected void resize(Rect worldBounds) {
        background.resize(worldBounds);
        for (int i = 0; i < trackingStars.length; i++) {
            trackingStars[i].resize(worldBounds);
        }
        mainShip.resize(worldBounds);
    }


    private void startNewGame() {
        state = State.PLAYING;
        frags = 0;

        mainShip.setToNewGame();
        enemiesEmitter.setToNewGame();

        bulletPool.freeAllActiveObjects();
        enemyPool.freeAllActiveObjects();
        explosionPool.freeAllActiveObjects();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (state == State.PLAYING) {
            mainShip.keyDown(keycode);
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (state == State.PLAYING) {
            mainShip.keyUp(keycode);
        }
        return false;
    }

    @Override
    public void touchDown(Vector2 touch, int pointer) {
        switch (state) {
            case PLAYING:
                mainShip.touchDown(touch, pointer);
                break;
            case GAME_OVER:
                buttonNewGame.touchDown(touch, pointer);
                break;
        }
    }

    @Override
    public void touchUp(Vector2 touch, int pointer) {
        switch (state) {
            case PLAYING:
                mainShip.touchUp(touch, pointer);
                break;
            case GAME_OVER:
                buttonNewGame.touchUp(touch, pointer);
                break;
        }
    }

    @Override
    public void actionPerformed(Object src) {
        if (src == buttonNewGame) {
            startNewGame();
        } else {
            throw new RuntimeException("Unknown src = " + src);
        }
    }

}


