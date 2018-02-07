package client;

/**
 * Created by Mesut on 07.02.2018.
 */
public class HttpMessage {
    private int httpStatus;
    private String userLoginStatus;

    private String userAddStatus;


    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getUserAddStatus() {
        return userAddStatus;
    }

    public void setUserAddStatus(String userAddStatus) {
        this.userAddStatus = userAddStatus;
    }

    public String getUserLoginStatus() {
        return userLoginStatus;
    }

    public void setUserLoginStatus(String userLoginStatus) {
        this.userLoginStatus = userLoginStatus;
    }

    @Override
    public String toString() {
        return "HttpMessage{" +
                "userLoginStatus='" + userLoginStatus + '\'' +
                ", userAddStatus='" + userAddStatus + '\'' +
                '}';
    }
}
