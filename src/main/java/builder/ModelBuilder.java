package builder;

import models.classes.UserImpl;
import models.interfaces.User;

/**
 * Created by Mesut on 07.02.2018.
 */
public class ModelBuilder {
    public static User getUserObject() {
        return new UserImpl();
    }
}
