/*
 * Лицензионное соглашение на использование набора средств разработки
 * «SDK Яндекс.Диска» доступно по адресу: http://legal.yandex.ru/sdk_agreement
 *
 */

package sdk.src.com.yandex.disk.client;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;

public class SSLSocketFactoryWithTimeout extends SSLSocketFactory {
    private int timeout;

    public SSLSocketFactoryWithTimeout(int timeout)
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super((KeyStore) null);
        this.timeout = timeout;
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
            throws IOException, UnknownHostException {
        return setSocketParams(super.createSocket(socket, host, port, autoClose));
    }

    @Override
    public Socket createSocket()
            throws IOException {
        return setSocketParams(super.createSocket());
    }

    private Socket setSocketParams(Socket socket)
            throws IOException {
        socket.setSoTimeout(timeout);
        return socket;
    }
}