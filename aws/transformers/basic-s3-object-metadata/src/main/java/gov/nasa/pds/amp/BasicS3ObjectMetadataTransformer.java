/*
 * Copyright 2019, by the California Institute of Technology. ALL RIGHTS RESERVED. United States Government
 * Sponsorship acknowledged. Any commercial use must be negotiated with the Office of Technology Transfer at the
 * California Institute of Technology.
 * This software may be subject to U.S. export control laws. By accepting this software, the user agrees to comply with
 * all applicable U.S. export laws and regulations. User has the responsibility to obtain export licenses, or other
 * export authority as may be required before exporting such information to foreign countries or providing access to
 * foreign persons.
 */
package gov.nasa.pds.amp;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * An AMP Amazon Web Services (AWS) Cloud transformer that extracts basic object-store metadata from a given object's
 * path on Amazon S3 and prints to stdout. This transformer leverages the AWS Lambda functionality to execute on
 * triggers created by object modifications in a particular AWS S3 bucket.
 *
 * @author riverma
 */
public class BasicS3ObjectMetadataTransformer implements
        RequestHandler<S3Event, String> {

    public String handleRequest(S3Event s3event, Context context) {
        try {
            // Initialize Lambda and S3 functionality
            LambdaLogger logger = context.getLogger();
            S3EventNotification.S3EventNotificationRecord record = s3event.getRecords().get(0);
            String srcBucket = record.getS3().getBucket().getName();

            // Object key may have spaces or unicode non-ASCII characters, so decode from UTF-8.
            String srcKey = record.getS3().getObject().getKey();
            srcKey = URLDecoder.decode(srcKey, "UTF-8");

            logger.log("--- AMP processing file: "+srcBucket+"/"+srcKey+" ---");

            // Collect the file from S3 and store into a stream
            AmazonS3 s3Client = new AmazonS3Client();
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(
                    srcBucket, srcKey));
            InputStream objectDataStream = s3Object.getObjectContent();
            ObjectMetadata objectMetadata = s3Object.getObjectMetadata();

            // Extract custom metadata from object
            HashMap<String, String> hashMap = extractMetadata(s3Object);
            logger.log("  METADATA:");
            for (String key : hashMap.keySet()) {
                logger.log("  "+key+" => "+hashMap.get(key));
            }

            // Delete file from S3 - note should do this based on configuration in the future
            s3Client.deleteObject(new DeleteObjectRequest(srcBucket, srcKey));
            logger.log("--- AMP finished extracting from and deleting file ---");

            return "Ok";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected HashMap<String, String> extractMetadata(S3Object s3Object) {

        // collect metadata
        Path artifactPath = FileSystems.getDefault().getPath(s3Object.getKey());
        HashMap<String, String> hashMap = new HashMap<String, String>();

        hashMap.put("path", artifactPath.toString());
        hashMap.put("md5", s3Object.getObjectMetadata().getETag());
        hashMap.put("size", String.valueOf(s3Object.getObjectMetadata().getContentLength()));
        hashMap.put("contentType", s3Object.getObjectMetadata().getContentType());

        return hashMap;
    }

}
