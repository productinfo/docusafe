package org.adorsys.docusafe.service;

import org.adorsys.docusafe.service.impl.DocumentPersistenceServiceImpl;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.service.types.complextypes.DocumentBucketPath;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.OverwriteFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by peter on 02.01.18.
 */
public class DocumentPersistenceServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentPersistenceServiceTest.class);

    private ExtendedStoreConnection extendedStoreConnection;
    private Set<DocumentBucketPath> createdBuckets = new HashSet<>();

    public DocumentPersistenceServiceTest(ExtendedStoreConnection extendedStoreConnection) {
        this.extendedStoreConnection = extendedStoreConnection;
    }

    public DocumentStuff testPersistDocument(DocumentGuardService documentGuardService,
                                             DocumentBucketPath documentBucketPath,
                                             DocumentKeyIDWithKey documentKeyIDWithKey,
                                             DocumentContent documentContent) {
        return testPersistDocument(documentGuardService, documentBucketPath, documentKeyIDWithKey, documentContent, OverwriteFlag.FALSE);
    }
    public DocumentStuff testPersistDocument(DocumentGuardService documentGuardService,
                                             DocumentBucketPath documentBucketPath,
                                             DocumentKeyIDWithKey documentKeyIDWithKey,
                                             DocumentContent documentContent,
                                             OverwriteFlag overwriteFlag) {
        extendedStoreConnection.createContainer(documentBucketPath.getBucketDirectory());
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceServiceImpl(extendedStoreConnection, null);
        documentPersistenceService.encryptAndPersistDocument(
                documentKeyIDWithKey,
                documentBucketPath,
                overwriteFlag,
                new SimplePayloadImpl(new SimpleStorageMetadataImpl(), true, documentContent.getValue()));
        createdBuckets.add(documentBucketPath);
        AllServiceTest.buckets.add(documentBucketPath.getBucketDirectory());
        return new DocumentStuff(documentBucketPath);
    }

    public Payload testLoadDocument(DocumentGuardService documentGuardService,
                                 KeyStoreAccess keyStoreAccess,
                                 DocumentBucketPath documentBucketPath) {
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceServiceImpl(extendedStoreConnection, null);
        StorageMetadata storageMetadata = extendedStoreConnection.getStorageMetadata(documentBucketPath);
/*
        if (UserMetaDataUtil.isNotEncrypted(storageMetadata.getUserMetadata())) {
            return documentPersistenceService.loadDocument(storageMetadata, documentBucketPath);
        }
        */
        Payload payload = documentPersistenceService.loadAndDecryptDocument(
                storageMetadata,
                keyStoreAccess,
                documentBucketPath);
        LOGGER.debug("Gelesenes Document enthält:" + payload.getData() + " bzw " + new String(payload.getData()));
        return payload;
    }

    public static class DocumentStuff {
        public final DocumentBucketPath documentBucketPath;

        public DocumentStuff(DocumentBucketPath documentBucketPath) {
            this.documentBucketPath = documentBucketPath;
        }
    }
}
