package com.ustadmobile.nanolrs.entitygen;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.apache.commons.io.FileUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.Property;
import org.jboss.forge.roaster.model.Type;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.PropertySource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by mike on 1/12/17.
 */

public class EntityGeneratorOrmLite extends EntityGenerator {

    public static String[] PRIMARY_KEY_PROPERTY_NAMES = new String[]{"id", "uuid", "activityId"};

    public void generate(File proxyInterfaceFile, File outDir) throws IOException{
        String proxyInterfaceName = proxyInterfaceFile.getName().substring(0,
                proxyInterfaceFile.getName().length()-".java".length());
        String baseName = proxyInterfaceName;

        String ormLiteClassName = baseName + "Entity";

        String proxyStr = FileUtils.readFileToString(proxyInterfaceFile, "UTF-8");
        JavaInterfaceSource proxyInterface = Roaster.parse(JavaInterfaceSource.class, proxyStr);
        Iterator<MethodSource<JavaInterfaceSource>> iterator = proxyInterface.getMethods().iterator();
        JavaClassSource ormLiteObj = Roaster.create(JavaClassSource.class);
        ormLiteObj.setPackage("com.something").setName(ormLiteClassName);

        String tableName = convertCamelCaseNameToUnderscored(
            Character.toLowerCase(baseName.charAt(0)) + baseName.substring(1));
        ormLiteObj.addAnnotation(DatabaseTable.class).setStringValue("tableName", tableName);
        ormLiteObj.addInterface(proxyInterface);

        //Find properties - Map of name -> Type
        Map<String, Object> propertiesFound = new HashMap<>();
        while(iterator.hasNext()) {
            MethodSource<JavaInterfaceSource> method = iterator.next();
            if(!method.getName().startsWith("get"))
                continue;


            String propertyName = Character.toLowerCase(method.getName().charAt(3)) +
                method.getName().substring(4);


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
            PropertySource<JavaClassSource> property = ormLiteObj.addProperty(propertyTypeName, propertyName);
            FieldSource propertyField = property.getField();
            AnnotationSource databaseFieldAnnotation = propertyField.addAnnotation(DatabaseField.class);
            databaseFieldAnnotation.setLiteralValue("columnName", "COLNAME_"
                    + dbFieldName.toUpperCase());

            /**
             * In case of handling a relationship field: The field must be the entity
             */
            if(!method.getReturnType().isPrimitive() && !propertyTypeName.equals("String")) {
                String propertyEntityClassName = baseName + "Entity";
                propertyField.setType(propertyEntityClassName);
                MethodSource mutatorMethod = property.getMutator() != null ? property.getMutator() : property.createMutator();
                mutatorMethod.setBody("this." + propertyName + " = (" + propertyEntityClassName +")" + propertyName + ";");
                databaseFieldAnnotation.setLiteralValue("foreign", "true");
            }

            /**
             * Check if this is the primary key
             */
            if(Arrays.asList(PRIMARY_KEY_PROPERTY_NAMES).contains(propertyName)) {
                databaseFieldAnnotation.setLiteralValue("id", "true");
            }
        }


        File outFile = new File(outDir, ormLiteClassName + ".java");
        FileUtils.write(outFile, ormLiteObj.toString(), "UTF-8");
    }

    public static void main(String[] args) throws Exception{
        File proxyInterfaceInDir = new File(args[0]);
        File outDir = new File(args[1]);
        EntityGeneratorOrmLite generator = new EntityGeneratorOrmLite();
        generator.generateDir(proxyInterfaceInDir, outDir, ".java", "OrmLite.java");

    }
}
