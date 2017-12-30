package ru.geekbrains.stargame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class StarGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	TextureRegion textureRegion;
	Texture background;
//	Vector2 vector1;
//	Vector2 vector2;
	
	@Override
	public void create () {
//		vector1 = new Vector2(0, 1);
//		vector2 = new Vector2(3, -2);
//		vector1.add(vector2);
//		System.out.println("vector1 + vector2 = " + vector1.x + " " + vector1.y);
//
//		vector1.set(1,2);
//		vector2.set(4,3);
//		vector2.sub(vector1);
//		System.out.println("vector2 - vector1 = " + vector2.x + " " + vector2.y);
//
//		vector2.scl(0.9f);
//		System.out.println("vector2 * 0.9f = " + vector2.x + " " + vector2.y);
//
//		vector2.set(0,0);
//		System.out.println("len vector2 = " + vector2.len());
//
//		vector2.nor();
//		System.out.println("nor vector2 = " +  vector2.x + " " + vector2.y);
//
//		vector1.set(1, 1);
//		vector2.set(-1, 1);
//		vector1.nor();
//		vector2.nor();
//		System.out.println("Угол = " + Math.acos(vector1.dot(vector2)));

		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		background = new Texture("bg.png");
		textureRegion = new TextureRegion(img, 20, 20, 100, 100);

	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(background, 0, 0);
		batch.draw(img, 0, 0);
		batch.draw(textureRegion, 0, 0);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
