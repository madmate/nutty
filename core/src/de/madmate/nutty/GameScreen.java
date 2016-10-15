package de.madmate.nutty;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by markus on 13.10.16.
 */
public class GameScreen extends ScreenAdapter {
    private final NuttyGame nuttyGame;

    private static final float WORLD_WIDTH = 960;
    private static final float WORLD_HEIGHT = 544;
    private static float UNITS_PER_METER = 32F;
    private static float UNIT_WIDTH = WORLD_WIDTH / UNITS_PER_METER;
    private static float UNIT_HEIGHT = WORLD_HEIGHT / UNITS_PER_METER;

    private static final float MAX_STRENGTH = 15;
    private static final float MAX_DISTANCE = 100;
    private static final float UPPER_ANGLE = 3 * MathUtils.PI / 2f;
    private static final float LOWER_ANGLE = MathUtils.PI / 2f;

    private final Vector2 anchor = new Vector2(convertMetresToUnits(6.125f), convertMetresToUnits(5.75f));
    private final Vector2 fireingPosition = anchor.cpy();
    private float distance;
    private float angle;

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Viewport viewport;
    private OrthographicCamera camera;
    private OrthographicCamera box2dcam;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer orthogonalTiledMapRenderer;
    TextureAtlas textureAtlas;

    private ObjectMap<Body, Sprite> sprites = new ObjectMap<>();
    private Array<Body> toRemove = new Array<>();
    private Sprite slingshot, squirrel, staticAcorn;


    public GameScreen(NuttyGame nuttyGame) {
        this.nuttyGame = nuttyGame;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        clearScreen();
        update(delta);
        draw();
        //drawDebug();
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

        box2dcam = new OrthographicCamera(UNIT_WIDTH, UNIT_HEIGHT);

        TiledObjectBodyBuilder.buildBuildingBodies(tiledMap, world);
        TiledObjectBodyBuilder.buildFloorBodies(tiledMap, world);
        TiledObjectBodyBuilder.buildBirdBodies(tiledMap, world);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                createBullet();
                fireingPosition.set(anchor.cpy());
                return true;
            }
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                calculateAngleAndDistanceForBullet(screenX, screenY);
                return true;
            }
        });

        textureAtlas = nuttyGame.getAssetManager().get("nutty_assets.atlas");
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        for (Body body:bodies) {
            Sprite sprite = SpriteGenerator.generateSpriteForBody(textureAtlas, body);
            if (sprite != null) sprites.put(body, sprite);
        }

        slingshot = new Sprite(textureAtlas.findRegion("slingshot"));
        slingshot.setPosition(170, 64);
        squirrel = new Sprite(textureAtlas.findRegion("squirrel"));
        squirrel.setPosition(32, 64);
        staticAcorn = new Sprite(textureAtlas.findRegion("acorn"));

        world.setContactListener(new NuttyContactListener());
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private void update(float delta) {
        clearDeadBodies();
        world.step(delta, 6, 2);
        box2dcam.position.set(UNIT_WIDTH/2, UNIT_HEIGHT/2, 0);
        box2dcam.update();
        updateSpritePositions();
    }

    private void updateSpritePositions() {
        for (Body body : sprites.keys()) {
            Sprite sprite = sprites.get(body);
            sprite.setPosition(
                    convertMetresToUnits(body.getPosition().x) - sprite.getWidth() / 2f,
                    convertMetresToUnits(body.getPosition().y) - sprite.getHeight() / 2f);
            sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
        }
        staticAcorn.setPosition(fireingPosition.x - staticAcorn.getWidth() / 2f, fireingPosition.y - staticAcorn.getHeight() / 2f);
    }


    private void clearDeadBodies() {
        for (Body body:toRemove) {
            sprites.remove(body);
            world.destroyBody(body);
        }
        toRemove.clear();
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
        for (Sprite sprite:sprites.values()) {
            sprite.draw(batch);
        }
        squirrel.draw(batch);
        staticAcorn.draw(batch);
        slingshot.draw(batch);
        batch.end();
    }

    private void drawDebug() {
        debugRenderer.render(world, box2dcam.combined);
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(anchor.x -5, anchor.y-5, 10, 10);
        shapeRenderer.rect(fireingPosition.x -5, fireingPosition.y-5, 10, 10);
        shapeRenderer.line(anchor.x, anchor.y, fireingPosition.x, fireingPosition.y);
        shapeRenderer.end();
    }

    private void createBullet() {
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(0.5f);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body bullet = world.createBody(bodyDef);
        bullet.setUserData("acorn");
        bullet.createFixture(circleShape, 1);
        bullet.setTransform(new Vector2(convertUnitsToMetres(fireingPosition.x), convertUnitsToMetres(fireingPosition.y)), 0);

        Sprite sprite = new Sprite(textureAtlas.findRegion("acorn"));
        sprite.setOriginCenter();
        sprites.put(bullet, sprite);

        circleShape.dispose();

        float velX = Math.abs((MAX_STRENGTH*-MathUtils.cos(angle)*(distance/100f)));
        float velY = Math.abs((MAX_STRENGTH*-MathUtils.sin(angle)*(distance/100f)));
        bullet.setLinearVelocity(velX, velY);
    }

    private float convertMetresToUnits(float metres) {
        return metres * UNITS_PER_METER;
    }

    private float convertUnitsToMetres(float pixles) {
        return pixles/UNITS_PER_METER;
    }

    private float angleBetweenTwoPoints() {
        float angle = MathUtils.atan2(anchor.y -fireingPosition.y, anchor.x - fireingPosition.x);
        angle%=2 * MathUtils.PI;
        if (angle<0) angle+= MathUtils.PI2;
        return  angle;
    }

    private float distanceBetweenTwoPoints() {
        return (float) Math.sqrt(((anchor.x - fireingPosition.x) * (anchor.x - fireingPosition.x)) + ((anchor.y - fireingPosition.y) * (anchor.y - fireingPosition.y)));
    }

    private void calculateAngleAndDistanceForBullet(int screenX, int screenY) {
        fireingPosition.set(screenX, screenY);
        viewport.unproject(fireingPosition);
        distance = distanceBetweenTwoPoints();
        angle = angleBetweenTwoPoints();
        if (distance > MAX_DISTANCE) {
            distance = MAX_DISTANCE;
        }
        if (angle > LOWER_ANGLE) {
            if (angle > UPPER_ANGLE) {
                angle = 0;
            } else {
                angle = LOWER_ANGLE;
            }
        }
        fireingPosition.set(anchor.x + (distance * -MathUtils.cos(angle)), anchor.y + (distance * -MathUtils.sin(angle)));
    }

    private class NuttyContactListener implements ContactListener {

        @Override
        public void beginContact(Contact contact) {
            if (contact.isTouching()) {
                Fixture attacker = contact.getFixtureA();
                Fixture defender = contact.getFixtureB();
                WorldManifold worldManifold = contact.getWorldManifold();
                if ("enemy".equals(defender.getUserData())) {
                    Vector2 vel1 = attacker.getBody().getLinearVelocityFromWorldPoint(worldManifold.getPoints()[0]);
                    Vector2 vel2 = defender.getBody().getLinearVelocityFromWorldPoint(worldManifold.getPoints()[0]);
                    Vector2 impactVelocity = vel1.sub(vel2);
                    if (Math.abs(impactVelocity.x) > 1 || Math.abs(impactVelocity.y) > 1) {
                        toRemove.add(defender.getBody());
                    }
                }
            }
        }

        @Override
        public void endContact(Contact contact) {

        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }
    }
}
