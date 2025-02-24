package com.axway.apim;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class Archive {

    public Manifest createManifest(String uuidStr){
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(new Attributes.Name("Id"),uuidStr);
        attributes.put(new Attributes.Name("Timestamp"), System.currentTimeMillis()+"");
        return manifest;
    }

    public  void createFed(String uuisStr, Manifest manifest, String filename, File dir) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(filename);
             JarOutputStream jarOutputStream = new JarOutputStream(fileOutputStream, manifest)){
            File[] files = dir.listFiles();
            for (File file : files) {
                if (!file.isDirectory()) {
                    addContent(jarOutputStream, file, uuisStr);
                } else {
                    if (file.getName().equals("meta-inf")) {
                        File[] metaInfFiles = file.listFiles();
                        for (File metaInfFile : metaInfFiles) {
                            if (metaInfFile.getName().equals("manifest.mf")) {
                                continue;
                            }

                            addContent(jarOutputStream, metaInfFile, "meta-inf");
                        }
                    }
                }
            }

        }
    }


    private   void addContent(JarOutputStream jarOutputStream, File file, String dirName) throws IOException {
        try ( BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))){
            jarOutputStream.putNextEntry(new JarEntry(dirName + "/" + file.getName()));
            byte[] buffer = new byte[1024];
            while (true) {
                int count = bufferedInputStream.read(buffer);
                if (count == -1)
                    break;
                jarOutputStream.write(buffer, 0, count);
            }
            jarOutputStream.closeEntry();
        }
    }
}
