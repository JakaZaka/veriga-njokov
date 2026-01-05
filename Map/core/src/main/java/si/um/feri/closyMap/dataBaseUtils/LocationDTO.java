package si.um.feri.closyMap.dataBaseUtils;

public class LocationDTO {
    public String _id;
    public String address;
    public String city;
    public String country;

    public Coordinates coordinates;
    public StoreDTO clothingStoreId;

    public static class Coordinates {
        public String type;
        public float[] coordinates;
    }
}
