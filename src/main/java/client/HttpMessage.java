package client;

import tools.AlertWindows;

import static client.constants.HttpStatusCodes.GC_HTTP_BAD_REQUEST;
import static client.constants.HttpStatusCodes.GC_HTTP_CONFLICT;

/**
 * Created by Mesut on 07.02.2018.
 */
public class HttpMessage {
    private int httpStatus;

    private String userLoginStatus;
    private String passwordChangeStatus;
    private String userAddStatus;
    private String addAdminStatus;

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

    public String getPasswordChangeStatus() {
        return passwordChangeStatus;
    }

    public void setPasswordChangeStatus(String passwordChangeStatus) {
        this.passwordChangeStatus = passwordChangeStatus;
    }

    public String getAddAdminStatus() {
        return addAdminStatus;
    }

    public void setAddAdminStatus(String addAdminStatus) {
        this.addAdminStatus = addAdminStatus;
    }

    @Override
    public String toString() {
        return "HttpMessage{" +
                "userLoginStatus='" + userLoginStatus + '\'' +
                ", userAddStatus='" + userAddStatus + '\'' +
                '}';
    }
}
