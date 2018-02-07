package models.classes;

public class UserImpl {
    private String email;
    private String password;
    private String name;

public UserImpl(){

}

    public UserImpl(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }
}