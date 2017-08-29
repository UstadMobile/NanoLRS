package com.ustadmobile.nanolrs.entitygen;

import org.apache.commons.io.FileUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mike on 1/15/17.
 */

public class EntityGeneratorSharkOrm extends EntityGenerator{

    private String srkImportLine = "#import <SharkORM/SharkORM.h>";

    @Override
    public void generate(String entityName, File proxyInterfaceFile, File outDir, String outPackage) throws IOException {
        String proxyStr = FileUtils.readFileToString(proxyInterfaceFile, "UTF-8");
        JavaInterfaceSource proxyInterface = Roaster.parse(JavaInterfaceSource.class, proxyStr);
        String entityPackage = proxyInterface.getPackage();


        String srkOrmClassName = entityName + "SrkObj";
        File outputHeaderFile = new File(outDir, srkOrmClassName + ".h");
        File outputModuleFile = new File(outDir, srkOrmClassName + ".m");

        StringBuilder moduleIncludeSb = new StringBuilder();
        StringBuilder headerProperties = new StringBuilder();
        StringBuilder headerMethodSignatures = new StringBuilder();

        ArrayList<String> moduleDynamicFieldList = new ArrayList<>();
        StringBuilder moduleMethods = new StringBuilder();

        moduleIncludeSb.append(srkImportLine).append('\n');
        moduleIncludeSb.append("#import <Foundation/Foundation.h>\n");
        moduleIncludeSb.append("#import \"").append(proxyInterface.getName()).append(".h\"\n");
        moduleIncludeSb.append("#include \"J2ObjC_source.h\"\n");

        Iterator<MethodSource<JavaInterfaceSource>> iterator = proxyInterface.getMethods().iterator();
        while(iterator.hasNext()) {
            //TODODone: make a method that does the getter finding to share
            //Update: Leaving it here as its only used once here
            MethodSource<JavaInterfaceSource> method = iterator.next();
            String methodPrefix = null;
            if(method.getName().startsWith("is"))
                methodPrefix = "is";
            else if(method.getName().startsWith("get"))
                methodPrefix = "get";

            if(methodPrefix == null)
                continue;

            int methodPrefixLength = methodPrefix.length();
            String propertyName = Character.toLowerCase(method.getName().charAt(methodPrefixLength)) +
                    method.getName().substring(methodPrefixLength+1);
            //Some properties are called id - this is not valid in objective c as it's a keyword
            String methodParamName = !propertyName.equals("id") ? propertyName : propertyName + "_";

            String returnTypeName = method.getReturnType().getName();
            String objcPropertyType = null;
            String methodPropertyType = null;
            String setterWithSuffix = null;//j2objc generates methods called setPropertyNameWithTypeName

            boolean isPrimaryKey = isPrimaryKey(method);
            String objcFieldName = isPrimaryKey ? "Id" : "_" + propertyName;
            moduleDynamicFieldList.add(objcFieldName);

            if(returnTypeName.equals("String")) {
                objcPropertyType = methodPropertyType = "NSString*";
                setterWithSuffix = "NSString";
            }else if(returnTypeName.equals("boolean")) {
                objcPropertyType =  "BOOL";
                methodPropertyType = "jboolean";
                setterWithSuffix = "Boolean";
            }else if(returnTypeName.equals("byte[]")) {
                objcPropertyType = "NSData*";
                methodPropertyType = "IOSByteArray *";
                setterWithSuffix = "ByteArray";
            }else if(method.getReturnType().isPrimitive()){
                objcPropertyType = methodPropertyType = returnTypeName;
                setterWithSuffix = upperCaseFirstLetter(returnTypeName);
            }else {
                //should be another entity
                objcPropertyType = returnTypeName + "SrkObj*";
                methodPropertyType = "id<" + convertJavaNameToObjc(entityPackage, returnTypeName) + ">";
                moduleIncludeSb.append("#import \"").append(returnTypeName).append("SrkObj.h\"\n");
                setterWithSuffix = convertJavaNameToObjc(entityPackage, returnTypeName);
            }
            headerProperties.append("@property ").append(objcPropertyType).append(' ')
                    .append(objcFieldName).append(";\n");

            String propNameMethodPostfix = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
            StringBuilder getterSignature = new StringBuilder().append("- (").append(methodPropertyType).append(") ")
                .append(methodPrefix).append(propNameMethodPostfix);
            StringBuilder setterSignature = new StringBuilder().append("- (void)set")
                    .append(upperCaseFirstLetter(propertyName)).append("With").append(setterWithSuffix)
                    .append(":(").append(methodPropertyType).append(")").append(methodParamName);

            headerMethodSignatures.append(getterSignature).append(";\n");
            headerMethodSignatures.append(setterSignature).append(";\n");

            moduleMethods.append(getterSignature).append("{\n");
            if(!returnTypeName.equals("byte[]")) {
                moduleMethods.append("    return self.").append(objcFieldName);
            }else {
                moduleMethods.append("    return [IOSByteArray arrayWithNSData:self.")
                        .append(objcFieldName).append("]");
            }
            moduleMethods.append(";\n}\n");

            moduleMethods.append(setterSignature).append("{\n");
            moduleMethods.append("    self.").append(objcFieldName).append(" = ");

            if(!objcPropertyType.equals(methodPropertyType) && !returnTypeName.equals("byte[]")) {
                moduleMethods.append("(").append(objcPropertyType).append(")");//cast it
            }

            if(!returnTypeName.equals("byte[]")) {
                moduleMethods.append(methodParamName);
            }else {
                moduleMethods.append("[").append(methodParamName).append(" toNSData]");
            }

            moduleMethods.append(";\n}\n");
        }

        StringBuilder headerFileSb = new StringBuilder();
        headerFileSb.append(moduleIncludeSb);
        headerFileSb.append("@interface ").append(srkOrmClassName).append(" : ").append("SRKObject ")
                .append("<").append(convertJavaNameToObjc(entityPackage, proxyInterface.getName()))
                .append("> \n");
        headerFileSb.append(headerProperties);
        headerFileSb.append(headerMethodSignatures);
        headerFileSb.append("@end\n");

        StringBuilder moduleFileSb = new StringBuilder();
        moduleFileSb.append("#import \"").append(srkOrmClassName).append(".h\"\n");
        moduleFileSb.append("@implementation ").append(srkOrmClassName).append('\n');
        moduleFileSb.append("@dynamic ");
        for(int i = 0; i < moduleDynamicFieldList.size(); i++) {
            moduleFileSb.append(moduleDynamicFieldList.get(i));
            if(i < (moduleDynamicFieldList.size() -1))
                moduleFileSb.append(',');
            moduleFileSb.append(' ');
        }
        moduleFileSb.append(";\n");
        moduleFileSb.append(moduleMethods);
        moduleFileSb.append("@end\n");

        FileUtils.write(outputHeaderFile, headerFileSb, "UTF-8");
        FileUtils.write(outputModuleFile, moduleFileSb, "UTF-8");
    }

