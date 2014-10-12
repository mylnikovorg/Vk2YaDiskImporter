package org.mylnikov.vk2yadisk;

import org.apache.commons.codec.digest.DigestUtils;
import sdk.src.com.yandex.disk.client.Credentials;
import sdk.src.com.yandex.disk.client.ProgressListener;
import sdk.src.com.yandex.disk.client.TransportClient;
import sdk.src.com.yandex.disk.client.exceptions.*;

import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.HashMap;

/**
 * Created by root on 10/12/14.
 */
public class YaDiskImport {
    String user, token;

    public YaDiskImport(String user, String token) {
        this.user = user;
        this.token = token;
    }

    public void UploadFilesToYaDisk(AbstractList<HashMap<String, String>> files, String directoryYaDisk, String tmpDir, int fileAmountLimit) {
        String directoryAction = DigestUtils.md5Hex("mylnikov" + System.currentTimeMillis() + Math.random());
        TransportClient diskClient = null;
        try {
            diskClient = TransportClient.getInstance(new Credentials(user, token));
            diskClient.makeFolder(directoryYaDisk + "/" + directoryAction);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DuplicateFolderException e) {
            e.printStackTrace();
        } catch (IntermediateFolderNotExistException e) {
            e.printStackTrace();
        } catch (WebdavUserNotInitialized webdavUserNotInitialized) {
            webdavUserNotInitialized.printStackTrace();
        } catch (PreconditionFailedException e) {
            e.printStackTrace();
        } catch (WebdavNotAuthorizedException e) {
            e.printStackTrace();
        } catch (ServerWebdavException e) {
            e.printStackTrace();
        } catch (UnsupportedMediaTypeException e) {
            e.printStackTrace();
        } catch (UnknownServerWebdavException e) {
            e.printStackTrace();
        } catch (WebdavClientInitException e) {
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

    private void uploadFileToYaDisk(HashMap<String, String> file, String directoryOnYaDisk, String tmpDirectory) {
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

        } catch (WebdavClientInitException e) {
            e.printStackTrace();
        } catch (ServerWebdavException e) {
            e.printStackTrace();
        } catch (UnknownServerWebdavException e) {
            e.printStackTrace();
        } catch (PreconditionFailedException e) {
            e.printStackTrace();
        } catch (IntermediateFolderNotExistException e) {
            e.printStackTrace();
        } catch (WebdavUserNotInitialized webdavUserNotInitialized) {
            webdavUserNotInitialized.printStackTrace();
        } catch (WebdavNotAuthorizedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File tmp = new File(tmpDirectory + "/" + filename);
        tmp.delete();
    }
}
