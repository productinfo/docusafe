package org.adorsys.resource.server.service;

import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.encobject.utils.TestKeyUtils;
import org.adorsys.resource.server.basetypes.DocumentContent;
import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.DocumentID;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.PersistentObjectWrapper;
import org.adorsys.resource.server.persistence.basetypes.BucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreName;
import org.junit.Assert;

import javax.security.auth.callback.CallbackHandler;

/**
 * Created by peter on 02.01.18.
 */
public class DocumentPersistenceServiceTest {
    private static BlobStoreContextFactory documentContextFactory;
    private static ExtendedObjectPersistence documentExtendedPersistence;
    private static ContainerPersistence containerPersistence;

    private BucketName documentBucketName = new BucketName("document-bucket");
    private DocumentID documentID = new DocumentID("document-id-123");
    private DocumentContent documentContent = new DocumentContent("Der Inhalt ist ein Affe".getBytes());


    public static void beforeClass() {
        TestKeyUtils.turnOffEncPolicy();

        documentContextFactory = new TestFsBlobStoreFactory();
        BlobStoreConnection blobStoreConnection = new BlobStoreConnection(documentContextFactory);
        documentExtendedPersistence = new ExtendedObjectPersistence(blobStoreConnection);
        containerPersistence = new ContainerPersistence(blobStoreConnection);
    }

    public static void afterClass() {

    }

    public void testPersistDocument(DocumentGuardService documentGuardService,
                                    CallbackHandler userKeystoreHandler,
                                    CallbackHandler keyPassHandler,
                                    DocumentGuardName documentGuardName) {
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceService(containerPersistence, documentExtendedPersistence, documentGuardService);
        documentPersistenceService.persistDocument(
                userKeystoreHandler,
                keyPassHandler,
                documentGuardName,
                documentBucketName,
                documentID,
                documentContent);
    }

    public void testPersistAndLoadDocument(DocumentGuardService documentGuardService,
                                                              CallbackHandler userKeystoreHandler,
                                                              CallbackHandler keyPassHandler,
                                                              KeyStoreName keyStoreName,
                                                              DocumentGuardName documentGuardName) {
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceService(containerPersistence, documentExtendedPersistence, documentGuardService);
        documentPersistenceService.persistDocument(
                userKeystoreHandler,
                keyPassHandler,
                documentGuardName,
                documentBucketName,
                documentID,
                documentContent);
        PersistentObjectWrapper persistentObjectWrapper = documentPersistenceService.loadDocument(
                keyStoreName,
                userKeystoreHandler,
                keyPassHandler,
                documentBucketName,
                documentID);

        DocumentContent readContent = new DocumentContent(persistentObjectWrapper.getData());
        Assert.assertEquals("Content of Document", this.documentContent.toString(), readContent.toString());
        System.out.println("Gelesenes Document enthält:" + readContent + " bzw " + new String(readContent.getValue()));
    }

}
