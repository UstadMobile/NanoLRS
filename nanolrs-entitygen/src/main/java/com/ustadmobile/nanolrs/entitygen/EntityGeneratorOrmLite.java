package com.ustadmobile.nanolrs.entitygen;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.apache.commons.io.FileUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaDocTag;
import org.jboss.forge.roaster.model.Type;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.PropertySource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Entity Generator to create Entity Classes for OrmLite.  For each entity it will:
 *
 * 1. Create a class with the \@DatabaseTable annotation with the table name set to the entity name
 *   separated by _ (e.g. XapiStatement will be stored in a table called xapi_statement) in a class
 *   with the entity name and the suffix "Entity" (e.g. XapiStatementEntity); which will be defined
 *   as implementing the interface it is being generated from (e.g.
 *   XapiStatementEntity implements XapiStatement ).
 *
 * 2. Create a final public static String constant for each property called COLNAME_PROPERTY_NAME
 *    initialized to property_name
 *
 * 3. Create a field with the \@DatabaseField annotation for each property with columnName set to the
 *    previously generated constant (useful for coding queries)
 *
 * 4. If the property is named one of values recognized as a primary key as per
 *    EntityGenerator.PRIMARY_KEY_PROPERTY_NAMES then id=true will be added to the DatabaseField
 *    annotation
 *
 * 5. If the property is a relationship to another entity : The field type will be the related
 *    entity class (e.g. XapiStatementEntity) and the setter function will cast the parameter
 *    e.g. setStatement(XapiStatement statement) { this.statement = (XapiStatementEntity)statement;}
 *
 */

public class EntityGeneratorOrmLite extends EntityGenerator {

    public static final String NANOLRS_MODEL_SYNCABLE_CLASS_NAME = "NanoLrsModelSyncable";
    public static final String NANOLRS_MODEL_NONSYNCABLE_CLASS_NAME = "NanoLrsModel";

