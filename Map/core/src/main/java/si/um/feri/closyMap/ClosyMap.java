package si.um.feri.closyMap;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;


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

    private BitmapFont font;

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
    private VisDialog infoDialog;

    private VisTable storePanel;
    private boolean storePanelVisible = false;

    private ObjectMap<String, StoreOccupancyDTO> occupancyMap =
        new ObjectMap<>();

    private float occupancyTimer = 0f;

    private static final int MAX_PEOPLE = 70;


    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        userIcon = new Texture("wardrobe.png");
        storeIcon = new Texture("store.png");
        currentLocationIcon = new Texture("currentLocation.png");
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("font/textFont.fnt"));

        VisUI.load(VisUI.SkinScale.X1);

        uiStage = new Stage(
            new ExtendViewport(1280, 720)
        );
        createToggleStoreButton();

        ApiService.loadStoreLocations(data -> {
            storeLocations = data;

            generateFakeOccupancy();

            if (storePanel != null) storePanel.remove();
            createStorePanel();
        });
        ApiService.loadUsers(data -> users = data);

//        ApiService.loadStoreOccupancy(data -> {
//            occupancyMap.clear();
//
//            for (StoreOccupancyDTO occ : data) {
//                occupancyMap.put(occ.storeId, occ);
//            }
//        });





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

                if (infoDialog != null) {
                    infoDialog.remove();
                    infoDialog = null;
                }

                popupPos.set(world.x + 20, world.y + 20);
                popupAlpha = 0f;


                //selectedStore = null;
                //selectedUser = null;

                float clickRadius = 45f * camera.zoom;

                if (storePanelVisible) {
                    toggleStorePanel();
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


//        camera.position.set(
//            Constants.MAP_WIDTH / 2f,
//            Constants.MAP_HEIGHT / 2f,
//            0
//        );

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

    private void updateTileZoomIfNeeded() {
        int desiredZoom = currentTileZoom;

        if (camera.zoom < 0.6f) {
            desiredZoom = 16;
        } else if (camera.zoom < 1.2f) {
            desiredZoom = 15;
        } else {
            desiredZoom = 14;
        }

        if (desiredZoom != currentTileZoom) {
            reloadMapTiles(desiredZoom);
            currentTileZoom = desiredZoom;
        }
    }


    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        handleInput();

        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        popupAlpha = Math.min(1f, popupAlpha + Gdx.graphics.getDeltaTime() * 6f);
        //updateTileZoomIfNeeded();

        occupancyTimer += Gdx.graphics.getDeltaTime();

        if (occupancyTimer > 20f) {
            generateFakeOccupancy();
            occupancyTimer = 0f;
        }


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

            drawOccupancyRing(pos, occ.peopleCount);
        }

        shapeRenderer.end();
        batch.begin();
        float baseIconSize = 31f;
        float scale = MathUtils.clamp(1f / camera.zoom, 0.01f, 1.4f);
        float iconSize = baseIconSize / scale;

        Vector2 marker = MapRasterTiles.getPixelPosition(
            MARKER_GEOLOCATION.lat,
            MARKER_GEOLOCATION.lng,
            beginTile.x,
            beginTile.y
        );


        batch.draw(
            currentLocationIcon,
            marker.x - iconSize / 2,
            marker.y - iconSize / 2,
            iconSize,
            iconSize
        );


        // STORES
        for (LocationDTO loc : storeLocations) {
            if (loc.coordinates == null || loc.coordinates.coordinates == null) {
                System.out.println("❌ STORE WITHOUT COORDINATES: " + loc._id);
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
        //float sweepAngle = 360f * fill;

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
        if (infoDialog != null) {
            infoDialog.remove();
        }

        infoDialog = new VisDialog("Location info");
        infoDialog.setModal(false);
        infoDialog.setMovable(true);
        infoDialog.setResizable(false);

        Table content = infoDialog.getContentTable();
        content.defaults().pad(6).left();

        if (selectedStore != null) {
            content.add(new VisLabel("Store:")).row();
            content.add(new VisLabel(selectedStore.clothingStoreId.name)).row();

            content.add(new VisLabel("Website:")).row();
            content.add(new VisLabel(selectedStore.clothingStoreId.website)).row();

            content.add(new VisLabel("Address:")).row();
            content.add(new VisLabel(selectedStore.address)).row();
        }

        if (selectedUser != null) {
            content.add(new VisLabel("User:")).row();
            content.add(new VisLabel(selectedUser.username)).row();

            content.add(new VisLabel("Email:")).row();
            content.add(new VisLabel(selectedUser.email)).row();

            if (selectedUser.location != null) {
                content.add(new VisLabel("Address:")).row();
                content.add(new VisLabel(selectedUser.location.address)).row();
            }
        }

        infoDialog.button("Close");
        infoDialog.pack();
        infoDialog.show(uiStage);


        infoDialog.setPosition(20, 20);
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
    public boolean pan(float x, float y, float deltaX, float deltaY) {
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

        if (storePanel == null) return;

        Actor content = storePanel.getChildren().first();

        if (storePanelVisible) {
            content.setPosition(width - content.getWidth(), height - content.getHeight());
        } else {
            content.setPosition(width, height - content.getHeight());
        }
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
        VisTextButton toggleButton = new VisTextButton("Stores");
        Table root = new Table();
        root.setFillParent(true);
        root.top().left().pad(16);

        root.add(toggleButton).width(120).height(40);

        toggleButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleStorePanel();
            }
        });

        uiStage.addActor(root);
    }

    private void createStorePanel() {
        // Root overlay (NE IMA backgrounda!)
        storePanel = new VisTable();
        storePanel.setFillParent(true);
        storePanel.setVisible(false);
        storePanel.top().right();

        float panelWidth = 220;

        // Pravi panel z backgroundom
        VisTable content = new VisTable(true);
        content.setBackground("window");
        content.top().pad(10);
        content.setWidth(panelWidth);

        content.setTransform(true);
        float stageWidth = uiStage.getViewport().getWorldWidth();
        float stageHeight = uiStage.getViewport().getWorldHeight();
        float panelHeight = content.getPrefHeight();

        content.pack();
        content.setPosition(stageWidth, stageHeight - panelHeight);


        VisLabel title = new VisLabel("Stores");
        content.add(title).left().row();
        content.addSeparator().pad(6).row();

        for (LocationDTO loc : storeLocations) {
            VisTable row = new VisTable();
            row.left().pad(6);

            VisLabel name = new VisLabel(loc.clothingStoreId.name);
            name.setFontScale(1.05f);

            VisLabel address = new VisLabel(loc.address);
            address.setColor(Color.LIGHT_GRAY);
            address.setWrap(true);

            row.add(name).left().row();
            row.add(address).width(panelWidth - 40).left().row();

            row.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectedStore = loc;
                    showEditStoreDialog(loc);
                }
            });

            content.add(row).growX().padBottom(8).row();
        }

        content.addSeparator().pad(10).row();

        VisTextButton addButton = new VisTextButton("+ Add store");
        addButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showAddStoreDialog();
            }
        });

        content.add(addButton).growX();

        // content dodamo v root
        storePanel.add(content).top().right();

        uiStage.addActor(storePanel);
    }

    private void toggleStorePanel() {
        Actor content = storePanel.getChildren().first();

        float panelWidth = content.getWidth();
        float panelHeight = content.getHeight();

        float stageWidth = uiStage.getViewport().getWorldWidth();
        float stageHeight = uiStage.getViewport().getWorldHeight();

        float targetY = stageHeight - panelHeight;

        content.clearActions();

        if (storePanelVisible) {
            // zapiranje – ven zgoraj desno
            content.addAction(
                Actions.moveTo(stageWidth, targetY, 0.25f)
            );
        } else {
            // odpiranje – zgoraj desno
            storePanel.setVisible(true);
            content.addAction(
                Actions.moveTo(stageWidth - panelWidth, targetY, 0.25f)
            );
        }

        storePanelVisible = !storePanelVisible;
    }

    private void showAddStoreDialog() {

        VisTextField nameField = new VisTextField();
        VisTextField websiteField = new VisTextField();
        VisTextField addressField = new VisTextField();
        VisTextField cityField = new VisTextField();
        VisTextField countryField = new VisTextField();

        VisDialog dialog = new VisDialog("Add store") {

            @Override
            protected void result(Object object) {
                if (Boolean.TRUE.equals(object)) {

                    ApiService.addNewStore(
                        nameField.getText(),
                        websiteField.getText(),
                        addressField.getText(),
                        cityField.getText(),
                        countryField.getText(),
                        () -> {
                            ApiService.loadStoreLocations(data -> {
                                storeLocations = data;

                                if (storePanel != null) storePanel.remove();
                                createStorePanel();

                                refreshMap();
                            });
                        }
                    );
                }
            }
        };



        Table t = dialog.getContentTable();
        t.defaults().pad(4).left();

        t.add("Name"); t.add(nameField).growX().row();
        t.add("Website"); t.add(websiteField).growX().row();
        t.add("Address"); t.add(addressField).growX().row();
        t.add("City"); t.add(cityField).growX().row();
        t.add("Country"); t.add(countryField).growX().row();

        dialog.button("Cancel", false);
        dialog.button("Save", true);

        dialog.show(uiStage);
    }

    private void refreshMap() {
        // libGDX renders every frame automatically.
        // Updating storeLocations is enough.
        // This method exists for clarity + future extensions.
        Gdx.app.postRunnable(() -> {
            // no-op on purpose
        });
    }

    private void showEditStoreDialog(LocationDTO loc) {
        VisTextField nameField =
            new VisTextField(loc.clothingStoreId.name);

        VisTextField websiteField =
            new VisTextField(loc.clothingStoreId.website);

        VisTextField addressField =
            new VisTextField(loc.address);

        VisTextField cityField =
            new VisTextField(loc.city);

        VisTextField countryField =
            new VisTextField(loc.country);

        VisDialog dialog = new VisDialog("Edit store") {

            @Override
            protected void result(Object object) {
                if (!Boolean.TRUE.equals(object)) return;

                // update ClothingStore
                ApiService.updateStore(
                    loc.clothingStoreId._id,
                    nameField.getText(),
                    websiteField.getText(),
                    () -> {

                        // update Location
                        ApiService.updateLocation(
                            loc._id,
                            addressField.getText(),
                            cityField.getText(),
                            countryField.getText(),
                            () -> {

                                // reload everything
                                ApiService.loadStoreLocations(data -> {
                                    storeLocations = data;

                                    if (storePanel != null)
                                        storePanel.remove();

                                    createStorePanel();
                                    storePanelVisible = true;
                                });
                            }
                        );
                    }
                );
            }
        };

        Table t = dialog.getContentTable();
        t.defaults().pad(4).left();

        t.add("Name");     t.add(nameField).growX().row();
        t.add("Website");  t.add(websiteField).growX().row();
        t.add("Address");  t.add(addressField).growX().row();
        t.add("City");     t.add(cityField).growX().row();
        t.add("Country");  t.add(countryField).growX().row();

        dialog.button("Cancel", false);
        dialog.button("Save", true);

        dialog.show(uiStage);
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


    private void generateFakeOccupancy() {
        occupancyMap.clear();

        for (LocationDTO loc : storeLocations) {
            StoreOccupancyDTO occ = new StoreOccupancyDTO();
            occ.storeId = loc._id;

            // realistične fake vrednosti
            occ.peopleCount = MathUtils.random(5, 80);

            occupancyMap.put(occ.storeId, occ);
        }
    }
    

}
