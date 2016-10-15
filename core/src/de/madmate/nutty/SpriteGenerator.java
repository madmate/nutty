package de.madmate.nutty;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by markus on 14.10.16.
 */
public class SpriteGenerator {
    public static Sprite generateSpriteForBody(TextureAtlas textureAtlas, Body body) {
        if ("horizontal".equals(body.getUserData())) {
            return createSprite(textureAtlas, "obstacleHorizontal");
        }
        if ("vertical".equals(body.getUserData())) {
            return createSprite(textureAtlas, "obstacleVertical");
        }
        if ("enemy".equals(body.getUserData())) {
            return createSprite(textureAtlas, "bird");
        }
        return null;
    }
    private static Sprite createSprite(TextureAtlas textureAtlas, String regionName) {
        Sprite sprite = new Sprite(textureAtlas.findRegion(regionName));
        sprite.setOriginCenter();
        return sprite;
    }
}
