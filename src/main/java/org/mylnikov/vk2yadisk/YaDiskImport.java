package org.mylnikov.vk2yadisk;

import org.apache.commons.codec.digest.DigestUtils;
import sdk.src.com.yandex.disk.client.Credentials;
import sdk.src.com.yandex.disk.client.ProgressListener;
import sdk.src.com.yandex.disk.client.TransportClient;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by root on 10/12/14.
 */
public class YaDiskImport {
    String user, token;

    public YaDiskImport(String user, String token) {
        this.user = user;
        this.token = token;
    }

    public void UploadFilesToYaDisk(AbstractList<HashMap<String, String>> files,
                                    String directoryYaDisk,
                                    String tmpDir,
                                    int fileAmountLimit) {
        String directoryAction = DigestUtils.md5Hex("mylnikov" + System.currentTimeMillis() + Math.random());
        TransportClient diskClient = null;
        try {
            diskClient = TransportClient.getInstance(new Credentials(user, token));
            diskClient.makeFolder(directoryYaDisk);
            diskClient.makeFolder(directoryYaDisk + "/" + directoryAction);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int i = 0;
        for (HashMap<String, String> one : files) {
            if (i >= fileAmountLimit)
                break;
            this.uploadFileToYaDisk(one, directoryYaDisk + "/" + directoryAction + "/", tmpDir);
            i++;
        }

    }

    public int UploadFilesToYaDisk(AbstractList<HashMap<String, String>> files,
                                    String directoryYaDisk,
                                    String groupName,
                                    String tmpDir,
                                    int fileAmountLimit) {

        int fileCount=0;
        final int sizeLimit = 5242880; //Max file size for import
        if (files.size() > 0) {
            Collections.shuffle(files, new Random(System.nanoTime()));
            String directoryAction = groupName;
            TransportClient diskClient = null;
            try {
                diskClient = TransportClient.getInstance(new Credentials(user, token));
                diskClient.makeFolder(directoryYaDisk);
                diskClient.makeFolder(directoryYaDisk + "/" + directoryAction);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ExecutorService executor = Executors.newFixedThreadPool(10);
            ArrayList<FutureTask<String>> mass = new ArrayList<>();
            Credentials sessionCred = new Credentials(user, token);
            for (HashMap<String, String> one : files) {

                if (fileCount >= fileAmountLimit )
                    break;
                if(Integer.parseInt(one.get("size")) <= sizeLimit) {
                    YaDiskUploadTread tread = new YaDiskUploadTread(one, directoryYaDisk + "/" + directoryAction + "/", tmpDir, sessionCred);
                    FutureTask<String> task = new FutureTask<String>(tread);
                    mass.add(task);
                    executor.execute(task);
                    fileCount++;
                }
            }
            while(true)
            {
                int out=0;
                for (FutureTask<String> mas : mass) {
                    if (!mas.isDone()) {
                        out = 1;
                        continue;
                    }
                }
                if (out==0)
                    break;
            }
        }
        return fileCount;

    }

    @Deprecated
    private void uploadFileToYaDisk(HashMap<String, String> file,
                                    String directoryOnYaDisk,
                                    String tmpDirectory) {
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
            diskClient = TransportClient.getUploadInstance(new Credentials(user, token));
            diskClient.uploadFile(tmpDirectory + "/" + filename, file.get("title") + "." + file.get("ext"), directoryOnYaDisk, pl);
            //System.out.print();

        } catch (Exception e) {
            e.printStackTrace();
        }
        File tmp = new File(tmpDirectory + "/" + filename);
        tmp.delete();
    }
}