    /**
     * For a given java source object (which has a name and a package) convert the name to the
     * style that is used by j2objc : e.g. j2objc will convert com.company.ClassName to ComCompanyClassName
     *
     * @param packageName package of the class
     * @param className name of the class within the package
     *
     * @return the objc classname as it would be generated by j2objc
     */
    protected String convertJavaNameToObjc(String packageName, String className) {
        StringBuilder sb = new StringBuilder().append(Character.toUpperCase(packageName.charAt(0)));
        boolean upperCaseNext = false;
        for(int i = 1; i < packageName.length(); i++) {
            if(packageName.charAt(i) == '.') {
                upperCaseNext = true;
            }else if(upperCaseNext){
                sb.append(Character.toUpperCase(packageName.charAt(i)));
                upperCaseNext = false;
            }else {
                sb.append(packageName.charAt(i));
            }
        }
        sb.append(className);
        return sb.toString();
    }


    /**
     * Usage: EntityGeneratorSharkOrm &lt;input directory&gt; &lt;output directory&gt;
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        if(args.length < 2) {
            System.err.println("Usage: EntityGeneratorSharkOrm <input directory> <output directory> <output package name>");
            System.exit(1);
        }

        File proxyInterfaceInDir = new File(args[0]);
        File outDir = new File(args[1]);

        if(!outDir.isDirectory()){
            outDir.mkdirs();
        }

        EntityGeneratorSharkOrm generator = new EntityGeneratorSharkOrm();
        generator.generateDir(proxyInterfaceInDir, outDir, null);

    }
}
