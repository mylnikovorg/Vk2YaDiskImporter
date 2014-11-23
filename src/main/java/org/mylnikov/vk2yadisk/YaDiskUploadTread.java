package org.mylnikov.vk2yadisk;
import org.apache.commons.codec.digest.DigestUtils;
import sdk.src.com.yandex.disk.client.Credentials;
import sdk.src.com.yandex.disk.client.ProgressListener;
import sdk.src.com.yandex.disk.client.TransportClient;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by alex on 11/23/14.
 */
public class YaDiskUploadTread implements Callable<String> {

    HashMap<String, String> file;
    String directoryOnYaDisk, tmpDirectory;
    Credentials accessCredentials;


    public YaDiskUploadTread(HashMap<String, String> file, String directoryOnYaDisk, String tmpDirectory, Credentials accessCredentials) {
        this.file = file;
        this.directoryOnYaDisk = directoryOnYaDisk;
        this.tmpDirectory = tmpDirectory;
        this.accessCredentials=accessCredentials;
    }

    @Override
    public String call() throws Exception {
        ProgressListener pl = new ProgressListener() {
            @Override
            public void updateProgress(long loaded, long total) {

            }

            @Override
            public boolean hasCancelled() {
                return false;
            }
        };
        TransportClient diskClient = null;
        String filename = DigestUtils.md5Hex("mylnikovfile" + System.currentTimeMillis() + Math.random()) + "." + file.get("ext");
        VkClient.getFile(file.get("url"), tmpDirectory + "/" + filename);
        try {
            diskClient = TransportClient.getUploadInstance(accessCredentials);
            diskClient.uploadFile(tmpDirectory + "/" + filename, file.get("title") + "." + file.get("ext"), directoryOnYaDisk, pl);
            //System.out.print();

        } catch (Exception e) {
            e.printStackTrace();
        }
        File tmp = new File(tmpDirectory + "/" + filename);
        tmp.delete();

        //return the thread name executing this callable task
        return Thread.currentThread().getName();
    }
}




