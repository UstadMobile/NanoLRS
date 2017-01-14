package com.ustadmobile.nanolrs.entitygen;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Created by mike on 1/12/17.
 */

public abstract class EntityGenerator {

    /**
     * Represents a TEXT field (as opposed to varchar) in the database
     */
    public static final String DATA_TYPE_LONG_STRING = "LONG_STRING";

    public static final String DATA_TYPE_BYTE_ARRAY = "BYTE_ARRAY";

    public EntityGenerator() {

    }

    /**
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

        String proxyFileName;
        File outFile;
        for(File proxyInterfaceFile: proxyInterfaceFiles) {
            proxyFileName = proxyInterfaceFile.getName();
            generate(proxyInterfaceFile, outDir, outPackage);
        }
    }

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

    public abstract void generate(File proxyInterface, File outDir, String outPackage) throws IOException;

}
