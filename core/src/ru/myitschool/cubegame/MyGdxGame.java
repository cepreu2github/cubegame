package ru.myitschool.cubegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class MyGdxGame extends InputAdapter implements ApplicationListener {
    public PerspectiveCamera cam;

    public Model model;
    public ModelInstance instance;
    public ModelBatch modelBatch;

    public Environment environment;

    final float[] startPos = {150f, -9f, 0f};
    final float bound = 45f;
    float[] pos = {startPos[0], startPos[1], startPos[2]};
    float[] Vpos = new float[3];

    protected Label label;
    protected Label crosshair;
    protected BitmapFont font;
    protected Stage stage;

    protected long startTime;
    protected long hits;

    boolean isUnder = false;
    long underFire;

    final float zone = 12f;
    final float speed = 2f;
    public AssetManager assets;
    public boolean loading;

    @Override
    public void create() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 10f, 10f, 20f));

        modelBatch = new ModelBatch();
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(startPos[0], startPos[1], startPos[2]);
        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createCone(20f, 120f, 20f, 3,
                new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        instance = new ModelInstance(model);
        instance.transform.setToRotation(Vector3.Z, 90).translate(-5,0,0);
        // initialize speed
        for (int i = 0; i < 3; i++){
            Vpos[i] = getSpeed();
        }


        font = new BitmapFont();
        label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        crosshair = new Label("+", new Label.LabelStyle(font, Color.RED));
        crosshair.setPosition(Gdx.graphics.getWidth() / 2 - 3, Gdx.graphics.getHeight() / 2 - 9);

        stage = new Stage();
        stage.addActor(label);
        stage.addActor(crosshair);


        startTime = System.currentTimeMillis();

        Gdx.input.setInputProcessor(new InputMultiplexer(this));

        assets = new AssetManager();
        assets.load("space_frigate_6.g3db", Model.class);
        loading = true;
    }

    private float getSpeed(){
        return speed*Math.signum((float) Math.random()-0.5f)*Math.max((float) Math.random(), 0.5f);
    }

    @Override
    public void render() {
        if (loading)
            if (assets.update()){
                model = assets.get("space_frigate_6.g3db", Model.class);
                instance = new ModelInstance(model);
                loading = false;
            } else {
                return;
            }

        if(Math.abs(pos[1] - startPos[1]) < zone &&
                Math.abs(pos[2] - startPos[2]) < zone) {
            isUnder = true;
            crosshair.setColor(Color.RED);
        } else {
            isUnder = false;
            crosshair.setColor(Color.LIME);
            underFire = 0;
        }


        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        for (int i = 0; i < 3; i++) {
            pos[i] += Vpos[i];
            if (pos[i] <= startPos[i] - bound) {
                pos[i] = startPos[i] - bound;
                Vpos[i] = getSpeed();
            }
            if (pos[i] >= startPos[i] + bound) {
                pos[i] = startPos[i] + bound;
                Vpos[i] = getSpeed();
            }
        }
        cam.position.set(pos[0], pos[1], pos[2]);
        cam.update();

        modelBatch.begin(cam);
        modelBatch.render(instance, environment);
        modelBatch.end();


        StringBuilder builder = new StringBuilder();
        builder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
        long time = System.currentTimeMillis() - startTime;
        builder.append("| Game time: ").append(time);
        builder.append("| Hits: ").append(hits);
        builder.append("| Rating: ").append((float) hits / (float) time);
        label.setText(builder);
        stage.draw();

    }

    @Override
    public void dispose() {
        model.dispose();
        modelBatch.dispose();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isUnder) {
            underFire = System.currentTimeMillis();
        } else {
            hits /= 2;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (isUnder && underFire != 0) {
            hits += System.currentTimeMillis() - underFire;
            underFire = 0;
        } else {
            hits /= 2;
        }
        return false;
    }
}