package com.trf.theegg;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.shape.IShape;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import android.hardware.SensorManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Game_ing extends SimpleBaseGameActivity implements IAccelerationListener{
	
	private static final int CAMERA_WIDTH = 480;
	private static final int CAMERA_HEIGHT = 800;
	
	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0, 0.5f);
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mElephant;
	private TiledTextureRegion mBoxFaceTextureRegion;
	private TiledTextureRegion mCircleFaceTextureRegion;
	private Scene mScene;
	private PhysicsWorld mPhysicsWorld;
	private PhysicsWorld mElephantPhysicsWorld;
	private int mFaceCount = 0;
	private int mScore = 0;
	public long mAddDelay = 1000;
	public long mLastAdd = 0;
	private int times = 10;
	private int mMode = READY;
	public static final int PAUSE = 0;
	public static final int READY = 1;
	public static final int RUNNING = 2;
	public static final int LOSE = 3;
	@Override
	public EngineOptions onCreateEngineOptions() {
		// TODO Auto-generated method stub
//		Toast.makeText(this, "back键退出", Toast.LENGTH_SHORT).show();

		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	protected void onCreateResources() {
		// TODO Auto-generated method stub
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
		this.mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this.getAssets(), "face_box_tiled.png", 0, 0, 2, 1); // 64x32
		this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this.getAssets(), "face_circle_tiled.png", 0, 32, 2, 1); // 64x32
		this.mElephant = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this.getAssets(), "elephant2.png", 0, 64, 1,1);
		this.mBitmapTextureAtlas.load();
	}

	@Override
	protected Scene onCreateScene() {
		// TODO Auto-generated method stub
		initNewGame();
		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0, 0, 0));
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		this.mElephantPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mElephantPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mElephantPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mElephantPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		this.mScene.registerUpdateHandler(this.mElephantPhysicsWorld);
		

		final AnimatedSprite face = new AnimatedSprite(0, 0, this.mBoxFaceTextureRegion, this.getVertexBufferObjectManager());
		Body body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.DynamicBody, FIXTURE_DEF);
		final AnimatedSprite face1 = new AnimatedSprite(140, 0, this.mCircleFaceTextureRegion, this.getVertexBufferObjectManager());
		Body body1 = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face1, BodyType.DynamicBody, FIXTURE_DEF);
		final AnimatedSprite face2 = new AnimatedSprite(280, 0, this.mCircleFaceTextureRegion, this.getVertexBufferObjectManager());
		Body body2 = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face2, BodyType.DynamicBody, FIXTURE_DEF);
		final AnimatedSprite face3 = new AnimatedSprite(460, 0, this.mBoxFaceTextureRegion, this.getVertexBufferObjectManager());
		Body body3 = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face3, BodyType.DynamicBody, FIXTURE_DEF);
		final AnimatedSprite elephant = new AnimatedSprite(0, 736, this.mElephant, this.getVertexBufferObjectManager());//屏幕底部中间位置
		Body elephantBody = PhysicsFactory.createBoxBody(this.mElephantPhysicsWorld, elephant, BodyType.DynamicBody, FIXTURE_DEF);
		
		this.mScene.registerUpdateHandler(new IUpdateHandler() {
			
			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onUpdate(float arg0) {
				// TODO Auto-generated method stub
				
				if(elephant.collidesWith((IShape)face)){
					removeFace(face);
				}
				if(elephant.collidesWith((IShape)face1)){
					removeFace(face1);
				}
				if(elephant.collidesWith((IShape)face2)){
					removeFace(face2);
				}
				if(elephant.collidesWith((IShape)face3)){
					removeFace(face3);
				}
				
				update();
				/*else{
					face.setColor(0, 1, 0);
				}*/
			}
		});
		face.animate(200);
		face1.animate(200);
		face2.animate(200);
		face3.animate(200);
		this.mScene.attachChild(elephant);
		this.mScene.attachChild(face3);
		this.mScene.attachChild(face2);
		this.mScene.attachChild(face1);
		this.mScene.attachChild(face);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face1, body1, true, true));
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face2, body2, true, true));
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face3, body3, true, true));
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(elephant, elephantBody,true, true));
		
		return this.mScene;
	}
	private void initNewGame() {
		mScore = 0;
		mAddDelay = 1000;
		times = 10;
	}
	@Override
	protected synchronized void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		this.enableAccelerationSensor(this);
	}

	@Override
	public synchronized void onPauseGame() {
		// TODO Auto-generated method stub
		super.onPauseGame();
		this.disableAccelerationSensor();
	}
	@Override
	public void onAccelerationAccuracyChanged(AccelerationData arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccelerationChanged(AccelerationData arg0) {
		// TODO Auto-generated method stub
		final Vector2 gravity = Vector2Pool.obtain(arg0.getX(), 0);
		final Vector2 gravity1 = Vector2Pool.obtain(0, arg0.getY());
		this.mElephantPhysicsWorld.setGravity(gravity);
//		this.mPhysicsWorld.setGravity(gravity1);  可以使物体只有向下的重力
		Vector2Pool.recycle(gravity);
	}
	private void removeFace(final AnimatedSprite face) {
		final PhysicsConnector facePhysicsConnector = this.mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(face);

		this.mPhysicsWorld.unregisterPhysicsConnector(facePhysicsConnector);
//		this.mPhysicsWorld.destroyBody(facePhysicsConnector.getBody());

		this.mScene.detachChild(face);
		
		System.gc();
	}
	public void update() {
		if (true) {
			long now = System.currentTimeMillis();
			if (now - mLastAdd > mAddDelay) {
				mLastAdd = now;
//				addFace(10,10); 添加AnimatedSprite，执行非常快，必须设置间隔
				
			}
			if (times >= 0) {
				times--;
			}
			else {
				/*Intent intent = new Intent();
				intent.setClass(Game_ing.this, Main.class);
				startActivity(intent);
				finish();*/
			}
		}
	}
	private int addFace(final float pX, final float pY) {
		this.mFaceCount++;
		if(this.mFaceCount > 3){
			return 0;
		}
		
		final Body body;
		final AnimatedSprite face;
		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);

		if(this.mFaceCount % 2 == 0) {
			face = new AnimatedSprite(pX, pY, this.mBoxFaceTextureRegion, this.getVertexBufferObjectManager());
			body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
		} else {
			face = new AnimatedSprite(pX, pY, this.mCircleFaceTextureRegion, this.getVertexBufferObjectManager());
			body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
		}

		face.animate(200, true);

		this.mScene.registerTouchArea(face);
		this.mScene.attachChild(face);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));
		return 0;
	}
	public void setMode(int newMode) {
		int oldMode = mMode;
		mMode = newMode;
		if(newMode == RUNNING && oldMode != RUNNING);
		if (newMode == READY);
		if (newMode == PAUSE);
		if (newMode == LOSE);
	}
	public int getGameState() {
		return mMode;
	}
}



