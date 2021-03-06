package org.adorsys.docusafe.service.impl;

import org.adorsys.docusafe.service.KeySourceService;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.service.api.KeyStore2KeySourceHelper;
import org.adorsys.encobject.service.api.KeystorePersistence;
import org.adorsys.encobject.service.impl.BlobStoreKeystorePersistenceImpl;
import org.adorsys.encobject.types.PublicKeyJWK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.jwk.JWK;

public class KeySourceServiceImpl implements KeySourceService {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeySourceServiceImpl.class);
    private KeystorePersistence keystorePersistence;

	public KeySourceServiceImpl(ExtendedStoreConnection extendedStoreConnection) {
        this.keystorePersistence = new BlobStoreKeystorePersistenceImpl(extendedStoreConnection);
	}

	@Override
	public PublicKeyJWK findPublicEncryptionKey(KeyStoreAccess keyStoreAccess) {
		PublicKeyJWK publicKeyJWK = KeyStore2KeySourceHelper.getPublicKeyJWK(keystorePersistence, keyStoreAccess);
        LOGGER.debug("Found public encryption key JWK :" + publicKeyJWK.getValue().getKeyID());
        return publicKeyJWK;
	}

	@Override
	public KeySource getPrivateKeySource(KeyStoreAccess keyStoreAccess) {
		return KeyStore2KeySourceHelper.getForPrivateKey(keystorePersistence, keyStoreAccess);
	}

}