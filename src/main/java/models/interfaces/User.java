package models.interfaces;

public interface User {
    void setEmail(String iva_email);
    void setPassword(String iva_password);
    void setIsAdmin(boolean iva_isAdmin);
    void setUserId(int iva_userId);
    void setAdminId(int iva_adminId);
    String getEmail();
    String getPassword();
    boolean getIsAdmin();
    int getUserId();
    int getAdminId();
    String getName();
    void setName(String gva_name);
    boolean equals(Object o);
    int hashCode();
}