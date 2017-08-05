package com.ustadmobile.nanolrs.core;

/**
 * Created by varuna on 6/22/2017.
 */

import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManagerSyncable;
import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiActivityManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiStateManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.manager.XapiVerbManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.UserCustomFields;
import com.ustadmobile.nanolrs.core.model.XapiActivity;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.model.XapiState;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.model.XapiVerb;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;

public class ProxyJsonSerializer {

    /**
     * When object is serialized to json this field will be added. When it is deserialized this field
     * is used to determine which class to instantiate
     */
    public static final String PROXY_CLASS_JSON_FIELD = "pCls";

    /**
     * Map of entity names to the proxy class
     */
    private static HashMap<String, Class> proxyNameToClassMap = new HashMap<>();

    private static HashMap<Class, Class> proxyClassToManagerMap = new HashMap<>();

    //TODO: Find a central place for this and other mappings..
    static {
        proxyNameToClassMap.put(User.class.getName(), User.class);
        proxyClassToManagerMap.put(User.class, UserManager.class);

        proxyNameToClassMap.put(UserCustomFields.class.getName(), UserCustomFields.class);
        proxyClassToManagerMap.put(UserCustomFields.class, UserCustomFieldsManager.class);

        proxyNameToClassMap.put(XapiVerb.class.getName(), XapiVerb.class);
        proxyClassToManagerMap.put(XapiVerb.class, XapiVerbManager.class);

        proxyNameToClassMap.put(XapiState.class.getName(), XapiState.class);
        proxyClassToManagerMap.put(XapiState.class, XapiStateManager.class);

        proxyNameToClassMap.put(XapiAgent.class.getName(), XapiAgent.class);
        proxyClassToManagerMap.put(XapiAgent.class, XapiAgentManager.class);

        proxyNameToClassMap.put(XapiActivity.class.getName(), XapiActivity.class);
        proxyClassToManagerMap.put(XapiActivity.class, XapiActivityManager.class);

        proxyNameToClassMap.put(XapiStatement.class.getName(), XapiStatement.class);
        proxyClassToManagerMap.put(XapiStatement.class, XapiStatementManager.class);

    }

