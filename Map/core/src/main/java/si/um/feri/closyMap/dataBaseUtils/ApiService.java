package si.um.feri.closyMap.dataBaseUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import java.util.function.Consumer;

public class ApiService {
    public static void loadStoreLocations(Consumer<Array<LocationDTO>> callback) {

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl("http://localhost:5000/api/locations");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                Json json = new Json();
                json.setIgnoreUnknownFields(true);
                Array<LocationDTO> data =
                    json.fromJson(Array.class, LocationDTO.class, response.getResultAsString());

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
}
