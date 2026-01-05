package si.um.feri.closyMap;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
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


import java.io.IOException;

import si.um.feri.closyMap.dataBaseUtils.ApiService;
import si.um.feri.closyMap.dataBaseUtils.LocationDTO;
import si.um.feri.closyMap.dataBaseUtils.UserDTO;
import si.um.feri.closyMap.utils.Constants;
import si.um.feri.closyMap.utils.Geolocation;
import si.um.feri.closyMap.utils.MapRasterTiles;
import si.um.feri.closyMap.utils.ZoomXY;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ClosyMap extends ApplicationAdapter implements GestureDetector.GestureListener {
    private Array<LocationDTO> storeLocations = new Array<>();
    private Array<UserDTO> users = new Array<>();

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

    private OrthographicCamera uiCamera;


    private Texture[] mapTiles;
    private ZoomXY beginTile;   // top left tile

    private Vector2 popupPos = new Vector2();

    private float popupAlpha = 0f;


    // center geolocation
    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.557314, 15.637771);

    // test marker
    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        userIcon = new Texture("wardrobe.png");
        storeIcon = new Texture("store.png");
        currentLocationIcon = new Texture("currentLocation.png");
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("font/textFont.fnt"));


        ApiService.loadStoreLocations(data -> storeLocations = data);
        ApiService.loadUsers(data -> users = data);


        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.viewportWidth = Constants.MAP_WIDTH / 2f;
        camera.viewportHeight = Constants.MAP_HEIGHT / 2f;
        camera.zoom = 2f;
        camera.update();

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());


        touchPosition = new Vector3();

        InputMultiplexer multiplexer = new InputMultiplexer();

        multiplexer.addProcessor(new GestureDetector(this));

        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(mouse);

                float oldZoom = camera.zoom;
                camera.zoom += amountY * 0.1f;
                camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 2f);

                float ratio = camera.zoom / oldZoom;
                camera.position.x += (mouse.x - camera.position.x) * (1 - ratio);
                camera.position.y += (mouse.y - camera.position.y) * (1 - ratio);

                return true;
            }
        });

        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {

                Vector3 world = new Vector3(screenX, screenY, 0);
                camera.unproject(world);
                popupPos.set(world.x + 20, world.y + 20);
                popupAlpha = 0f;


                selectedStore = null;
                selectedUser = null;

                float clickRadius = 45f * camera.zoom;

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
                        selectedUser = user;
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
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        handleInput();

        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        popupAlpha = Math.min(1f, popupAlpha + Gdx.graphics.getDeltaTime() * 6f);


        drawMarkers();
        drawPopup();
//        drawStores();
//        drawUsers();
    }

    private void drawMarkers() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        float iconSize = 82f;

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

    private void drawPopup() {
        if (selectedStore == null && selectedUser == null) return;

        float width =900;
        float height = 250;
        float padding = 25;

        float margin = 20f;

        float x = margin;
        float y = height + margin;



        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0.84f, 0.8f, 0.95f*popupAlpha);
        shapeRenderer.rect(x, y - height, width, height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 1f, 1f, 0.4f*popupAlpha);
        shapeRenderer.rect(x, y - height, width, height);
        shapeRenderer.end();

        font.setColor(1f, 1f, 1f, popupAlpha);
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        float textY = y - padding;

        if (selectedStore != null) {

            font.draw(batch, selectedStore.clothingStoreId.name, x + padding, textY);
            textY -= 47;

            font.draw(batch, "Website:", x + padding, textY);
            textY -= 45;

            font.draw(batch, selectedStore.clothingStoreId.website, x + padding, textY);
            textY -= 45;

            font.draw(batch, "Address:", x + padding, textY);
            textY -= 45;

            font.draw(batch, selectedStore.address, x + padding, textY);
        }

        if (selectedUser != null) {
            font.draw(batch, selectedUser.username, x + padding, textY);
            textY -= 47;

            font.draw(batch, "Email: " + selectedUser.email, x + padding, textY);
            textY -= 45;

            if (selectedUser.location != null) {
                font.draw(batch, "Address: " + selectedUser.location.address, x + padding, textY);
            }
        }

        batch.end();
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
        uiCamera.setToOrtho(false, width, height);
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



        camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 2f);

        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        camera.position.x = MathUtils.clamp(camera.position.x, effectiveViewportWidth / 2f, Constants.MAP_WIDTH - effectiveViewportWidth / 2f);
        camera.position.y = MathUtils.clamp(camera.position.y, effectiveViewportHeight / 2f, Constants.MAP_HEIGHT - effectiveViewportHeight / 2f);
    }

//    private void drawStores() {
//        shapeRenderer.setProjectionMatrix(camera.combined);
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//
//        shapeRenderer.setColor(Color.BLUE);
//
//        for (LocationDTO loc : storeLocations) {
//            float lon = loc.coordinates.coordinates[0];
//            float lat = loc.coordinates.coordinates[1];
//
//            Vector2 pos = MapRasterTiles.getPixelPosition(lat, lon, beginTile.x, beginTile.y);
//            shapeRenderer.circle(pos.x, pos.y, 8);
//        }
//
//        shapeRenderer.end();
//    }
//
//    private void drawUsers() {
//        shapeRenderer.setProjectionMatrix(camera.combined);
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//
//        shapeRenderer.setColor(Color.GREEN);
//
//        for (UserDTO user : users) {
//            if (user.location == null || user.location.coordinates == null) continue;
//
//            float lon = user.location.coordinates.coordinates[0];
//            float lat = user.location.coordinates.coordinates[1];
//
//            Vector2 pos = MapRasterTiles.getPixelPosition(lat, lon, beginTile.x, beginTile.y);
//            shapeRenderer.circle(pos.x, pos.y, 5);
//        }
//
//        shapeRenderer.end();
//    }


}
