package org.adorsys.docusafe.transactional.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.transactional.impl.LastCommitedTxID;

/**
 * Created by peter on 13.06.18 at 18:17.
 */
public class TxNotFoundException extends BaseException {
    public TxNotFoundException(DocumentFQN file, LastCommitedTxID lastTxID) {
        super(file.getValue() + " not found for last known transaction " + lastTxID.getValue());
    }
}