package com.ustadmobile.nanolrs.entitygen;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 *
 */
public abstract class EntityGenerator {

    public static String[] PRIMARY_KEY_PROPERTY_NAMES = new String[]{"id", "uuid", "activityId"};

    /**
     * Represents a TEXT field (as opposed to varchar) in the database
     */
    public static final String DATA_TYPE_LONG_STRING = "LONG_STRING";

    /**
     * Represents a byte array data field
     */
    public static final String DATA_TYPE_BYTE_ARRAY = "BYTE_ARRAY";

    public EntityGenerator() {

    }

    /**
     * Calls the generate method for all .java files in the given input directory
     *
     * @param proxyInterfaceDir The directory containing proxy interface files
     * @param outDir
     * @param outPackage
     * @throws IOException
     */
    public void generateDir(File proxyInterfaceDir, File outDir, String outPackage) throws IOException{
        File[] proxyInterfaceFiles = proxyInterfaceDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".java");
            }
        });

        for(File proxyInterfaceFile: proxyInterfaceFiles) {
            String entityName = proxyInterfaceFile.getName().substring(0,
                    proxyInterfaceFile.getName().length()-".java".length());
            generate(entityName, proxyInterfaceFile, outDir, outPackage);
        }
    }

    /**
     * Converts a property name from e.g. from fullName to full_name
     *
     * @param propertyName Property Name e.g. propertyName
     *
     * @return Property named in lower case separated by underscores e.g. property_name
     */
    public String convertCamelCaseNameToUnderscored(String propertyName) {
        String undererScoredName = "";
        for(int i = 0; i < propertyName.length(); i++) {
            if(Character.isUpperCase(propertyName.charAt(i)) && (i == 0 || Character.isLowerCase(propertyName.charAt(i-1)))) {
                undererScoredName += "_";
            }
            undererScoredName += Character.toLowerCase(propertyName.charAt(i));
        }

        return undererScoredName;
    }

    /**
     * Return a given string with the first letter upper case. Useful when you have propertyName
     * and want to generate something like setPropertyName
     *
     * @param str Input String
     *
     * @return String with first letter converted to upper case
     */
    public String upperCaseFirstLetter(String str){
        String result = "";
        if(str.length() > 0)
            result += Character.toUpperCase(str.charAt(0));
        if(str.length() > 1)
            result += str.substring(1);

        return  result;
    }


    /**
     * Generate an implementation of this entity for the given interface
     *
     * @param entityName The name of the Entity e.g. XapiStatement
     * @param proxyInterface Java source file containing the interface with getters/setters
     * @param outDir output directory to save generated interface to
     * @param outPackage package declaration to use for generated interface
     *
     * @throws IOException
     */
    public abstract void generate(String entityName, File proxyInterface, File outDir, String outPackage) throws IOException;

}
