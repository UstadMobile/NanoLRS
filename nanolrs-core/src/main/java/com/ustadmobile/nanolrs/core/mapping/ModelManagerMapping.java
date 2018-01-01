package com.ustadmobile.nanolrs.core.mapping;

import java.util.HashMap;

import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.XapiActivity;
import com.ustadmobile.nanolrs.core.manager.XapiActivityManager;
import com.ustadmobile.nanolrs.core.model.SyncStatus;
import com.ustadmobile.nanolrs.core.manager.SyncStatusManager;
import com.ustadmobile.nanolrs.core.model.UserCustomFields;
import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatement;
import com.ustadmobile.nanolrs.core.manager.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiVerb;
import com.ustadmobile.nanolrs.core.manager.XapiVerbManager;
import com.ustadmobile.nanolrs.core.model.ChangeSeq;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.model.NodeSyncStatus;
import com.ustadmobile.nanolrs.core.manager.NodeSyncStatusManager;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.model.XapiState;
import com.ustadmobile.nanolrs.core.manager.XapiStateManager;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;

public class ModelManagerMapping {

	public static HashMap<String, Class> proxyNameToClassMap = new HashMap<>();
	public static HashMap<Class, Class> proxyClassToManagerMap = new HashMap<>();
	static{

		proxyNameToClassMap.put(Node.class.getName(),Node.class);

		proxyClassToManagerMap.put(Node.class,NodeManager.class);

		proxyNameToClassMap.put(User.class.getName(),User.class);

		proxyClassToManagerMap.put(User.class,UserManager.class);

		proxyNameToClassMap.put(XapiActivity.class.getName(),XapiActivity.class);

		proxyClassToManagerMap.put(XapiActivity.class,XapiActivityManager.class);

		proxyNameToClassMap.put(SyncStatus.class.getName(),SyncStatus.class);

		proxyClassToManagerMap.put(SyncStatus.class,SyncStatusManager.class);

		proxyNameToClassMap.put(UserCustomFields.class.getName(),UserCustomFields.class);

		proxyClassToManagerMap.put(UserCustomFields.class,UserCustomFieldsManager.class);

		proxyNameToClassMap.put(XapiForwardingStatement.class.getName(),XapiForwardingStatement.class);

		proxyClassToManagerMap.put(XapiForwardingStatement.class,XapiForwardingStatementManager.class);

		proxyNameToClassMap.put(XapiVerb.class.getName(),XapiVerb.class);

		proxyClassToManagerMap.put(XapiVerb.class,XapiVerbManager.class);

		proxyNameToClassMap.put(ChangeSeq.class.getName(),ChangeSeq.class);

		proxyClassToManagerMap.put(ChangeSeq.class,ChangeSeqManager.class);

		proxyNameToClassMap.put(NodeSyncStatus.class.getName(),NodeSyncStatus.class);

		proxyClassToManagerMap.put(NodeSyncStatus.class,NodeSyncStatusManager.class);

		proxyNameToClassMap.put(XapiAgent.class.getName(),XapiAgent.class);

		proxyClassToManagerMap.put(XapiAgent.class,XapiAgentManager.class);

		proxyNameToClassMap.put(XapiState.class.getName(),XapiState.class);

		proxyClassToManagerMap.put(XapiState.class,XapiStateManager.class);

		proxyNameToClassMap.put(XapiStatement.class.getName(),XapiStatement.class);

		proxyClassToManagerMap.put(XapiStatement.class,XapiStatementManager.class);

	}
	public static Class[] SYNCABLE_ENTITIES = new Class[]{
		User.class, XapiActivity.class, UserCustomFields.class, XapiForwardingStatement.class, XapiVerb.class, XapiAgent.class, XapiState.class, XapiStatement.class
	};
}