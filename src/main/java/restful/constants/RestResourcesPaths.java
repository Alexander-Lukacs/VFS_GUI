package restful.constants;

public abstract class RestResourcesPaths {
    public static final String GC_REST_ADD_USER_PATH = "/user/addNewUser";
    public static final String GC_REST_LOGIN_USER_PATH = "/user/auth/login";
    public static final String GC_REST_CHANGE_PASSWORD_PATH = "/user/auth/changePassword";
    public static final String GC_REST_GET_ALL_USERS_PATH = "/user/auth/getAllUser";
    public static final String GC_REST_ADD_ADMIN_PATH = "/admin/adminAuth/addNewAdmin";
    public static final String GC_REST_UNREGISTER_CLIENT = "/user/auth/unregisterIp";

    public static final String GC_REST_ADD_NEW_SHARED_DIRECTORY = "sharedDirectory/auth/addNewSharedDirectory/";
    public static final String GC_REST_ADD_NEW_MEMBER_TO_SHARED_DIR = "sharedDirectory/auth/addNewMemberToSharedDirectory/";
    public static final String GC_REST_DELETE_SHARED_DIRECTORY = "/sharedDirectory/auth/deleteSharedDirectory";
    public static final String GC_REST_REMOVE_MEMBER_FROM_SHARED_DIR = "sharedDirectory/auth/removeMemberFromSharedDirectory/";
}
