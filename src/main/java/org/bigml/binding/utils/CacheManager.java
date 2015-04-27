package org.bigml.binding.utils;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * This class will we used to manage the local cache of resource
 */
@SuppressWarnings("unused")
public class CacheManager {

    // Logging
    Logger logger = LoggerFactory.getLogger(CacheManager.class);

    private static final String TMP_FOLDER_PROPERTY = "java.io.tmpdir";

    private String storage = null;

    private File storageFolder;

    public CacheManager(String storage) {
        this.storage = storage;
        if( storage == null || storage.isEmpty() ) {
            // Get the temporary directory and print it.
            String tmpDir = System.getProperty(TMP_FOLDER_PROPERTY);
            if( tmpDir.endsWith("/") ) {
                this.storage = String.format("%s%s",
                        System.getProperty(TMP_FOLDER_PROPERTY),
                        "bigml_cache");
            } else {
                this.storage = String.format("%s%s%s",
                        System.getProperty(TMP_FOLDER_PROPERTY),
                        File.separator, "bigml_cache");
            }
        }

        storageFolder = new File(this.storage);
        if( !storageFolder.exists() ) {
            storageFolder.mkdirs();
        }
    }

    public String getStorage() {
        return storage;
    }

    public String getAbsoluteStorage() {
        return storageFolder.getAbsolutePath();
    }

    public boolean exists(String resource, String queryString) {
        return getResourceFile(resource, queryString).exists();
    }

    public synchronized JSONObject get(String resource, String queryString) {
        try {
            if( exists(resource, queryString) ) {
                StringBuilder fileContents = new StringBuilder();
                File resourceFile = getResourceFile(resource, queryString);
                BufferedReader input = new BufferedReader(new FileReader(
                        resourceFile));

                String sCurrentLine;
                while ((sCurrentLine = input.readLine()) != null) {
                    fileContents.append(sCurrentLine);
                }

                input.close();
                return (JSONObject) JSONValue.parse(fileContents.toString());
            }
        } catch (IOException e) {
            logger.error(String.format("Unable to read the cache file for resource: %s",
                    getResourceFile(resource, queryString).getAbsolutePath()));
        }

        return null;
    }

    public synchronized boolean put(String resource, String queryString, JSONObject value) {
        // If exists we first delete the current cached file
        if( exists(resource, queryString) ) {
            evict(resource, queryString);
        }

        return putIfNotExists(resource, queryString, value);
    }

    public synchronized boolean putIfNotExists(String resource, String queryString, JSONObject value) {
        try {
            if( !exists(resource, queryString) ) {
                File resourceFile = getResourceFile(resource, queryString);
                BufferedWriter output = new BufferedWriter(new FileWriter(
                        resourceFile));

                output.write(JSONValue.toJSONString(value));

                output.flush();
                output.close();

                return true;
            }
        } catch (IOException e) {
            logger.error(String.format("Unable to read the cache file for resource: %s",
                    getResourceFile(resource, queryString).getAbsolutePath()));
        }

        return false;
    }

    public synchronized boolean evict(String resource, String queryString) {
        try {
            // If exists we first delete the current cached file
            if( exists(resource, queryString) ) {
                forceDelete(getResourceFile(resource, queryString));
                return true;
            }
        } catch (IOException e) {
            logger.error(String.format("Unable to evict the resource with file: %s",
                    getResourceFile(resource, queryString).getAbsolutePath()));
        }

        return false;
    }

    public synchronized void cleanCache() throws IOException {
        final File[] files = storageFolder.listFiles();
        if( files != null ) {
            for (File file : files) {
                forceDelete(file);
            }
        }
    }

    protected void forceDelete(File file) throws IOException {
        boolean filePresent = file.exists();
        if (!file.delete()) {
            if (!filePresent){
                throw new FileNotFoundException("File does not exist: " + file);
            }
            final String message = "Unable to delete file: " + file;
            throw new IOException(message);
        }
    }

    protected File getResourceFile(String resource, String queryString) {
        if( queryString != null && queryString.length() > 0 ) {
            queryString = queryString.replaceAll("=","_");
            queryString = queryString.replaceAll("&","_");
            queryString = queryString.replaceAll(";","_");
        } else {
            queryString = "allfields";
        }

        String resourceFile = String.format("%s%s%s_%s", storageFolder.getAbsolutePath(),
                File.separator, resource.replaceAll("/", "_"), queryString);
        return new File(resourceFile);
    }
}