    /**
     *
     * @param baseName eg: XapiStatement.java
     * @param proxyInterfaceFile    eg: File<XapiStatement.java>
     * @param outDir output directory to save generated interface to eg: .../generated/model/
     * @param outPackage package declaration to use for generated interface     eg: ....generated.model
     *
     * @throws IOException
     */
    public void generate(String baseName, File proxyInterfaceFile,
                         File outDir, String outPackage) throws IOException{
        String ormLiteClassName = baseName + "Entity";
        File outFile = new File(outDir, ormLiteClassName + ".java");

        String generatorPath =
                "..\\nanolrs-entitygen\\src\\main\\java\\com\\ustadmobile\\nanolrs\\entitygen";
        File thisGeneratorFile =
                new File(generatorPath, this.getClass().getSimpleName() + ".java");

        //This is checking if the proxy file is newer than the entity file created
        //(ie: no more changes)
        //This is also checking if there is any change to EntityGen itself.
        //If Entitygen is newer than the proxy, this will be processed.
        //Update: this might not be needed, as we delete everything every time this is run..
        if(outFile.lastModified() > proxyInterfaceFile.lastModified())
            if(thisGeneratorFile.lastModified() < outFile.lastModified())
                return;

        String proxyStr = FileUtils.readFileToString(proxyInterfaceFile, "UTF-8");
        JavaInterfaceSource proxyInterface =
                Roaster.parse(JavaInterfaceSource.class, proxyStr);
        Iterator<MethodSource<JavaInterfaceSource>> iterator =
                proxyInterface.getMethods().iterator();
        JavaClassSource ormLiteObj = Roaster.create(JavaClassSource.class);
        ormLiteObj.setPackage(outPackage).setName(ormLiteClassName);

        String tableName = convertCamelCaseNameToUnderscored(
            Character.toLowerCase(baseName.charAt(0)) + baseName.substring(1));
        ormLiteObj.addAnnotation(DatabaseTable.class).setStringValue("tableName", tableName);
        ormLiteObj.addInterface(proxyInterface);

        //Inheritence: Base class
        /* Steps:
        1. Get Base classes
        2. Loop over them
        3. Create entites for every method
        */
        List<String> allInterfaces = proxyInterface.getInterfaces();


        for(String everyInterface:allInterfaces){
            try {
                //This won't work since entitygen doesn't have proxy in class path.
                //Thats why we work with file paths.
                //Class<?> interfaceClass = Class.forName(everyInterface);
                //String everyInterfacePath = proxyInterface.resolveType(everyInterface);
                String everyInterfacePath =
                        proxyInterfaceFile.getParent() + File.separator +
                                everyInterface.split("\\.")
                                        [everyInterface.split("\\.").length -1] + ".java";

                File everyInterfaceFile = new File(everyInterfacePath);

                String everyInterfaceStr = FileUtils.readFileToString(everyInterfaceFile, "UTF-8");
                JavaInterfaceSource everyInterfaceSource =
                        Roaster.parse(JavaInterfaceSource.class, everyInterfaceStr);

                /*
                Before we go ahead, the following must be extending something.
                eg: NanoLrsModelSyncable extends NanoLrsModel . So we need that
                as well..
                 */
                List<String> allSubInterfaces = everyInterfaceSource.getInterfaces();
                for(String everySubInterface:allSubInterfaces){
                    try{
                        String everySubInterfacePath =
                                proxyInterfaceFile.getParent() + File.separator +
                                        everySubInterface.split("\\.")
                                [everySubInterface.split("\\.").length -1] + ".java";

                        File everySubInterfaceFile = new File(everySubInterfacePath);

                        String everySubInterfaceStr = FileUtils.readFileToString(everySubInterfaceFile, "UTF-8");
                        JavaInterfaceSource everySubInterfaceSource =
                                Roaster.parse(JavaInterfaceSource.class, everySubInterfaceStr);
                        Iterator<MethodSource<JavaInterfaceSource>> everySubInterfaceIterator =
                                everySubInterfaceSource.getMethods().iterator();
                        while(everySubInterfaceIterator.hasNext()){
                            everyInterfaceSource.addMethod(everySubInterfaceIterator.next());
                        }

                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
                Iterator<MethodSource<JavaInterfaceSource>> everyInterfaceIterator =
                        everyInterfaceSource.getMethods().iterator();

                generateFromIterator(everyInterfaceIterator, ormLiteObj, everyInterfaceSource);

            }catch(Exception e){
                e.printStackTrace();
            }
        }

        //Find properties - Map of name -> Type
        Map<String, Object> propertiesFound = new HashMap<>();

        generateFromIterator(iterator, ormLiteObj, proxyInterface);

        FileUtils.write(outFile, ormLiteObj.toString(), "UTF-8");
    }

    @Override
    public void generateMapping(Map<File, File> proxiesWithManagers, File outFile,
                                String outPackage, String modelPackage, String managerPackage)
            throws IOException {
        /*
        Not using JavaClassSource because it cant do static inline blocks / can't figure that out.
         */

        JavaClassSource ormLiteObj = Roaster.create(JavaClassSource.class);
        ormLiteObj.setPackage(outPackage).setName(EntityGenerator.MODEL_MANAGER_MAPPING_FILE);

        ormLiteObj.addImport(HashMap.class);

        ormLiteObj.addField("private static HashMap<String, Class> proxyNameToClassMap = new HashMap<>();");
        ormLiteObj.addField("private static HashMap<Class, Class> proxyClassToManagerMap = new HashMap<>();");

        String top = "package com.ustadmobile.nanolrs.core.mapping;\n" +
                "\n" +
                "import java.util.HashMap;\n";

        String classStart = "\npublic class ModelManagerMapping {\n";
        String classEnd = "\n}";
        String importBit = "\n";
        String initBit = "\n\tpublic static HashMap<String, Class> proxyNameToClassMap = new HashMap<>();\n" +
                "\tpublic static HashMap<Class, Class> proxyClassToManagerMap = new HashMap<>();\n";
        String bodyBit = "";
        String syncableEntitiesBit = "\n\tpublic static Class[] SYNCABLE_ENTITIES = new Class[]{\n";
        String syncableEntitiesEndBit = "\n\t};";
        boolean first = true;
        Iterator<Map.Entry<File, File>> proxiesWithManagersIterator = proxiesWithManagers.entrySet().iterator();
        while(proxiesWithManagersIterator.hasNext()){
            Map.Entry<File, File> proxyManagerEntry = proxiesWithManagersIterator.next();
            File proxy = proxyManagerEntry.getKey();
            File manager = proxyManagerEntry.getValue();
            String proxyName = proxy.getName().substring(0,
                    proxy.getName().length()-".java".length());
            String managerName = manager.getName().substring(0,
                    manager.getName().length()-".java".length());


            String proxyStr = FileUtils.readFileToString(proxy, "UTF-8");
            JavaInterfaceSource proxyInterface =
                    Roaster.parse(JavaInterfaceSource.class, proxyStr);

            String pntcm = "proxyNameToClassMap.put("+ proxyName+".class.getName()" +"," + proxyName+".class" + ");";
            String pctmp = "proxyClassToManagerMap.put(" +proxyName+".class" + "," + managerName+".class"+ ");";
            //ormLiteObj.addField(pntcm);
            //ormLiteObj.addField(pctmp);

            bodyBit = bodyBit + "\n\t\t" + pntcm + "\n";
            bodyBit = bodyBit + "\n\t\t" + pctmp + "\n";

            importBit = importBit + "import " + modelPackage + "." + proxyName + ";\n";
            importBit = importBit + "import " + managerPackage + "." + managerName + ";\n";

            if (proxyInterface.hasInterface(NANOLRS_MODEL_SYNCABLE_CLASS_NAME)) {
                if(first){
                    first = false;
                    syncableEntitiesBit = syncableEntitiesBit + "\t\t" + proxyName + ".class";
                }else {
                    syncableEntitiesBit = syncableEntitiesBit + ", " + proxyName + ".class";
                }
            }



        }
        String staticStart = "\tstatic{\n";
        String staticEnd = "\n\t}";
        String finalFileString = top + importBit + classStart + initBit + staticStart +
                bodyBit + staticEnd + syncableEntitiesBit + syncableEntitiesEndBit + classEnd;
        FileUtils.write(outFile, finalFileString, "UTF-8");
        //FileUtils.write(outFile, ormLiteObj.toString(), "UTF-8");

    }

    @Override
    public void generateTableList(File[] proxyInterfaceFiles, File entityDir, File tableListFile,
                                  String tableListPackage) throws IOException {


        JavaClassSource ormLiteObj = Roaster.create(JavaClassSource.class);
        ormLiteObj.setPackage(tableListPackage).setName(EntityGenerator.ENTITIES_TABLE_FILE);


        //Loop through proxy interfaces
        boolean firstOne = true;
        String entitiesClassList = "";
        for(File proxyInterfaceFile: proxyInterfaceFiles) {
            String proxyStr = FileUtils.readFileToString(proxyInterfaceFile, "UTF-8");
            JavaInterfaceSource proxyInterface =
                    Roaster.parse(JavaInterfaceSource.class, proxyStr);
            Iterator<MethodSource<JavaInterfaceSource>> iterator =
                    proxyInterface.getMethods().iterator();

            List<String> allInterfaces = proxyInterface.getInterfaces();
            //System.out.println("All interfaces: " + allInterfaces);

            if(proxyInterface.hasInterface(NANOLRS_MODEL_SYNCABLE_CLASS_NAME) ||
                    proxyInterface.hasInterface(NANOLRS_MODEL_NONSYNCABLE_CLASS_NAME)){
                String proxyName = proxyInterface.getName();
                String proxyClassPath = proxyInterface.getQualifiedName();
                //System.out.println(proxyName + " has it..");
                String entityName = proxyName + "Entity";
                String entityFileName = entityName + ".java";
                File entityFile = new File(entityDir.getAbsolutePath() + File.separator + entityFileName);
                if(entityFile.exists() && !proxyName.equals(NANOLRS_MODEL_SYNCABLE_CLASS_NAME)){
                    String entityStr = FileUtils.readFileToString(entityFile, "UTF-8");
                    JavaClassSource entityClassSource =
                            Roaster.parse(JavaClassSource.class, entityStr);
                    String entityClasspath = entityClassSource.getQualifiedName();
                    ormLiteObj.addImport(entityClasspath);
                    if(firstOne){
                        firstOne = false;
                        entitiesClassList = entityName + ".class";
                    }else{
                        entitiesClassList = entitiesClassList + ", " + entityName + ".class";
                    }
                }

            }
        }
        String entitiesToTableField = "public static Class[] TABLE_CLASSES = new Class[]{" + entitiesClassList + "};";

        ormLiteObj.addField(entitiesToTableField);
        FileUtils.write(tableListFile, ormLiteObj.toString(), "UTF-8");

    }

    protected void generateFromIterator(Iterator<MethodSource<JavaInterfaceSource>> iterator,
                                        JavaClassSource ormLiteObj, JavaInterfaceSource proxyInterface){
        while(iterator.hasNext()) {
            MethodSource<JavaInterfaceSource> method = iterator.next();
            String methodPrefix = null;
            if(method.getName().startsWith("get"))
                methodPrefix = "get";
            else if(method.getName().startsWith("is"))
                methodPrefix = "is";

            if(methodPrefix == null)
                continue;
            int methodPrefixLength = methodPrefix.length();

            String propertyName =
                    Character.toLowerCase(method.getName().charAt(methodPrefixLength)) +
                            method.getName().substring(methodPrefixLength+1);

            String dbFieldName = convertCamelCaseNameToUnderscored(propertyName);

            //Make a static field COLNAME_PROPNAME that provides the column name for use in queries etc
            FieldSource colNameField = ormLiteObj.addField();
            colNameField.setStatic(true);
            colNameField.setPublic();
            colNameField.setFinal(true);
            colNameField.setType("String");
            colNameField.setName("COLNAME_"  + dbFieldName.toUpperCase());
            colNameField.setLiteralInitializer('\"' + dbFieldName + '\"');

            String propertyTypeName = method.getReturnType().getName();

            if(propertyTypeName.startsWith("List")){
                propertyTypeName = "List<" + method.getReturnType().getTypeArguments().get(0)+ ">";
            }

            PropertySource<JavaClassSource> property =
                    ormLiteObj.addProperty(propertyTypeName, propertyName).setMutable(false);
            FieldSource propertyField = property.getField();

            /*
            If it isn't a collection, add @Database . (We don't put that for foreignCollections)
             */
            if(!propertyTypeName.startsWith("Collection")){
                AnnotationSource databaseFieldAnnotation =
                        propertyField.addAnnotation(DatabaseField.class);
                databaseFieldAnnotation.setLiteralValue("columnName", "COLNAME_"
                        + dbFieldName.toUpperCase());
                if(!method.getReturnType().isPrimitive() && !propertyTypeName.equals("String")) {
                    databaseFieldAnnotation.setLiteralValue("foreign", "true");
                    databaseFieldAnnotation.setLiteralValue("foreignAutoRefresh", "true");
                }

                List<JavaDocTag> dataTypeJavaDocTags = method.getJavaDoc().getTags("@nanolrs.datatype");
                if(dataTypeJavaDocTags != null && dataTypeJavaDocTags.size() > 0) {
                    String tagValue = dataTypeJavaDocTags.get(0).getValue();
                    ormLiteObj.addImport(DataType.class);

                    if(tagValue != null && tagValue.equals(DATA_TYPE_LONG_STRING)) {
                        databaseFieldAnnotation.setLiteralValue("dataType", "DataType.LONG_STRING");
                    }else if(tagValue != null && tagValue.equals(DATA_TYPE_BYTE_ARRAY)) {
                        databaseFieldAnnotation.setLiteralValue("dataType", "DataType.BYTE_ARRAY");
                    }
                }

                if(isPrimaryKey(method)) {
                    databaseFieldAnnotation.setLiteralValue("id", "true");
                }

                String foreignColumnNameString = null;
                Set<String> foreignFields = method.getJavaDoc().getTagNames();
                for (String field:foreignFields){
                    if(field.startsWith("@nanolrs.foreignColumnName=")){
                        foreignColumnNameString = field.substring(field.indexOf("=")+1);
                        break;
                    }
                }
                if(foreignColumnNameString != null){
                    databaseFieldAnnotation.setLiteralValue("foreignColumnName", "\"" + foreignColumnNameString + "\"");
                }
            }


            /**
             * In case of handling a relationship field: The field must be the entity
             */
            if(propertyTypeName.startsWith("Collection")){
                Type<JavaInterfaceSource> listFirstType =
                        method.getReturnType().getTypeArguments().get(0);
                String propertyEntityClassName = null;
                boolean complexType = false;
                if (listFirstType.getSimpleName().startsWith("? extends ")){
                    complexType = true;
                    propertyEntityClassName =
                            listFirstType.getName().split("\\s+")[listFirstType.getName().split("\\s+").length-1];
                }else{
                    propertyEntityClassName = listFirstType.getName();
                }
                propertyTypeName = method.getReturnType().toString();
                String listTypeName = propertyEntityClassName;

                if(!listFirstType.isPrimitive() && !listFirstType.getName().equals("String")) {
                    String foreignFieldNameString = null;
                    //Needs ForeignCollection
                    AnnotationSource foreignCollectionField = propertyField.addAnnotation(ForeignCollectionField.class);

                    foreignCollectionField.setLiteralValue("eager", "false");

                    propertyEntityClassName = "ForeignCollection<" + listTypeName + "Entity" + ">";
                }else{
                    propertyEntityClassName = "List<" + listTypeName + ">";
                }
                propertyField.setType(propertyEntityClassName);
                property.getField().setFinal(false);

                MethodSource mutatorMethod;
                if(property.getMutator() == null) {
                    mutatorMethod = property.createMutator();
                }else {
                    mutatorMethod = property.getMutator();
                }

                mutatorMethod.setBody("if(this." + propertyName + "!= null){" + '\n'+
                        "    this." + propertyName + ".clear();" +'\n' +
                        "}" + '\n'+ "this." + propertyName +
                        " = (" + propertyEntityClassName +")" + propertyName + ";");

                ormLiteObj.addImport(method.getReturnType().getQualifiedName());
                ormLiteObj.addImport(ForeignCollection.class.getName());
                for(Type<JavaInterfaceSource> everyType:method.getReturnType().getTypeArguments()){
                    String qualifiedName = everyType.getQualifiedName();
                    if(qualifiedName.startsWith("? extends ")){
                        String newQualifiedName = qualifiedName.split("\\s+")[qualifiedName.split("\\s").length-1];
                        if (newQualifiedName != null && !newQualifiedName.equals("")){
                            qualifiedName = proxyInterface.resolveType(newQualifiedName);
                            //qualifiedName = newQualifiedName;
                        }

                    }
                    ormLiteObj.addImport(qualifiedName);
                }

            }
            else
            if(!method.getReturnType().isPrimitive() && !propertyTypeName.equals("String")) {


                String propertyEntityClassName = null;

                if(method.getReturnType().getName().equals(("DateTimeType"))){
                    propertyEntityClassName = propertyTypeName;
                }else {
                    propertyEntityClassName = propertyTypeName + "Entity";
                }
                propertyField.setType(propertyEntityClassName);
                property.getField().setFinal(false);

                MethodSource mutatorMethod;
                if(property.getMutator() == null) {
                    mutatorMethod = property.createMutator();
                }else {
                    mutatorMethod = property.getMutator();
                }

                mutatorMethod.setBody("this." + propertyName + " = (" + propertyEntityClassName +")" + propertyName + ";");

                ormLiteObj.addImport(method.getReturnType().getQualifiedName());
            }else {
                property.setMutable(true);
            }

        }

    }

    /**
     *
     * @param proxyInterfaceDir The directory containing proxy interface files ../core/model/
     * @param outDir ..../generated/model/
     * @param outPackage ...generated.model
     * @throws IOException
     */
    @Override
    public void generateDir(File proxyInterfaceDir, File outDir, String outPackage) throws IOException {
        super.generateDir(proxyInterfaceDir, outDir, outPackage);

        File[] allEntities = outDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith("Entity.java");
            }
        });

        //remove any entities that might have been deleted
        for(File entityFile: allEntities) {
            File proxyInterfaceFile = new File(proxyInterfaceDir, entityFile.getName().substring(0,
                    entityFile.getName().length() - "Entity.java".length()) + ".java");
            if(!proxyInterfaceFile.exists()) {
                //System.out.println("Deleting removed entity : Interface for " + entityFile.getName() + " does not exist at " + proxyInterfaceFile.getAbsolutePath());
                entityFile.delete();
            }
        }
    }

    /**
     * Usage: EntityGeneratorOrmLite &lt;input directory&gt; &lt;output directory&gt; &lt;output package name&gt;
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        File proxyInterfaceInDir = new File(args[0]);
        File outDir = new File(args[1]); //..generated/model/

        if(args.length < 3) {
            System.err.println("Usage: EntityGeneratorOrmLite <input directory> <output directory> <output package name>");
            System.exit(1);
        }

        if(!proxyInterfaceInDir.isDirectory()){
            outDir.mkdirs();
        }

        EntityGeneratorOrmLite generator = new EntityGeneratorOrmLite();
        generator.generateDir(proxyInterfaceInDir, outDir, args[2]);

    }
}
