package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.KeyIDRestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 17:50:49.
 */
@XmlJavaTypeAdapter(KeyIDRestAdapter.class)
@XmlType
public class DocumnentKeyID extends BaseTypeString {
    public DocumnentKeyID() {}

    public DocumnentKeyID(String value) {
        super(value);
    }
}