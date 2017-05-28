package com.ustadmobile.nanolrs.entitygen;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.apache.commons.io.FileUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaDocTag;
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



    public void generate(String baseName, File proxyInterfaceFile, File outDir, String outPackage) throws IOException{
        String ormLiteClassName = baseName + "Entity";
        File outFile = new File(outDir, ormLiteClassName + ".java");

        if(outFile.lastModified() > proxyInterfaceFile.lastModified())
            return;

        String proxyStr = FileUtils.readFileToString(proxyInterfaceFile, "UTF-8");
        JavaInterfaceSource proxyInterface = Roaster.parse(JavaInterfaceSource.class, proxyStr);
        Iterator<MethodSource<JavaInterfaceSource>> iterator = proxyInterface.getMethods().iterator();
        JavaClassSource ormLiteObj = Roaster.create(JavaClassSource.class);
        ormLiteObj.setPackage(outPackage).setName(ormLiteClassName);

        String tableName = convertCamelCaseNameToUnderscored(
            Character.toLowerCase(baseName.charAt(0)) + baseName.substring(1));
        ormLiteObj.addAnnotation(DatabaseTable.class).setStringValue("tableName", tableName);
        ormLiteObj.addInterface(proxyInterface);

        //Find properties - Map of name -> Type
        Map<String, Object> propertiesFound = new HashMap<>();
        generateFromIterator(iterator, ormLiteObj);

        FileUtils.write(outFile, ormLiteObj.toString(), "UTF-8");
    }

    protected void generateFromIterator(Iterator<MethodSource<JavaInterfaceSource>> iterator, JavaClassSource ormLiteObj) {
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


            String propertyName = Character.toLowerCase(method.getName().charAt(methodPrefixLength)) +
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
            PropertySource<JavaClassSource> property = ormLiteObj.addProperty(propertyTypeName, propertyName).setMutable(false);
            FieldSource propertyField = property.getField();
            AnnotationSource databaseFieldAnnotation = propertyField.addAnnotation(DatabaseField.class);
            databaseFieldAnnotation.setLiteralValue("columnName", "COLNAME_"
                    + dbFieldName.toUpperCase());


            /**
             * In case of handling a relationship field: The field must be the entity
             */
            if(!method.getReturnType().isPrimitive() && !propertyTypeName.equals("String")) {
                String propertyEntityClassName = propertyTypeName + "Entity";
                propertyField.setType(propertyEntityClassName);
                property.getField().setFinal(false);

                MethodSource mutatorMethod;
                if(property.getMutator() == null) {
                    mutatorMethod = property.createMutator();
                }else {
                    mutatorMethod = property.getMutator();
                }

                mutatorMethod.setBody("this." + propertyName + " = (" + propertyEntityClassName +")" + propertyName + ";");
                databaseFieldAnnotation.setLiteralValue("foreign", "true");
                databaseFieldAnnotation.setLiteralValue("foreignAutoRefresh", "true");
                ormLiteObj.addImport(method.getReturnType().getQualifiedName());
            }else {
                property.setMutable(true);
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
        }
    }

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
                System.out.println("Deleting removed entity : Interface for " + entityFile.getName() + " does not exist at " + proxyInterfaceFile.getAbsolutePath());
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
        File outDir = new File(args[1]);

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
