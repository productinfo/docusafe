package org.adorsys.resource.server.service;

import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.service.*;
import org.adorsys.encobject.utils.ShowKeyStore;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.encobject.utils.TestKeyUtils;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.resource.server.basetypes.BucketName;
import org.adorsys.resource.server.basetypes.UserID;
import org.junit.*;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Created by peter on 02.01.18.
 */
public class UserKeyStoreServiceTest {

    private static String container = UserKeyStoreServiceTest.class.getSimpleName();
    private static BlobStoreContextFactory storeContextFactory;
    private static KeystorePersistence keystorePersistence;
    private static ContainerPersistence containerPersistence;

    @BeforeClass
    public static void beforeClass(){
        TestKeyUtils.turnOffEncPolicy();
        storeContextFactory = new TestFsBlobStoreFactory();
        keystorePersistence = new BlobStoreKeystorePersistence(storeContextFactory);
        containerPersistence = new ContainerPersistence(new BlobStoreConnection(storeContextFactory));

        try {
            containerPersistence.creteContainer(container);
        } catch (ContainerExistsException e) {
            Assume.assumeNoException(e);
        }

    }

    @AfterClass
    public static void afterClass(){
        try {
            if(containerPersistence!=null && containerPersistence.containerExists(container))
                containerPersistence.deleteContainer(container);
        } catch (UnknownContainerException e) {
            Assume.assumeNoException(e);
        }
    }

    @Test
    public void testCreateUserKeyStore() throws CertificateException, NoSuchAlgorithmException, UnknownContainerException, MissingKeystoreProviderException, MissingKeyAlgorithmException, WrongKeystoreCredentialException, MissingKeystoreAlgorithmException, KeystoreNotFoundException, IOException, KeyStoreException, UnrecoverableKeyException {
        String keypasswordstring = "KeyPassword";
        String useridstring = "UserPeter";
        String bucketnamestring = container;

        UserKeyStoreService userKeyStoreService = new UserKeyStoreService(keystorePersistence);
        UserID userID = new UserID(useridstring);
        CallbackHandler userKeyStoreHandler = new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback call: callbacks) {
                    System.out.println("user key Store handler callback:" + call);
                }
            }
        };
        CallbackHandler keyPassHanlder = new PasswordCallbackHandler(keypasswordstring.toCharArray());
        BucketName bucketName = new BucketName(bucketnamestring);
        KeyStore userKeyStore = userKeyStoreService.createUserKeyStore(userID, userKeyStoreHandler, keyPassHanlder, bucketName);
        Assert.assertTrue(TestFsBlobStoreFactory.existsOnFs(container, useridstring + ".keystore"));
        Assert.assertEquals("Number of Entries", 15, userKeyStore.size());
        System.out.println(ShowKeyStore.toString(userKeyStore, keypasswordstring));
    }
}