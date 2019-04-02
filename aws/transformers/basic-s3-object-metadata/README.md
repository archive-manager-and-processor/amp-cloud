# basic-s3-object-metadata
AMP Transformers for extracting and processing basic AWS S3 object metadata using AWS Lambda.

## Requirements

* A cloud-provider account on Amazon Web Service (AWS) with access to the following AWS services:
  - AWS Lambda
  - AWS S3 Storage
  - AWS Cloudwatch 
* Local development tools:
  - Apache Maven 3.2+
  - Java 8+
  - (Optional) AWS Command Line Interface (AWS CLI)

## Building the Code

First, build the source using Apache Maven by issuing the following command on your terminal:

```
mvn clean package
```

## Cloud Provider (AWS) Configuration

Once you have successfully built the code (see step above "Building the Code"), you will need to upload the built Java
Archive File (JAR) to Amazon's Lambda service and configure it there.

1. Log into the [AWS Management Console](http://aws.amazon.com)
2. Create an S3 Bucket (if you have not already)
2. Search for the "Lambda" service in the search bar or navigational menu
3. Once you've reached the AWS Lambda Management Console, click the "Create function" button
4. Select the "Author from scratch" option
5. Enter the following sample function information 
  - Function name: myS3ObjectMetadataTransformer (or customize as you see fit)
  - Runtime: select "Java 8"
  - Under Permissions, select an execution role per your account policies. If you do not know what to enter here, please
    contact your AWS account system administrator. Failure to specify the correct permission policy could prevent your
    transformer from running
6. Click "Create function", and you'll be navigated to a Lambda design view page
7. Click on your function in the design view and then scroll down to the bottom of the (web) page. You may need to
   scroll your browser view not just the Lambda panel.
8. In the "Function code" section, click the "Upload" button and select your built JAR file on your local machine, under 
   the ``target/`` directory.
9. In the "Handler" input box, enter (for example) "gov.nasa.pds.amp.BasicS3ObjectMetadataTransformer::handleRequest".  
   This is where you may specify any transformer that resides within the ``basic-s3-object-metadata`` source directory.
10. Enter any additional configuration as  you see fit in the rest of the page. Defaults are acceptable. Click the 
    "Save" button at the top-right. NOTE: the more memory you assign to your function, the faster Lambda will run. See 
    [pricing](https://aws.amazon.com/lambda/pricing/) 
11. Scroll back up to the design view, and add an S3 trigger as an input to your function, by clicking the "S3" trigger 
    name on the left hand menu.
12. Scroll down to configure your S3 trigger. 
13. Under "Bucket" select your chosen S3 bucket to trigger upon, and select the Event Type, Prefix, and Suffix as you
    see fit. Defaults are okay. Click "Add" when done.
14. Go back to the Lambda design view and select "Amazon Cloudwatch" as the output to the right of your function or 
    transformer.


## Running your Transformer

### Triggering an exectuion

To run the function, simply upload files to your AWS S3 chosen bucket, and this should trigger your transformer to run. 

TIP: it's best to upload using AWS-CLI's sync, which preserves directory hierarchies.   

For example, use the command ``aws s3 sync data_dir/ s3://amp-sample-bucket/`` to upload your local ``data_dir``
directory.

### Monitoring progress 

You may see the live progress /results of your transformer using Amazon CloudWatch 
(https://console.aws.amazon.com/cloudwatch/home#logs:)