    /**
     * Converts an Entity Proxy Object to JSON. Serialises the Object.
     * To be used in SyncAPI
     * @param object    The object to be seralised to JSON
     * @param proxyClass    The proxy class of this object
     * @return
     */
    public static JSONObject toJson(NanoLrsModel object, Class proxyClass) {
        Method[] methods = proxyClass.getMethods();
        JSONObject returnVal = new JSONObject();
        for(int i = 0; i < methods.length; i++){
            String methodName = methods[i].getName();
            int prefixLen = 0;
            if(methodName.startsWith("is"))
                prefixLen = 2;
            else if(methodName.startsWith("get"))
                prefixLen = 3;

            if(prefixLen != 0) {
                String propName = Character.toLowerCase(methodName.charAt(prefixLen))
                        + methodName.substring(prefixLen +1);

                Method valGetterMethod = methods[i];
                Object invocationTarget = object;

                if(!(methods[i].getReturnType().isPrimitive() || methods[i].getReturnType().equals(String.class))) {
                    try {
                        invocationTarget = methods[i].invoke(object);
                        Class relatedClass = methods[i].getReturnType();
                        Method[] relatedClassMethods = relatedClass.getMethods();
                        valGetterMethod = null;
                        for(Method relatedMethod : relatedClassMethods) {
                            if(relatedMethod.isAnnotationPresent(PrimaryKeyAnnotationClass.class)) {
                                valGetterMethod = relatedMethod;
                                break;
                            }
                        }

                        if(valGetterMethod == null) {
                            throw new RuntimeException("No Primary Key Annotation Class");
                        }
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    if(invocationTarget != null) {
                        Object propValue = valGetterMethod.invoke(invocationTarget);
                        if(propValue != null) {
                            returnVal.put(propName, propValue);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        returnVal.put(PROXY_CLASS_JSON_FIELD, proxyClass.getName());
        return returnVal;
    }

    /**
     * Convert JSON to Entity Object
     * @param entityJSON
     * @return
     */
    public static NanoLrsModel toEntity(JSONObject entityJSON, Object context ){
        String proxyClassName = entityJSON.getString(PROXY_CLASS_JSON_FIELD);
        Class proxyClass = proxyNameToClassMap.get(proxyClassName);
        Class managerClass = proxyClassToManagerMap.get(proxyClass);
        NanoLrsManager manager = PersistenceManager.getInstance().getManager(managerClass);
        //Object context = PlatformTestUtil.getContext();
        NanoLrsModel newObj = null;
        try {
            newObj = manager.makeNew();

            Iterator<String> keys = entityJSON.keys();
            String keyName;
            while(keys.hasNext()) {
                keyName = keys.next();
                if(keyName.equals(PROXY_CLASS_JSON_FIELD))
                    continue;

                //Check if a relationship or primitive

                Object value = entityJSON.get(keyName);
                Class valueType = value.getClass();
                String methodPropName = Character.toUpperCase(keyName.charAt(0)) +
                        keyName.substring(1);
                String methodName = "set" + methodPropName;

                String getterMethodName = (value instanceof Boolean ? "is" : "get") + methodPropName;
                Method getterMethod = proxyClass.getMethod(getterMethodName);

                if(!(getterMethod.getReturnType().isPrimitive() ||
                        getterMethod.getReturnType().equals(String.class)))
                {
                    System.out.println("RELATIONSHIP!");

                    Class relatedProxyClass = proxyNameToClassMap.get(getterMethod.getReturnType().getName());
                    Class realtedManagerClass = proxyClassToManagerMap.get(relatedProxyClass);

                    //Get the manager of the related type
                    NanoLrsManager relatedManager =
                            PersistenceManager.getInstance().getManager(realtedManagerClass);


                    /*
                        LOOK UP. IF NOT THERE>CREATE>ASSIGN ID. ASSIGN
                    */
                    ///*
                    Object entity_id = value;
                    Class relatedObjProxy = getterMethod.getReturnType();

                    //TODO:
                    // Don't look up Just set it. (DB Managers will do an insert)
                    //look up entity by this id
                    NanoLrsModel relatedObj = relatedManager.findByPrimaryKey(context, value);
                    // if doesn't exist, create a blank one.
                    if (relatedObj == null){
                        //create a blank one
                        relatedObj = relatedManager.makeNew();

                        //Assign primary key (entity_id) to addMe
                        //Get Primary Key setter from Primary Key Annotation in the getter of addMe Class
                        //Get relatedObj's proxy FIRST
                        //Loop through methods of the proxy to get the primary key getter first then
                        //get the setter from the name of the getter and then invote it with the value.

                        Method relatedObjProxyPKGetter = null;
                        Method relatedObjProxyPKSetter = null;
                        Method relatedObjEntityPKSetter = null;
                        relatedObj.getClass().getMethods();

                        Method[] relatedObjProxyMethods = relatedObjProxy.getMethods();
                        for (Method everyProxyMethod : relatedObjProxyMethods){
                            if(everyProxyMethod.isAnnotationPresent(PrimaryKeyAnnotationClass.class)){
                                relatedObjProxyPKGetter = everyProxyMethod;
                                break;

                            }
                        }
                        String primaryKeyGetterName = null;
                        String primaryKeySetterName = null;
                        if(relatedObjProxyPKGetter != null){
                            primaryKeyGetterName = relatedObjProxyPKGetter.getName();
                            int prefixLen = 0;
                            if(primaryKeyGetterName.startsWith("is"))
                                prefixLen = 2;
                            else if(primaryKeyGetterName.startsWith("get"))
                                prefixLen = 3;
                            primaryKeySetterName = "set" + Character.toUpperCase(primaryKeyGetterName.charAt(prefixLen))
                                    + primaryKeyGetterName.substring(prefixLen +1);

                            //relatedObjEntityPKSetter = relatedObj.getClass().getMethod(primaryKeySetterName, entity_id.getClass());
                            Method relatedObjPKMethod = relatedObj.getClass().getMethod(primaryKeySetterName, entity_id.getClass());
                            //((NanoLrsManagerSyncable)relatedManager).persist(context, relatedObj, false);
                            relatedObjPKMethod.invoke(relatedObj, value);
                            valueType = relatedObjProxy;
                        }
                    }
                    //((NanoLrsManagerSyncable)relatedManager).persist(context, relatedObj, false);
                    //relatedManager.persist(context, relatedObj);
                    value = relatedObj;


                    //*/


                }

                //gets the setter Method on the current methodName with argument of
                // value's type (class)

                Method[] methods = proxyClass.getMethods();
                Type methodReturnType = null;
                Class methodReturnClass = null;
                Class methodReturnClassClass = null;
                String methodReturnTypeName = null;
                Object valueCasted = null;
                for(Method method:methods){
                    if (method.getName().equals(methodName)) {
                        methodReturnType = method.getParameterTypes()[0];
                        methodReturnClass = (Class)methodReturnType;

                        //Cannot get class for primitive
                        //methodReturnClassClass = Class.forName(methodReturnTypeName);
                        break;
                    }
                }
                if (methodReturnType == null){
                    continue;
                }

                //Cast primitive
                if(methodReturnClass.getName().equals("long")){
                    valueCasted = Long.parseLong(value.toString());
                }else if(methodReturnClass.getName().equals("float")){
                    valueCasted = Float.parseFloat(value.toString());
                }else if(methodReturnClass.getName().equals("int")){
                    valueCasted = Integer.parseInt(value.toString());
                }else if(methodReturnClass.getName().equals("double")){
                    valueCasted = Double.parseDouble(value.toString());
                }else if(methodReturnClass.getName().equals("short")){
                    valueCasted = Short.parseShort(value.toString());
                }else if(methodReturnClass.getName().equals("char")){
                    valueCasted = value.toString().charAt(0);
                }else if(methodReturnClass.getName().equals("boolean")){
                    valueCasted = Boolean.parseBoolean(value.toString());
                }
                else{
                    valueCasted=value;
                }
                //Cannot cast long to int, etc
                //methodReturnClass.cast(valueCasted);

                //Method setterMethod = proxyClass.getMethod(methodName, valueType);
                Method setterMethod = proxyClass.getMethod(methodName, methodReturnClass);
                //Invokes the setter Method that we got and gives it the value to pass
                //to its argument
                //setterMethod.invoke(newObj, value);
                String setterMethodName = setterMethod.getName();
                setterMethod.invoke(newObj, valueCasted);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }

        return newObj;
    }

}
