package org.adorsys.docusafe.business;

import com.googlecode.catchexception.CatchException;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.exceptions.UserIDDoesNotExistException;
import org.adorsys.docusafe.business.exceptions.WrongPasswordException;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.BucketContentFQNWithUserMetaData;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.exceptions.NoDocumentGuardExists;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.domain.UserMetaData;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 19.01.18 at 16:25.
 */
@SuppressWarnings("Duplicates")
public class BusinessTest extends BusinessTestBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(BusinessTest.class);

    @Test
    public void documentListMetadata_DOC_77() {
        UserIDAuth userIDAuth = createUser(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));

        DocumentFQN documentFQN = new DocumentFQN("first/document.txt");
        Assert.assertFalse(service.documentExists(userIDAuth, documentFQN));
        DocumentContent documentContent = new DocumentContent(("Einfach nur a bisserl Text").getBytes());
        UserMetaData userMetaData = new UserMetaData();
        userMetaData.put("first", "1");
        userMetaData.put("second", "2");
        DSDocument dsDocument = new DSDocument(documentFQN, documentContent, new DSDocumentMetaInfo(userMetaData));
        service.storeDocument(userIDAuth, dsDocument);
        Assert.assertTrue(service.documentExists(userIDAuth, documentFQN));
        BucketContentFQNWithUserMetaData list = service.list(userIDAuth, new DocumentDirectoryFQN(""), ListRecursiveFlag.TRUE);
        final Boolean found[] = new Boolean[1];
        found[0] = false;
        list.getFiles().stream().filter(file -> file.equals(documentFQN)).forEach(file -> {
            found[0] = true;
            LOGGER.info("found:" + file);
            list.getUserMetaData(file).keySet().forEach(key -> LOGGER.info("UserMetaData: " + key + " " + list.getUserMetaData(file).get(key)));
            for (String key : userMetaData.keySet()) {
                Assert.assertEquals(userMetaData.get(key), list.getUserMetaData(file).get(key));
            }
        });
        Assert.assertTrue(found.length == 1);
        Assert.assertTrue(found[0].equals(Boolean.TRUE));
    }

    @Test
    public void documentExistsTest_DOC_36() {
        UserIDAuth userIDAuth = createUser(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        DocumentFQN documentFQNReadme1 = new DocumentFQN("README.txt");
        DocumentFQN documentFQNReadme2 = new DocumentFQN("README2.txt");
        DocumentFQN documentFQNnewDir = new DocumentFQN("affe/README2.txt");
        Assert.assertTrue(service.documentExists(userIDAuth, documentFQNReadme1));
        Assert.assertFalse(service.documentExists(userIDAuth, documentFQNReadme2));
        Assert.assertFalse(service.documentExists(userIDAuth, documentFQNnewDir));
        UserIDAuth userIDAuth2 = new UserIDAuth(new UserID("UserPeter2"), new ReadKeyPassword("peterkey"));
        CatchException.catchException(() -> service.documentExists(userIDAuth2, documentFQNReadme1));
        Assert.assertNotNull(CatchException.caughtException());
        Assert.assertTrue(CatchException.caughtException() instanceof UserIDDoesNotExistException);
    }

    @Test
    public void performanceTest_DOC_29() {

        int REPEATS = 1;
        int i = 0;

        UserIDAuth userIDAuth = createUser();
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth.getUserID()));


        while (i > 0) {
            LOGGER.info("wait for visualVM profiler " + i);
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
            }
            i--;
        }

        for (int j = 0; j < REPEATS; j++) {
            DocumentFQN documentFQN = new DocumentFQN("first/next/document" + j + ".txt");
            Assert.assertFalse(service.documentExists(userIDAuth, documentFQN));
            DocumentContent documentContent = new DocumentContent(("Einfach nur a bisserl Text" + j).getBytes());
            DSDocument dsDocument = new DSDocument(documentFQN, documentContent, new DSDocumentMetaInfo());
            service.storeDocument(userIDAuth, dsDocument);
            Assert.assertTrue(service.documentExists(userIDAuth, documentFQN));
            DSDocument dsDocumentResult = service.readDocument(userIDAuth, documentFQN);
            LOGGER.debug("original  document:" + new String(documentContent.getValue()));
            LOGGER.debug("retrieved document:" + new String(dsDocumentResult.getDocumentContent().getValue()));
            Assert.assertEquals("document content ok", documentContent, dsDocumentResult.getDocumentContent());
        }
    }

    @Test
    public void sequenceDiagramTest() {

        UserIDAuth userIDAuth = new UserIDAuth(new UserID("user1"), new ReadKeyPassword("password1"));
        users.add(userIDAuth);
        service.createUser(userIDAuth);
    }

    @Test
    public void testCreateUser() {

        UserIDAuth userIDAuth = createUser();
        Assert.assertEquals("Anzahl der guards muss 1 betragen", 1, getNumberOfGuards(userIDAuth.getUserID()));
    }


    @Test
    public void loadDSDocument() {

        UserIDAuth userIDAuth = createUser(new UserID("affe"), new ReadKeyPassword("ab_irgendwas_cd"));
        DocumentFQN fqn = new DocumentFQN("README.txt");
        Assert.assertEquals("Anzahl der guards muss 1 betragen", 1, getNumberOfGuards(userIDAuth.getUserID()));
        // Dieser Read muss ok sein
        service.readDocument(userIDAuth, fqn);
        userIDAuth = new UserIDAuth(new UserID("affe"), new ReadKeyPassword("ab_123456789_cd"));
        Boolean catched = false;
        try {
            // Dieser Read muss fehlschlagen. Es gab einen Bug im Cache, wo statt des ReadKeyPassword der toString Text benutzt wurde.
            // Dieser ist aber mit **** ausgegraut, so dass alle Passworte bis auf Anfang und Ende gleich sind !!!
            service.readDocument(userIDAuth, fqn);
        } catch (BaseException e) {
            catched = true;
        }
        Assert.assertTrue(catched);
    }

    @Test
    public void deleteDocumentTest() {

        UserID userID = new UserID("DelPeter");
        Assert.assertFalse(service.userExists(userID));
        UserIDAuth userIDAuth = createUser(userID);
        Assert.assertTrue(service.userExists(userID));
        DocumentFQN fqn = new DocumentFQN("README.txt");
        service.readDocument(userIDAuth, fqn);
        Assert.assertTrue(service.documentExists(userIDAuth, fqn));
        service.deleteDocument(userIDAuth, fqn);
        Assert.assertFalse(service.documentExists(userIDAuth, fqn));
        CatchException.catchException(() -> service.readDocument(userIDAuth, fqn));
        Assert.assertNotNull(CatchException.caughtException());
    }

    // DOC-45, now the number guards still should be 1
    @Test
    public void storeDSDocumentInANewFolder() {

        UserIDAuth userIDAuth = createUser();
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth.getUserID()));

        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        DSDocument dsDocument1 = createDocument(userIDAuth, documentFQN);
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth.getUserID()));
        readDocument(userIDAuth, documentFQN, dsDocument1.getDocumentContent());

        DSDocument dsDocument2 = createDocument(userIDAuth, new DocumentFQN("first/next/another-new-document.txt"));
        readDocument(userIDAuth, dsDocument2.getDocumentFQN(), dsDocument2.getDocumentContent());
    }

    /**
     * Zunächst erstellt peter ein document.
     * Dann darf Francis das Document auch lesen, aber nicht schreiben.
     * Dann darf er auch schreiben.
     * Schließlich darf er garnicht mehr drauf zugreifen.
     * Zuletzt wird gezeigt, dass Peter das Dokument nach wie vor schreiben darf.
     */
    @Test
    public void tryOverwriteGrantAccessToFolder() {
        // Fliegt mit DOC-82 eh alles raus
    }

    @Test
    public void tryToDeleteUserWithWrongPassword() {

        UserIDAuth userIDAuthPeter = createUser(new UserID("peter"), new ReadKeyPassword("keyPasswordForPeter"));
        UserIDAuth wrongUserIDAuthPeter = new UserIDAuth(userIDAuthPeter.getUserID(), new ReadKeyPassword("WRONGPASSWORD"));
        boolean exceptionRaised = false;
        try {
            service.destroyUser(wrongUserIDAuthPeter);
        } catch (WrongPasswordException e) {
            LOGGER.debug("THIS EXCEPTION WAS EXPECTED");
            LOGGER.info("caught exception");
            exceptionRaised = true;
        }
        Assert.assertTrue(exceptionRaised);
        service.destroyUser(userIDAuthPeter);
        users.clear();

    }

    @Test
    public void checkDirectoryListings() {
        int DEPTH = 4;
        int SUBDIRS = 4;
        int FILES = 4;
        // TODO actually a performance teste
        DEPTH = 2;
        SUBDIRS = 2;
        FILES = 1;
        UserIDAuth userIDAuth = createUser();
        DocumentDirectoryFQN dir = new DocumentDirectoryFQN("/");
        createDirectoryWithSubdirectories(DEPTH, userIDAuth, dir, SUBDIRS, FILES);
        int expectedFilesInRootNonRecursive = FILES + 1; // README.txt is created in every root
        int expectedDirectoriesInRootNonRecursive = SUBDIRS;
        int dirs = 0;
        for (int i = 1; i < DEPTH; i++) {
            dirs = dirs + (int) Math.pow(SUBDIRS, i);
        }
        int expectedDirsRecursive = dirs;
        int expectedFilesRecursive = dirs * FILES + expectedFilesInRootNonRecursive;

        LOGGER.info("expectedFilesInRootNonRecursive:       " + expectedDirectoriesInRootNonRecursive);
        LOGGER.info("expectedDirectoriesInRootNonRecursive: " + expectedFilesInRootNonRecursive);
        LOGGER.info("expectedDirsRecursive:                 " + expectedDirsRecursive);
        LOGGER.info("expectedFilesRecursive:                " + expectedFilesRecursive);
        {
            BucketContentFQN list = service.list(userIDAuth, dir, ListRecursiveFlag.FALSE);
            list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
            list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
            Assert.assertEquals(expectedDirectoriesInRootNonRecursive, list.getDirectories().size());
            Assert.assertEquals(expectedFilesInRootNonRecursive, list.getFiles().size());
        }
        {
            BucketContentFQN list = service.list(userIDAuth, dir, ListRecursiveFlag.TRUE);
            list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
            list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
            Assert.assertEquals(expectedDirsRecursive, list.getDirectories().size());
            Assert.assertEquals(expectedFilesRecursive, list.getFiles().size());
        }
    }

    @Test
    public void checkRootDirectoryListings() {

        UserIDAuth userIDAuth = createUser();
        DocumentDirectoryFQN dir = new DocumentDirectoryFQN("/");
        createDirectoryWithSubdirectories(2, userIDAuth, dir, 1, 1);
        {
            BucketContentFQN list = service.list(userIDAuth, dir, ListRecursiveFlag.FALSE);
            list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
            list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
            Assert.assertEquals(1, list.getDirectories().size());
            Assert.assertEquals(2, list.getFiles().size());
        }
        {
            BucketContentFQN list = service.list(userIDAuth, dir, ListRecursiveFlag.TRUE);
            list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
            list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
            Assert.assertEquals(1, list.getDirectories().size());
            Assert.assertEquals(3, list.getFiles().size());
        }
    }

    @Test
    public void checkRootDirectoryListingVerySimple() {

        UserIDAuth userIDAuth = createUser();
        DocumentDirectoryFQN dir = new DocumentDirectoryFQN("/");
        DocumentFQN documentFQN = new DocumentFQN("/affe.txt");
        createDocument(userIDAuth, documentFQN);

        BucketContentFQN list = service.list(userIDAuth, dir, ListRecursiveFlag.FALSE);
        list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
        list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
        Assert.assertEquals(0, list.getDirectories().size());
        Assert.assertEquals(2, list.getFiles().size());

        list = service.list(userIDAuth, dir, ListRecursiveFlag.TRUE);
        list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
        list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
        Assert.assertEquals(0, list.getDirectories().size());
        Assert.assertEquals(2, list.getFiles().size());
    }

    @Test
    public void checkRootDirectoryListingSimple() {

        UserIDAuth userIDAuth = createUser();
        DocumentDirectoryFQN dir = new DocumentDirectoryFQN("/anyfolder");
        DocumentFQN documentFQN = new DocumentFQN("/anyfolder/affe.txt");
        createDocument(userIDAuth, documentFQN);

        BucketContentFQN list = service.list(userIDAuth, dir, ListRecursiveFlag.FALSE);
        list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
        list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
        Assert.assertEquals(0, list.getDirectories().size());
        Assert.assertEquals(1, list.getFiles().size());

        list = service.list(userIDAuth, dir, ListRecursiveFlag.TRUE);
        list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
        list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
        Assert.assertEquals(0, list.getDirectories().size());
        Assert.assertEquals(1, list.getFiles().size());
    }


}
