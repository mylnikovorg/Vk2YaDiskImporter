/*
 * Лицензионное соглашение на использование набора средств разработки
 * «SDK Яндекс.Диска» доступно по адресу: http://legal.yandex.ru/sdk_agreement
 *
 */

package sdk.src.com.yandex.disk.client.exceptions;

public class WebdavFileNotFoundException extends WebdavException {
    public WebdavFileNotFoundException(String detailMessage) {
        super(detailMessage);
    }
}
