package si.um.feri.closyMap.dataBaseUtils;

public class UserDTO {
    public String _id;
    public String username;

    public String email;

    public String avatar;
    public String role;

    public ContactInfoDTO contactInfo;

    public String createdAt;

    public String updatedAt;
    public Location location;

    public static class Location {

        public String address;
        public String city;
        public String country;
        public Coordinates coordinates;
    }

    public static class Coordinates {
        public String type;
        public float[] coordinates;
    }
}
