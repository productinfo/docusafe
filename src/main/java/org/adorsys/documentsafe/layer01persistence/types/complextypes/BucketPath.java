package org.adorsys.documentsafe.layer01persistence.types.complextypes;

import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * Created by peter on 16.01.18.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class BucketPath {
    private final static Logger LOGGER = LoggerFactory.getLogger(BucketPath.class);

    List<BucketName> buckets = new ArrayList<>();

    public BucketPath() {
    }

    public BucketPath(String path) {
        buckets = split(path);
    }

    public BucketPath set(BucketName bucketName) {
        buckets.clear();
        return sub(bucketName);
    }

    public BucketPath sub(BucketName bucketName) {
        buckets.add(bucketName);
        return this;
    }

    public String getObjectHandlePath() {
        return buckets.stream().map(b -> b.getValue()).collect(Collectors.joining(BucketName.BUCKET_SEPARATOR));
    }

    private static List<BucketName> split(String fullBucketPath) {
        List<BucketName> list = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(fullBucketPath, BucketName.BUCKET_SEPARATOR);
        while (st.hasMoreElements()) {
            list.add(new BucketName(st.nextToken()));
        }
        return list;
    }
}