package models.classes;


import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;


@XmlRootElement
public class User {
    private String email;
    private String password;
    private String name;
    private boolean isAdmin;
    private int userId;
    private int adminId;

    public User() {
    }

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public User(String iva_email, String iva_password, String iva_name, boolean iva_isAdmin, int iva_userId, int iva_adminId) {
        setEmail(iva_email);
        setPassword(iva_password);
        setName(iva_name);
        this.isAdmin = iva_isAdmin;
        setUserId(iva_userId);

        if (iva_isAdmin) {
            setAdminId(iva_adminId);
        }
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String iva_email) {

        this.email = iva_email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String iva_password) {

        this.password = iva_password;
    }

    public boolean getIsAdmin() {
        return this.isAdmin;
    }

    public void setIsAdmin(boolean iva_isAdmin) {
        this.isAdmin = iva_isAdmin;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int iva_userId) {

        this.userId = iva_userId;
    }

    public int getAdminId() {
        return this.adminId;
    }

    public void setAdminId(int iva_adminId) {
        this.adminId = iva_adminId;
    }

    public String getName() {
        return name;
    }

    public void setName(String iva_name) {

        this.name = iva_name;
    }

    @Override
    public String toString() {
        return "UserImpl{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", isAdmin=" + isAdmin +
                ", userId=" + userId +
                ", adminId=" + adminId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email) &&
                Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {

        return Objects.hash(email, password);
    }
}
