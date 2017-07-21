package com.ustadmobile.nanolrs.core.manager;
/**
 * Created by varuna on 7/10/2017.
 */

import com.ustadmobile.nanolrs.core.model.Node;

import java.sql.SQLException;
import java.util.List;

public interface NodeManager extends NanoLrsManager {

    List<Node> getNodeByRoleName(Object dbContext, String role_name)
            throws SQLException;
}
