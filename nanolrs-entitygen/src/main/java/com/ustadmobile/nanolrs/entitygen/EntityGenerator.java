package com.ustadmobile.nanolrs.entitygen;

import org.jboss.forge.roaster.model.JavaDocTag;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public abstract class EntityGenerator {

    /**
     * Represents a TEXT field (as opposed to varchar) in the database
     */
    public static final String DATA_TYPE_LONG_STRING = "LONG_STRING";

    /**
     * Represents a byte array data field
     */
    public static final String DATA_TYPE_BYTE_ARRAY = "BYTE_ARRAY";

    public static final String MODEL_MANAGER_MAPPING_FILE = "ModelManagerMapping";

    public static final String ENTITIES_TABLE_FILE = "EntitiesToTable";

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

        //List all proxy interface files
        File[] proxyInterfaceFiles = proxyInterfaceDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".java");
            }
        });

        //loop over every proxy interface file and generate its entity
        for(File proxyInterfaceFile: proxyInterfaceFiles) {
            String entityName = proxyInterfaceFile.getName().substring(0,
                    proxyInterfaceFile.getName().length()-".java".length());
            generate(entityName, proxyInterfaceFile, outDir, outPackage);
        }

        //Generate Proxy-Manager Mapping
        //System.out.println("Generating Model Manager Mapping");
        String mappingPackage = "com.ustadmobile.nanolrs.core.mapping";
        generateModelManagerMapping(proxyInterfaceDir, mappingPackage);

        //Generate Table-Classes: Entities that will become tables
        String tableListPackage = outPackage.substring(0, outPackage.length()-".model".length());
        String generatedDir = outDir.getAbsolutePath() + File.separator + ".." + File.separator;
        File tableListFile = new File(generatedDir + ENTITIES_TABLE_FILE + ".java");
        if(tableListFile.exists()){
            tableListFile.delete();
            tableListFile.createNewFile();
        }
        generateTableList(proxyInterfaceFiles, outDir, tableListFile, tableListPackage);

    }


    public void generateModelManagerMapping(File proxyInterfaceDir, String mappingDirPackage) throws IOException {
        String baseDirPath = proxyInterfaceDir.getAbsolutePath() +
                File.separator + ".." + File.separator;
        String managerInterfaceDirPath = baseDirPath + "manager" + File.separator;
        String mappingDirPath = baseDirPath + "mapping" + File.separator;

        //System.out.println("manager int path : " + managerInterfaceDirPath);

        File managerInterfaceDir = new File(managerInterfaceDirPath);
        File[] managerInterfaceFiles = managerInterfaceDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".java");
            }
        });
        //System.out.println("No. of managers : " + managerInterfaceFiles.length);
        File mappingFile = new File(mappingDirPath + MODEL_MANAGER_MAPPING_FILE + ".java");
        if(mappingFile.exists()){
            mappingFile.delete();
            mappingFile.createNewFile();
        }

        Map<File, File> proxyWithManagerMap = new HashMap<>();
        //Think of using classes instead ?
        Map<Class, Class> proxyWithManagerClass = null;

        for(File managerInterfaceFile:managerInterfaceFiles){
            String managerName = managerInterfaceFile.getName().substring(0,
                    managerInterfaceFile.getName().length()-".java".length());
            String associatedProxyName;
            if(managerName.endsWith("Manager")){
                associatedProxyName =managerName.substring(0,
                        managerName.length()-"Manager".length());
                String thisProxyInterfaceFilePath = proxyInterfaceDir.getAbsolutePath() +
                        File.separator + associatedProxyName + ".java";
                //System.out.println("Checking interface: " + thisProxyInterfaceFilePath);
                File thisProxyInterfaceFile = new File(thisProxyInterfaceFilePath);
                if (thisProxyInterfaceFile.exists()){
                    //System.out.println("Model-Manager exists!");
                    //Add this interface and manager mapping
                    proxyWithManagerMap.put(thisProxyInterfaceFile, managerInterfaceFile);

                    //proxyWithManagerClass.put(proxyClass, managerClass);
                }else{
                    //System.out.println("Model-Manager NOT exists");
                }
            }else{
                //System.out.println("Manager does not end with Manager");
            }
        }

        String modelPackage = "com.ustadmobile.nanolrs.core.model";
        String managerPackage = "com.ustadmobile.nanolrs.core.manager";

        generateMapping(proxyWithManagerMap, mappingFile, mappingDirPackage, modelPackage,
                managerPackage);
    }

    /**
     * Return true if this getter method represents a primary key - look for the @nanolrs.primarykey
     * javadoc tag
     *
     * @param method Getter method to check
     * @return true if method javadoc contains the nanolrs.primarykey tag, false otherwise
     */
    protected boolean isPrimaryKey(MethodSource method) {
        List<JavaDocTag> primaryKeyJavaDocTags = method.getJavaDoc().getTags("@nanolrs.primarykey");
        return primaryKeyJavaDocTags != null && primaryKeyJavaDocTags.size() > 0;
    }

    /**
     * Returns true is this getter method represents a password key - look for the @nanolrs.password
     * javadoc tag.
     *
     * @param method Getter method to check
     * @return true if mehtod javadoc contains the nanolrs.password tag, else false.
     */
    protected boolean isPasswordField(MethodSource method){
        List<JavaDocTag> passwordKeyJavaDocTags = method.getJavaDoc().getTags("@nanolrs.password");
        return passwordKeyJavaDocTags != null && passwordKeyJavaDocTags.size() > 0;
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
    public abstract void generate(String entityName, File proxyInterface, File outDir,
                                  String outPackage) throws IOException;

    /**
     * Generate model-manager mapping (interface model <-> interface manager)
     * @param proxiesWithManagers   The list of proxies that have managers (List of ProxyNameManager.java)
     * @param mappingFile           File of mapping
     * @param mappingDirPackage     package of mapping package : ...core.mapping
     */
    public abstract void generateMapping(Map<File, File> proxiesWithManagers, File mappingFile,
                                         String mappingDirPackage, String modelPackage,
                                         String managerPackage) throws IOException;

    /**
     * Generate list of entities that should be persisted as tables in the database
     * @param proxyInterfaceFiles   List of all proxy interfaces to look for
     * @param entityDir Where the entities at
     */
    public abstract void generateTableList(File[] proxyInterfaceFiles, File entityDir,
                                           File tableListFile, String tableListPackage)
            throws IOException;

}
