package de.madmate.nutty;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by markus on 13.10.16.
 */
public class GameScreen extends ScreenAdapter {
    private final NuttyGame nuttyGame;

    private static final float WORLD_WIDTH = 960;
    private static final float WORLD_HEIGHT = 544;
    private static final float UNITS_PER_METER = 32F;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Viewport viewport;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    SpriteBatch batch;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer orthogonalTiledMapRenderer;


    public GameScreen(NuttyGame nuttyGame) {
        this.nuttyGame = nuttyGame;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        clearScreen();
        update(delta);
        draw();
        drawDebug();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public void show() {
        super.show();
        world = new World(new Vector2(0, -9.81F), true);
        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply(true);
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        tiledMap = nuttyGame.getAssetManager().get("nuttybirds.tmx");
        orthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, batch);
        orthogonalTiledMapRenderer.setView(camera);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private void update(float delta) {
        world.step(delta, 6, 2);
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(Color.TEAL.r, Color.TEAL.g, Color.TEAL.b, Color.TEAL.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void draw() {
        batch.setProjectionMatrix(camera.projection);
        batch.setTransformMatrix(camera.view);
        orthogonalTiledMapRenderer.render();
        batch.begin();
        batch.end();
    }

    private void drawDebug() {
        debugRenderer.render(world, camera.combined);
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.end();

    }
}
