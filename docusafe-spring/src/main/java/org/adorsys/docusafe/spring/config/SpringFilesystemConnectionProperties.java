package org.adorsys.docusafe.spring.config;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.types.connection.FilesystemBasedirectoryName;
import org.adorsys.encobject.types.properties.FilesystemConnectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Created by peter on 05.10.18.
 */
@Component
@ConfigurationProperties(prefix = "docusafe.storeconnection.filesystem")
@Validated
public class SpringFilesystemConnectionProperties extends SpringConnectionPropertiesImpl implements FilesystemConnectionProperties {
    private final static Logger LOGGER = LoggerFactory.getLogger(SpringFilesystemConnectionProperties.class);
    public final static String template = "\n" +
            "docusafe:\n" +
            "  storeconnection:\n" +
            "    filesystem:\n" +
            "      basedir: (mandatory)\n" +
            SpringConnectionPropertiesImpl.template;


    private String basedir;

    @Override
    public FilesystemBasedirectoryName getFilesystemBasedirectoryName() {
        if (basedir == null) {
            throw new BaseException("basedir must not be null");
        }
        LOGGER.debug("basedir:" + new FilesystemBasedirectoryName(basedir));
        return new FilesystemBasedirectoryName(basedir);
    }

    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }
}