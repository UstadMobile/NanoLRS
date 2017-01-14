package com.ustadmobile.nanolrs.entitygen;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Created by mike on 1/12/17.
 */

public abstract class EntityGenerator {

    public EntityGenerator() {

    }

    /**
     *
     * @param proxyInterfaceDir The directory containing proxy interface files
     * @param outDir
     * @param proxyInterfaceSuffix
     * @param file
     * @throws IOException
     */
    public void generateDir(File proxyInterfaceDir, File outDir, final String proxyInterfaceSuffix, String outSuffix) throws IOException{
        File[] proxyInterfaceFiles = proxyInterfaceDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(proxyInterfaceSuffix);
            }
        });

        String proxyFileName;
        File outFile;
        for(File proxyInterfaceFile: proxyInterfaceFiles) {
            proxyFileName = proxyInterfaceFile.getName();
            outFile = new File(outDir, proxyFileName.substring(0,
                    proxyFileName.length()-proxyInterfaceSuffix.length()));
            generate(proxyInterfaceFile, outDir);
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

    public abstract void generate(File proxyInterface, File outDir) throws IOException;

}
