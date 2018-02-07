package builder;

import models.classes.UserImpl;
import models.interfaces.User;

/**
 * Created by Mesut on 07.02.2018.
 */
public class ModelObjectBuilder {
    public static User getUserObject() {
        return new UserImpl();
    }

    public static User getUserObject(String email, String password, String name) {
        return new UserImpl(email, password, name);
    }
}
