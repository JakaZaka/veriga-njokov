package si.um.feri.closyMap;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import si.um.feri.closyMap.ui.*;



import java.io.IOException;

import si.um.feri.closyMap.dataBaseUtils.ApiService;
import si.um.feri.closyMap.dataBaseUtils.LocationDTO;
import si.um.feri.closyMap.dataBaseUtils.StoreOccupancyDTO;
import si.um.feri.closyMap.dataBaseUtils.UserDTO;
import si.um.feri.closyMap.utils.Constants;
import si.um.feri.closyMap.utils.Geolocation;
import si.um.feri.closyMap.utils.MapRasterTiles;
import si.um.feri.closyMap.utils.ZoomXY;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ClosyMap extends ApplicationAdapter implements GestureDetector.GestureListener {
    private Array<LocationDTO> storeLocations = new Array<>();
    private Array<UserDTO> users = new Array<>();

    private int currentTileZoom = Constants.ZOOM;

    private LocationDTO selectedStore = null;
    private UserDTO selectedUser = null;

    private ShapeRenderer shapeRenderer;

    private Texture userIcon;
    private Texture storeIcon;
    private Texture currentLocationIcon;

    private SpriteBatch batch;


    private Vector3 touchPosition;

    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private OrthographicCamera camera;



    private Texture[] mapTiles;
    private ZoomXY beginTile;   // top left tile

    private Vector2 popupPos = new Vector2();

    private float popupAlpha = 0f;


    // center geolocation
    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.557314, 15.637771);

    // test marker
    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);

    //UI
    private Stage uiStage;
    private BitmapFont uiFont;
    private ModernUI ui;


    private boolean storePanelVisible = false;

    private ObjectMap<String, StoreOccupancyDTO> occupancyMap =
        new ObjectMap<>();
    private ObjectMap<String, Float> animatedOccupancy =
        new ObjectMap<>();


    private float occupancyTimer = 0f;

    private static final int MAX_PEOPLE = 70;

    private int selectedDay = 2;   // 1 = ponedeljek
    private int selectedHour = 12; // 0–23

    private boolean simulationMode = false;

    private boolean timePanelVisible = false;

    private boolean playMode = false;

    private float playTimer = 0f;
    private static final float PLAY_STEP_TIME = 0.6f;

    private int playDay = 1;   // 1–7
    private int playHour = 0;  // 0–23
    private boolean sliderDragging = false;


    private Table storePanel;
    private Table timePanel;
    private Label timeStatusLabel;
    private Slider timeSlider;
    private TextButton playButton;
    private TextButton pauseButton;
    private Table infoPopup;






    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        userIcon = new Texture("wardrobe.png");
        storeIcon = new Texture("store.png");
        currentLocationIcon = new Texture("currentLocation.png");
        batch = new SpriteBatch();


        uiStage = new Stage(
            new ExtendViewport(1280, 720)
        );

        uiFont = new BitmapFont(Gdx.files.internal("font/uiText.fnt"));
        ui = new ModernUI(uiFont);

        createToggleStoreButton();


        timeSlider = ui.slider(0, 7 * 24 - 1, 1);
        timeSlider.setWidth(320);
        timeSlider.setVisible(false);

        timeSlider.addListener(event -> {
            if (!timeSlider.isDragging()) return false;

            sliderDragging = true;

            int value = (int) timeSlider.getValue();
            selectedDay = value / 24 + 1;
            selectedHour = value % 24;

            simulationMode = true;
            //playMode = false;

            playDay = selectedDay;
            playHour = selectedHour;


            updateOccupancyForSelectedTime();
            updateTimeStatusLabel();

            return true;
        });



        Table statusChip = ui.panel();
        statusChip.pad(10, 20, 10, 20);

        timeStatusLabel = ui.label("");
        timeStatusLabel.setAlignment(Align.center);

        statusChip.add(timeStatusLabel);

        Table statusWrapper = new Table();
        statusWrapper.setFillParent(true);
        statusWrapper.top().padTop(12);
        statusWrapper.add(statusChip);

        uiStage.addActor(statusWrapper);


        updateTimeStatusLabel();

        Table sliderTable = new Table();
        sliderTable.setFillParent(true);
        sliderTable.bottom().pad(16);

        sliderTable.add(timeSlider);

        uiStage.addActor(sliderTable);


        ApiService.loadStoreLocations(data -> {
            storeLocations = data;

            loadLiveOccupancy();

            if (storePanel != null) storePanel.remove();
            createStorePanel();
        });
        ApiService.loadUsers(data -> users = data);




        touchPosition = new Vector3();

        InputMultiplexer multiplexer = new InputMultiplexer();

        multiplexer.addProcessor(new GestureDetector(this));
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(mouse);

                float oldZoom = camera.zoom;
                camera.zoom += amountY * 0.1f;
                float minZoomX = camera.viewportWidth  / Constants.MAP_WIDTH;
                float minZoomY = camera.viewportHeight / Constants.MAP_HEIGHT;
                float minZoom  = Math.max(minZoomX, minZoomY);

                camera.zoom = MathUtils.clamp(camera.zoom, minZoom, 2f);


                float ratio = camera.zoom / oldZoom;
                camera.position.x += (mouse.x - camera.position.x) * (1 - ratio);
                camera.position.y += (mouse.y - camera.position.y) * (1 - ratio);

                return true;
            }
        });

        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {

                Actor hit = uiStage.hit(screenX, uiStage.getViewport().getScreenHeight() - screenY, true);
                if (hit != null) {
                    return false;
                }




                Vector3 world = new Vector3(screenX, screenY, 0);
                camera.unproject(world);

                if (infoPopup != null) {
                    infoPopup.remove();
                    infoPopup = null;
                }

                popupPos.set(world.x + 20, world.y + 20);
                popupAlpha = 0f;




                float clickRadius = 45f * camera.zoom;

                if (storePanelVisible) {
                    toggleStorePanel();
                }
                if (timePanelVisible) {
                    hideTimePanel();
                }
                if(selectedStore != null){
                    selectedStore = null;
                }
                if (selectedUser != null){
                    selectedUser = null;
                }


                // STORES
                for (LocationDTO loc : storeLocations) {
                    Vector2 pos = MapRasterTiles.getPixelPosition(
                        loc.coordinates.coordinates[1],
                        loc.coordinates.coordinates[0],
                        beginTile.x,
                        beginTile.y
                    );

                    if (pos.dst(world.x, world.y) < clickRadius) {
                        selectedStore = loc;
                        selectedUser = null;
                        showInfoPopup();
                        return true;
                    }
                }

                // USERS
                for (UserDTO user : users) {
                    if (user.location == null || user.location.coordinates == null) continue;

                    Vector2 pos = MapRasterTiles.getPixelPosition(
                        user.location.coordinates.coordinates[1],
                        user.location.coordinates.coordinates[0],
                        beginTile.x,
                        beginTile.y
                    );

                    if (pos.dst(world.x, world.y) < clickRadius) {
                        selectedStore = null;
                        selectedUser = user;
                        showInfoPopup();
                        return true;
                    }
                }

                return false;
            }
        });


        Gdx.input.setInputProcessor(multiplexer);


        try {
            //in most cases, geolocation won't be in the center of the tile because tile borders are predetermined (geolocation can be at the corner of a tile)
            ZoomXY centerTile = MapRasterTiles.getTileNumber(CENTER_GEOLOCATION.lat, CENTER_GEOLOCATION.lng, Constants.ZOOM);
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES);
            //you need the beginning tile (tile on the top left corner) to convert geolocation to a location in pixels.
            beginTile = new ZoomXY(Constants.ZOOM, centerTile.x - ((Constants.NUM_TILES - 1) / 2), centerTile.y - ((Constants.NUM_TILES - 1) / 2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        tiledMap = new TiledMap();
        MapLayers layers = tiledMap.getLayers();

        TiledMapTileLayer layer = new TiledMapTileLayer(Constants.NUM_TILES, Constants.NUM_TILES, MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE);
        int index = 0;
        for (int j = Constants.NUM_TILES - 1; j >= 0; j--) {
            for (int i = 0; i < Constants.NUM_TILES; i++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(new TextureRegion(mapTiles[index], MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE)));
                layer.setCell(i, j, cell);
                index++;
            }
        }
        layers.add(layer);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        camera = new OrthographicCamera();
        camera.setToOrtho(
            false,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );




        camera.zoom = 1f;

        Vector2 centerPixel = MapRasterTiles.getPixelPosition(
            CENTER_GEOLOCATION.lat,
            CENTER_GEOLOCATION.lng,
            beginTile.x,
            beginTile.y
        );

        camera.position.set(centerPixel.x, centerPixel.y, 0);
        camera.update();

        camera.update();

    }



    private void updateTimeStatusLabel() {
        if (playMode) {
            timeStatusLabel.setText(
                "▶ Playing • " + getDayName(playDay) + " • " + String.format("%02d:00", playHour)
            );
        } else if (simulationMode) {
            timeStatusLabel.setText(
                "Selected • " + getDayName(selectedDay) + " • " + String.format("%02d:00", selectedHour)
            );
        } else {
            timeStatusLabel.setText("NOW • Live occupancy");
        }
    }

    private String getDayName(int day) {
        switch (day) {
            case 1: return "Monday";
            case 2: return "Tuesday";
            case 3: return "Wednesday";
            case 4: return "Thursday";
            case 5: return "Friday";
            case 6: return "Saturday";
            case 7: return "Sunday";
            default: return "";
        }
    }




    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        if (playMode && !timeSlider.isDragging()) {
            playTimer += Gdx.graphics.getDeltaTime();

            if (playTimer >= PLAY_STEP_TIME) {
                playTimer = 0f;
                advancePlayTime();
            }
        }


        handleInput();

        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        popupAlpha = Math.min(1f, popupAlpha + Gdx.graphics.getDeltaTime() * 6f);

        if (!simulationMode) {
            occupancyTimer += Gdx.graphics.getDeltaTime();

            if (occupancyTimer > 300f) {
                loadLiveOccupancy();
                occupancyTimer = 0f;
            }
        }

        updateOccupancyAnimation(Gdx.graphics.getDeltaTime());

        drawMarkers();
        uiStage.act(Gdx.graphics.getDeltaTime());
        uiStage.draw();
    }


    private void drawMarkers() {
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (LocationDTO loc : storeLocations) {
            StoreOccupancyDTO occ = occupancyMap.get(loc._id);
            if (occ == null || loc.coordinates == null) continue;

            Vector2 pos = MapRasterTiles.getPixelPosition(
                loc.coordinates.coordinates[1],
                loc.coordinates.coordinates[0],
                beginTile.x,
                beginTile.y
            );

            float animated =
                animatedOccupancy.get(loc._id, (float)occ.peopleCount);

            drawOccupancyRing(pos, Math.round(animated));
        }

        shapeRenderer.end();
        batch.begin();
        float baseIconSize = 31f;
        float scale = MathUtils.clamp(1f / camera.zoom, 0.01f, 1.4f);
        float iconSize = baseIconSize / scale;


        // STORES
        for (LocationDTO loc : storeLocations) {
            if (loc.coordinates == null || loc.coordinates.coordinates == null) {
                System.out.println("STORE WITHOUT COORDINATES: " + loc._id);
                continue;
            }
            float lon = loc.coordinates.coordinates[0];
            float lat = loc.coordinates.coordinates[1];

            Vector2 pos = MapRasterTiles.getPixelPosition(lat, lon, beginTile.x, beginTile.y);


            batch.draw(
                storeIcon,
                pos.x - iconSize / 2,
                pos.y - iconSize / 2,
                iconSize,
                iconSize
            );
        }

        // USERS
        for (UserDTO user : users) {
            if (user.location == null || user.location.coordinates == null) continue;

            float lon = user.location.coordinates.coordinates[0];
            float lat = user.location.coordinates.coordinates[1];

            Vector2 pos = MapRasterTiles.getPixelPosition(lat, lon, beginTile.x, beginTile.y);

            batch.draw(
                userIcon,
                pos.x - iconSize / 2,
                pos.y - iconSize / 2,
                iconSize,
                iconSize
            );
        }

        batch.end();


    }

    private void drawOccupancyRing(Vector2 center, int people) {
        float fill = MathUtils.clamp(people / (float) MAX_PEOPLE, 0f, 1f);

        float scale = MathUtils.clamp(1f / camera.zoom, 0.3f, 1.4f);
        float radius = 28f / scale;
        float thickness = 5f / scale;
        int segments = 60;

        float startAngle = -90f;


        shapeRenderer.setColor(new Color(0.7f, 0.7f, 0.7f, 0.4f));

        for (int i = 0; i < segments; i++) {
            float angle1 = startAngle + (i * 360f / segments);
            float angle2 = startAngle + ((i + 1) * 360f / segments);

            float x1 = center.x + MathUtils.cosDeg(angle1) * radius;
            float y1 = center.y + MathUtils.sinDeg(angle1) * radius;

            float x2 = center.x + MathUtils.cosDeg(angle2) * radius;
            float y2 = center.y + MathUtils.sinDeg(angle2) * radius;

            shapeRenderer.rectLine(x1, y1, x2, y2, thickness);
        }

        Color color = getOccupancyColor(fill);

        int filledSegments = MathUtils.round(segments * fill);
        shapeRenderer.setColor(color);

        for (int i = 0; i < filledSegments; i++) {
            float angle1 = startAngle + (i * 360f / segments);
            float angle2 = startAngle + ((i + 1) * 360f / segments);

            float x1 = center.x + MathUtils.cosDeg(angle1) * radius;
            float y1 = center.y + MathUtils.sinDeg(angle1) * radius;

            float x2 = center.x + MathUtils.cosDeg(angle2) * radius;
            float y2 = center.y + MathUtils.sinDeg(angle2) * radius;

            shapeRenderer.rectLine(x1, y1, x2, y2, thickness);
        }
    }



    private void reloadMapTiles(int zoom) {
        try {
            ZoomXY centerTile = MapRasterTiles.getTileNumber(
                CENTER_GEOLOCATION.lat,
                CENTER_GEOLOCATION.lng,
                zoom
            );

            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES);

            beginTile = new ZoomXY(
                zoom,
                centerTile.x - ((Constants.NUM_TILES - 1) / 2),
                centerTile.y - ((Constants.NUM_TILES - 1) / 2)
            );

            TiledMapTileLayer layer = new TiledMapTileLayer(
                Constants.NUM_TILES,
                Constants.NUM_TILES,
                MapRasterTiles.TILE_SIZE,
                MapRasterTiles.TILE_SIZE
            );

            int index = 0;
            for (int j = Constants.NUM_TILES - 1; j >= 0; j--) {
                for (int i = 0; i < Constants.NUM_TILES; i++) {
                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    cell.setTile(new StaticTiledMapTile(
                        new TextureRegion(mapTiles[index++])
                    ));
                    layer.setCell(i, j, cell);
                }
            }

            MapLayers layers = tiledMap.getLayers();

            while (layers.getCount() > 0) {
                layers.remove(0);
            }

            layers.add(layer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showInfoPopup() {
        // Remove old popup
        if (infoPopup != null) {
            infoPopup.remove();
            infoPopup = null;
        }

        Table content = ui.panel();
        content.defaults().left().padBottom(4);


        content.add(ui.subtitle(
            selectedStore != null ? "Store info" : "User info"
        )).row();
        content.add(createDivider()).growX().height(1).pad(6, 0, 8, 0).row();

        // Store info
        if (selectedStore != null) {
            content.add(ui.mutedLabel("Name")).row();
            content.add(ui.label(selectedStore.clothingStoreId.name)).row();

            content.add(ui.mutedLabel("Website")).padTop(4).row();
            content.add(ui.label(selectedStore.clothingStoreId.website)).row();

            content.add(ui.mutedLabel("Address")).padTop(4).row();
            content.add(ui.label(selectedStore.address)).row();

            Integer people = getOccupancyForStore(selectedStore);

            content.add(ui.mutedLabel(
                simulationMode ? "Simulated occupancy" : "Current occupancy"
            )).padTop(6).row();

            if (people != null) {
                content.add(
                    ui.label(people + " people")
                ).row();
            } else {
                content.add(
                    ui.mutedLabel("No data")
                ).row();
            }
        }



        // User info
        if (selectedUser != null) {
            content.add(ui.mutedLabel("Username")).row();
            content.add(ui.label(selectedUser.username)).row();

            content.add(ui.mutedLabel("Email")).padTop(4).row();
            content.add(ui.label(selectedUser.email)).row();

            if (selectedUser.location != null) {
                content.add(ui.mutedLabel("Address")).padTop(4).row();
                content.add(ui.label(selectedUser.location.address)).row();
            }
        }


        TextButton close = ui.secondaryButton("Close");
        close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                infoPopup.addAction(Actions.sequence(
                    Actions.fadeOut(0.15f),
                    Actions.removeActor()
                ));
                infoPopup = null;
            }
        });

        content.add(close).padTop(10).width(150);


        infoPopup = new Table();
        infoPopup.setFillParent(true);
        infoPopup.bottom().left().pad(20);
        infoPopup.add(content);

        infoPopup.getColor().a = 0f;
        infoPopup.addAction(Actions.fadeIn(0.2f));

        uiStage.addActor(infoPopup);
    }



    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        userIcon.dispose();
        storeIcon.dispose();

    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        touchPosition.set(x, y, 0);
        camera.unproject(touchPosition);
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {Actor hit = uiStage.hit(
        Gdx.input.getX(),
            uiStage.getViewport().getScreenHeight() - Gdx.input.getY(),
            true
        );

        if (hit != null) {
            return false;
        }
        camera.translate(-deltaX * camera.zoom, deltaY * camera.zoom);
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (initialDistance >= distance)
            camera.zoom += 0.02;
        else
            camera.zoom -= 0.02;
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }
    @Override
    public void resize(int width, int height) {
        uiStage.getViewport().update(width, height, true);
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }


    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.zoom += 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom -= 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 3, 0);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            selectedStore = null;
            selectedUser = null;
        }



        float minZoomX = camera.viewportWidth  / Constants.MAP_WIDTH;
        float minZoomY = camera.viewportHeight / Constants.MAP_HEIGHT;
        float minZoom  = Math.max(minZoomX, minZoomY);

        camera.zoom = MathUtils.clamp(camera.zoom, minZoom, 2f);


        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        camera.position.x = MathUtils.clamp(camera.position.x, effectiveViewportWidth / 2f, Constants.MAP_WIDTH - effectiveViewportWidth / 2f);
        camera.position.y = MathUtils.clamp(camera.position.y, effectiveViewportHeight / 2f, Constants.MAP_HEIGHT - effectiveViewportHeight / 2f);
    }

    private void createToggleStoreButton() {
        Table root = ui.panel();

        TextButton stores = ui.secondaryButton("Stores");
        TextButton time = ui.secondaryButton("Time");
        TextButton now = ui.secondaryButton("Now");
        playButton = ui.button("Play");
        pauseButton = ui.dangerButton("Pause");
        pauseButton.setVisible(false);

        root.add(stores).width(120).row();
        root.add(time).padTop(8).width(120).row();
        root.add(now).padTop(8).width(120).row();
        root.add(playButton).padTop(8).width(120).row();
        root.add(pauseButton).padTop(8).width(120);


        Table wrapper = new Table();
        wrapper.setFillParent(true);
        wrapper.top().left().pad(16);
        wrapper.add(root);


        stores.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleStorePanel();
            }
        });

        time.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleTimePanel();
                playMode = false;
                playTimer = 0f;
                pauseButton.setVisible(false);
                timeSlider.setVisible(false);

            }
        });

        now.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playMode = false;
                simulationMode = false;
                playTimer = 0f;
                pauseButton.setVisible(false);
                timeSlider.setVisible(false);

                loadLiveOccupancy();
                updateTimeStatusLabel();

            }
        });
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!playMode) {
                    startPlayMode();
                }

                timeSlider.setVisible(true);
                timeSlider.setValue((playDay - 1) * 24 + playHour);
                pauseButton.setVisible(true);
                pauseButton.setDisabled(false);
                updateTimeStatusLabel();

            }
        });

        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                playMode = false;
                pauseButton.setDisabled(true);
                updateTimeStatusLabel();
            }
        });


        uiStage.addActor(wrapper);
    }

    private void createStorePanel() {
        if (storePanel != null) storePanel.remove();

        Table content = ui.panel();

        content.add(ui.subtitle("Stores")).left().row();
        content.add(createDivider()).growX().height(1).pad(8, 0, 8, 0).row();

        for (LocationDTO loc : storeLocations) {
            Table row = new Table();
            row.left();

            row.add(ui.label(loc.clothingStoreId.name)).left().row();
            Label address = ui.mutedLabel(loc.address);
            address.setWrap(true);

            row.add(address).width(180).row();

            row.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectedStore = loc;
                    showEditStoreDialog(loc);
                }
            });

            content.add(row).growX().padBottom(10).row();
        }

        content.add(createDivider()).growX().height(1).pad(8, 0, 8, 0).row();

        TextButton addButton = ui.successButton("+ Add store");
        addButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showAddStoreDialog();
            }
        });

        content.add(addButton).width(180);

        storePanel = new Table();
        storePanel.setFillParent(true);
        storePanel.top().right().pad(16);
        storePanel.add(content);
        storePanel.setVisible(false);

        uiStage.addActor(storePanel);
    }

    private void createTimePanel() {
        Table content = ui.panel();

        content.add(ui.subtitle("Time simulation")).left().row();
        content.add(createDivider()).growX().height(1).pad(8, 0, 8, 0).row();


        TextField dayField = ui.textField(String.valueOf(selectedDay), "1 to 7");
        TextField hourField = ui.textField(String.valueOf(selectedHour), "0 to 23");

        content.add(ui.mutedLabel("Day (1 to 7)")).left().row();
        content.add(dayField).width(200).left().row();

        content.add(ui.mutedLabel("Hour (0 to 23)")).left().padTop(6).row();
        content.add(hourField).width(200).left().row();

        content.add(createDivider()).growX().height(1).pad(10, 0, 10, 0).row();

        TextButton apply = ui.successButton("Apply");
        TextButton close = ui.secondaryButton("Close");

        Table buttons = new Table();
        buttons.add(close).width(95);
        buttons.add(apply).width(95).padLeft(8);

        content.add(buttons);

        timePanel = new Table();
        timePanel.setFillParent(true);
        timePanel.center();
        timePanel.add(content).center();
        timePanel.setVisible(false);

        uiStage.addActor(timePanel);

        // APPLY
        apply.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedDay = MathUtils.clamp(
                    Integer.parseInt(dayField.getText()), 1, 7
                );

                selectedHour = MathUtils.clamp(
                    Integer.parseInt(hourField.getText()), 0, 23
                );

                simulationMode = true;
                updateOccupancyForSelectedTime();
                updateTimeStatusLabel();
                hideTimePanel();
            }
        });

        // CLOSE
        close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hideTimePanel();
            }
        });
    }

    private void toggleTimePanel() {
        if (timePanelVisible) {
            hideTimePanel();
        } else {
            showTimePanel();
        }
    }

    private void showTimePanel() {
        if (timePanel == null) createTimePanel();
        timePanel.setVisible(true);
        timePanelVisible = true;
    }

    private void hideTimePanel() {
        timePanel.setVisible(false);
        timePanelVisible = false;
    }




    private void toggleStorePanel() {
        storePanelVisible = !storePanelVisible;
        storePanel.setVisible(storePanelVisible);
    }

    private void showAddStoreDialog() {
        Table content = ui.panel();
        content.defaults().pad(4).left();

        content.add(ui.title("Add store")).row();
        content.add(createDivider()).growX().height(1).pad(8, 0, 10, 0).row();

        TextField name = createInput("");
        TextField website = createInput("");
        TextField address = createInput("");
        TextField city = createInput("");
        TextField country = createInput("");

        content.add(ui.mutedLabel("Name")).row();
        content.add(name).width(280).row();

        content.add(ui.mutedLabel("Website")).row();
        content.add(website).width(280).row();

        content.add(ui.mutedLabel("Address")).row();
        content.add(address).width(280).row();

        content.add(ui.mutedLabel("City")).row();
        content.add(city).width(280).row();

        content.add(ui.mutedLabel("Country")).row();
        content.add(country).width(280).row();

        TextButton cancel = ui.secondaryButton("Cancel");
        TextButton save = ui.successButton("Save");

        Table buttons = new Table();
        buttons.add(cancel).width(135);
        buttons.add(save).width(135).padLeft(8);

        content.add(buttons).padTop(12);

        // Center the dialog using a wrapper
        Table dialog = new Table();
        dialog.setFillParent(true);
        dialog.add(content);

        dialog.getColor().a = 0f;
        dialog.addAction(Actions.fadeIn(0.2f));
        uiStage.addActor(dialog);

        cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.addAction(Actions.sequence(
                    Actions.fadeOut(0.15f),
                    Actions.removeActor()
                ));
            }
        });

        save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ApiService.addNewStore(
                    name.getText(),
                    website.getText(),
                    address.getText(),
                    city.getText(),
                    country.getText(),
                    () -> {
                        ApiService.loadStoreLocations(data -> {
                            storeLocations = data;
                            createStorePanel();
                        });
                    }
                );

                dialog.addAction(Actions.sequence(
                    Actions.fadeOut(0.15f),
                    Actions.removeActor()
                ));
            }
        });
    }

    private void refreshMap() {

        Gdx.app.postRunnable(() -> {
            // no-op on purpose
        });
    }

    private void showEditStoreDialog(LocationDTO loc) {
        Table content = ui.panel();
        content.defaults().pad(4).left();

        content.add(ui.title("Edit store")).row();
        content.add(createDivider()).growX().height(1).pad(8, 0, 10, 0).row();

        TextField name = createInput(loc.clothingStoreId.name);
        TextField website = createInput(loc.clothingStoreId.website);
        TextField address = createInput(loc.address);
        TextField city = createInput(loc.city);
        TextField country = createInput(loc.country);

        content.add(ui.mutedLabel("Name")).row();
        content.add(name).width(280).row();

        content.add(ui.mutedLabel("Website")).row();
        content.add(website).width(280).row();

        content.add(ui.mutedLabel("Address")).row();
        content.add(address).width(280).row();

        content.add(ui.mutedLabel("City")).row();
        content.add(city).width(280).row();

        content.add(ui.mutedLabel("Country")).row();
        content.add(country).width(280).row();

        TextButton cancel = ui.secondaryButton("Cancel");
        TextButton save = ui.successButton("Save");

        Table buttons = new Table();
        buttons.add(cancel).width(135);
        buttons.add(save).width(135).padLeft(8);

        content.add(buttons).padTop(12);

        Table dialog = new Table();
        dialog.setFillParent(true);
        dialog.add(content);

        dialog.getColor().a = 0f;
        dialog.addAction(Actions.fadeIn(0.2f));
        uiStage.addActor(dialog);

        cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.addAction(Actions.sequence(
                    Actions.fadeOut(0.15f),
                    Actions.removeActor()
                ));
            }
        });

        save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ApiService.updateStore(
                    loc.clothingStoreId._id,
                    name.getText(),
                    website.getText(),
                    () -> {
                        ApiService.updateLocation(
                            loc._id,
                            address.getText(),
                            city.getText(),
                            country.getText(),
                            () -> {
                                ApiService.loadStoreLocations(data -> {
                                    storeLocations = data;
                                    createStorePanel();
                                });
                            }
                        );
                    }
                );

                dialog.addAction(Actions.sequence(
                    Actions.fadeOut(0.15f),
                    Actions.removeActor()
                ));
            }
        });
    }




    private Color getOccupancyColor(float fill) {
        fill = MathUtils.clamp(fill, 0f, 1f);


        if (fill < 0.5f) {
            // green -> orange
            float t = fill / 0.5f;
            return new Color(
                MathUtils.lerp(0f, 1f, t),   // R
                MathUtils.lerp(1f, 0.65f, t),// G
                0f,                          // B
                0.9f
            );
        } else {
            // orange -> red
            float t = (fill - 0.5f) / 0.5f;
            return new Color(
                1f,                          // R
                MathUtils.lerp(0.65f, 0f, t),// G
                0f,                          // B
                0.9f
            );
        }
    }


    private void loadLiveOccupancy() {
        ApiService.loadLatestOccupancy(data -> {

            occupancyMap.clear();

            // DB values
            for (StoreOccupancyDTO occ : data) {
                occupancyMap.put(occ.storeId, occ);

                if (!animatedOccupancy.containsKey(occ.storeId)) {
                    animatedOccupancy.put(occ.storeId, (float) occ.peopleCount);
                }
            }

            // fallback for missing stores
            for (LocationDTO loc : storeLocations) {
                if (!occupancyMap.containsKey(loc._id)) {

                    StoreOccupancyDTO fake = new StoreOccupancyDTO();
                    fake.storeId = loc._id;
                    fake.peopleCount = MathUtils.random(5, 80);

                    occupancyMap.put(fake.storeId, fake);

                    if (!animatedOccupancy.containsKey(fake.storeId)) {
                        animatedOccupancy.put(fake.storeId, (float) fake.peopleCount);
                    }
                }
            }
        });
    }


    private void updateOccupancyForSelectedTime() {
        occupancyMap.clear();

        for (LocationDTO loc : storeLocations) {
            StoreOccupancyDTO occ = new StoreOccupancyDTO();
            occ.storeId = loc._id;
            occ.peopleCount = simulateOccupancy(loc, selectedDay, selectedHour);
            occupancyMap.put(occ.storeId, occ);

            if (!animatedOccupancy.containsKey(loc._id)) {
                animatedOccupancy.put(loc._id, (float) occ.peopleCount);
            }
        }
    }


    private void updateOccupancyAnimation(float delta) {
        float speed = 6f;

        for (ObjectMap.Entry<String, StoreOccupancyDTO> e : occupancyMap.entries()) {
            float current =
                animatedOccupancy.get(e.key, (float)e.value.peopleCount);

            float target = e.value.peopleCount;

            current = MathUtils.lerp(current, target, delta * speed);

            if (Math.abs(current - target) < 0.1f) {
                current = target;
            }

            animatedOccupancy.put(e.key, current);
        }
    }



    private int simulateOccupancy(LocationDTO store, int day, int hour) {
        // osnovna zasedenost glede na tip dneva
        float base;

        boolean weekend = (day == 6 || day == 7); // sobota, nedelja

        if (weekend) {
            base = 0.6f;
        } else {
            base = 0.4f;
        }

        // vpliv ure
        float hourFactor;
        if (hour < 9) {
            hourFactor = 0.2f;
        } else if (hour < 12) {
            hourFactor = 0.6f;
        } else if (hour < 15) {
            hourFactor = 0.8f;
        } else if (hour < 18) {
            hourFactor = 1.0f; // peak
        } else if (hour < 21) {
            hourFactor = 0.7f;
        } else {
            hourFactor = 0.3f;
        }

        // unikaten “šum” na trgovino
        int seed = store._id.hashCode() + day * 31 + hour * 17;
        MathUtils.random.setSeed(seed);
        float noise = MathUtils.random(0.8f, 1.2f);

        float result = MAX_PEOPLE * base * hourFactor * noise;

        return MathUtils.clamp(Math.round(result), 0, MAX_PEOPLE);
    }

    private void startPlayMode() {
        playMode = true;
        simulationMode = true;

        if (playDay == 0 && playHour == 0) {
            playDay = selectedDay;
            playHour = selectedHour;
        }
        playTimer = 0f;

        selectedDay = playDay;
        selectedHour = playHour;

        updateOccupancyForSelectedTime();
        updateTimeStatusLabel();
    }

    private void advancePlayTime() {
        playHour++;

        if (playHour >= 24) {
            playHour = 0;
            playDay++;
        }

        int sliderValue = (playDay - 1) * 24 + playHour;
        timeSlider.setValue(sliderValue);
        updateTimeStatusLabel();


        // konec animacije (7 dni)
        if (playDay > 7) {
            stopPlayMode();
            return;
        }

        selectedDay = playDay;
        selectedHour = playHour;

        updateOccupancyForSelectedTime();
        updateTimeStatusLabel();
    }

    private void stopPlayMode() {
        playMode = false;
        updateTimeStatusLabel();

    }


    private Actor createDivider() {
        return ui.divider();
    }

    private TextField createInput(String text) {
        return ui.textField(text, "...");
    }

    private Integer getOccupancyForStore(LocationDTO store) {
        if (store == null) return null;

        StoreOccupancyDTO occ = occupancyMap.get(store._id);
        if (occ == null) return null;

        return occ.peopleCount;
    }


}
