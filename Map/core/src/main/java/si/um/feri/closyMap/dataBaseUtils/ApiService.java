package si.um.feri.closyMap.dataBaseUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.function.Consumer;

public class ApiService {
    public static void loadStoreLocations(Consumer<Array<LocationDTO>> callback) {

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl("http://localhost:5000/api/locations");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                String raw = response.getResultAsString();

                Gdx.app.log("API", "=== /api/locations RESPONSE ===");
                Gdx.app.log("API", raw);
                Gdx.app.log("API", "================================");


                Json json = new Json();
                json.setIgnoreUnknownFields(true);

                Array<LocationDTO> data =
                    json.fromJson(Array.class, LocationDTO.class, raw);

                Gdx.app.postRunnable(() -> callback.accept(data));
            }

            @Override public void failed(Throwable t) { t.printStackTrace(); }
            @Override public void cancelled() {}
        });
    }

    public static void loadUsers(Consumer<Array<UserDTO>> callback) {

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl("http://localhost:5000/api/users");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                Json json = new Json();
                json.setIgnoreUnknownFields(true);
                UsersResponseDTO dto =
                    json.fromJson(UsersResponseDTO.class, response.getResultAsString());

                Gdx.app.postRunnable(() -> callback.accept(dto.data));
            }

            @Override public void failed(Throwable t) { t.printStackTrace(); }
            @Override public void cancelled() {}
        });
    }
    public static void createStore(
        String name,
        String website,
        Consumer<String> onStoreCreated
    ) {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        CreateStoreRequest body = new CreateStoreRequest();
        body.name = name;
        body.website = website;

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl("http://localhost:5000/api/stores");
        request.setHeader("Content-Type", "application/json");
        request.setContent(json.toJson(body));

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                JsonValue result = json.fromJson(null, response.getResultAsString());
                String storeId = result.getString("_id");

                Gdx.app.postRunnable(() -> onStoreCreated.accept(storeId));
            }

            @Override public void failed(Throwable t) { t.printStackTrace(); }
            @Override public void cancelled() {}
        });
    }
    public static void createLocation(
        String storeId,
        String address,
        String city,
        String country,
        Runnable onDone
    ) {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        CreateLocationRequest body = new CreateLocationRequest();
        body.clothingStoreId = storeId;
        body.address = address;
        body.city = city;
        body.country = country;

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl("http://localhost:5000/api/locations");
        request.setHeader("Content-Type", "application/json");
        request.setContent(json.toJson(body));

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                Gdx.app.postRunnable(onDone);
            }

            @Override public void failed(Throwable t) { t.printStackTrace(); }
            @Override public void cancelled() {}
        });
    }
    public static void addNewStore(
        String name,
        String website,
        String address,
        String city,
        String country,
        Runnable onFinished
    ) {
        createStore(name, website, storeId -> {

            createLocation(
                storeId,
                address,
                city,
                country,
                onFinished
            );
        });
    }

    public static void updateStore(
        String storeId,
        String name,
        String website,
        Runnable onDone
    ) {
        Json json = new Json();

        Net.HttpRequest request =
            new Net.HttpRequest(Net.HttpMethods.PUT);

        request.setUrl("http://localhost:5000/api/stores/" + storeId);
        request.setHeader("Content-Type", "application/json");

        String body =
            "{"
                + "\"name\":\"" + name + "\","
                + "\"website\":\"" + website + "\""
                + "}";

        request.setContent(body);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override public void handleHttpResponse(Net.HttpResponse response) {
                Gdx.app.postRunnable(onDone);
            }
            @Override public void failed(Throwable t) { t.printStackTrace(); }
            @Override public void cancelled() {}
        });
    }

    public static void updateLocation(
        String locationId,
        String address,
        String city,
        String country,
        Runnable onDone
    ) {
        System.out.println("[API] PUT /locations " + locationId);
        System.out.println("[API] BODY: " + address + ", " + city + ", " + country);
        Json json = new Json();

        Net.HttpRequest request =
            new Net.HttpRequest(Net.HttpMethods.PUT);

        request.setUrl("http://localhost:5000/api/locations/" + locationId);
        request.setHeader("Content-Type", "application/json");

        String body =
            "{"
                + "\"address\":\"" + address + "\","
                + "\"city\":\"" + city + "\","
                + "\"country\":\"" + country + "\""
                + "}";

        request.setContent(body);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override public void handleHttpResponse(Net.HttpResponse response) {
                Gdx.app.postRunnable(onDone);
            }
            @Override public void failed(Throwable t) { t.printStackTrace(); }
            @Override public void cancelled() {}
        });
    }

    public static void loadLatestOccupancy(
        Consumer<Array<StoreOccupancyDTO>> callback
    ) {
        Net.HttpRequest request =
            new Net.HttpRequest(Net.HttpMethods.GET);

        request.setUrl("http://localhost:5000/api/events/latest");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                Json json = new Json();
                json.setIgnoreUnknownFields(true);

                Array<StoreOccupancyDTO> data =
                    json.fromJson(
                        Array.class,
                        StoreOccupancyDTO.class,
                        response.getResultAsString()
                    );

                Gdx.app.postRunnable(() -> callback.accept(data));
            }

            @Override public void failed(Throwable t) { t.printStackTrace(); }
            @Override public void cancelled() {}
        });
    }





}
