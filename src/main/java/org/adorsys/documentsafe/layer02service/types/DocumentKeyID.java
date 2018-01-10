package org.adorsys.documentsafe.layer02service.types;

import org.adorsys.documentsafe.layer00common.basetypes.BaseTypeString;

/**
 * Created by peter on 23.12.2017 at 17:50:49.
 * Gehört immer zu einem DocumentKey, siehe auch DocumentKeyIDWithKey
 */
public class DocumentKeyID extends BaseTypeString {
    public DocumentKeyID() {}

    public DocumentKeyID(String value) {
        super(value);
    }
}