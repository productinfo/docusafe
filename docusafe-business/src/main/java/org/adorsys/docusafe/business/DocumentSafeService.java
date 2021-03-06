package org.adorsys.docusafe.business;

import org.adorsys.docusafe.business.types.MoveType;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQNWithUserMetaData;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentStream;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.OverwriteFlag;
import org.adorsys.encobject.types.PublicKeyJWK;

/**
 * Created by peter on 19.01.18 at 16:30.
 */
public interface DocumentSafeService {
    /**
     * User
     */
    void createUser(UserIDAuth userIDAuth);

    void destroyUser(UserIDAuth userIDAuth);

    boolean userExists(UserID userID);

    /**
     * returns the user public encryption key of the given user.
     */
    PublicKeyJWK findPublicEncryptionKey(UserID userID);

    /**
     * Document
     */
    void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument);

    DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void storeDocumentStream(UserIDAuth userIDAuth, DSDocumentStream dsDocumentStream);

    DSDocumentStream readDocumentStream(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void deleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN);

    BucketContentFQNWithUserMetaData list(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag);

    /**
     * InboxStuff
     */
    BucketContentFQNWithUserMetaData listInbox(UserIDAuth userIDAuth);

    void writeDocumentToInboxOfUser(UserID receiverUserID, DSDocument document, DocumentFQN destDocumentFQN);

    DSDocument readDocumentFromInbox(UserIDAuth userIDAuth, DocumentFQN source);

    void deleteDocumentFromInbox(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    /**
     * conveniance methods
     */
    void moveDocumnetToInboxOfUser(UserIDAuth userIDAuth, UserID receiverUserID, DocumentFQN sourceDocumentFQN, DocumentFQN destDocumentFQN, MoveType moveType);

    DSDocument moveDocumentFromInbox(UserIDAuth userIDAuth, DocumentFQN source, DocumentFQN destination, OverwriteFlag overwriteFlag);
}

