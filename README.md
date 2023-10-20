# AWS-Object-Text-Recognition-Pipeline
<p><b>Goal:</b> The purpose of this individual assignment is to learn how to use the Amazon AWS cloud platform and how to develop an AWS application that uses existing cloud services. Specifically, you will learn: (1) how to create VMs (EC2 instances) in the cloud; (2) how to use cloud storage (S3) in your applications; (3) how to communicate between VMs using a queue service (SQS); (4) how to program distributed applications in Java on Linux VMs in the cloud; and (5) how to use a machine learning service (AWS Rekognition) in the cloud.</p>

<b>Description:</b> You have to build an image recognition pipeline in AWS, using two EC2 instances, S3, SQS, and Rekognition. The assignment must be done in Java on Amazon Linux VMs. For the rest of the description, you should refer to the figure below:

![image](https://github.com/CK-ghub/AWS-Object-Text-Recognition-Pipeline/assets/69519007/e4358681-7eca-446d-bd90-6fb5a27c7632)

You have to create 2 EC2 instances (EC2 A and B in the figure), with Amazon Linux AMI, that will work in parallel. Each instance will run a Java application. Instance A will read 10 images from an S3 bucket that we created (https://njit-cs-643.s3.us-east-1.amazonaws.com) and perform object detection in the images. When a car is detected using Rekognition, with confidence higher than 90%, the index of that image (e.g., 2.jpg) is stored in SQS. Instance B reads indexes of images from SQS as soon as these indexes become available in the queue, and performs text recognition on these images (i.e., downloads them from S3 one by one and uses Rekognition for text recognition). Note that the two instances work in parallel: for example, instance A is processing image 3, while instance B is processing image 1 that was recognized as a car by instance A. When instance A terminates its image processing, it adds index -1 to the queue to signal to instance B that no more indexes will come. When instance B finishes, it prints to a file, in its associated EBS, the indexes of the images that have both cars and text, and also prints the actual text in each image next to its index.

# Approached Solution

## Initial Setup
1) Login to the AWS Academy account with the credentials sent by your professor and go to your course. Access the vocareum learner lab in Modules > Launch AWS Learner Lab.
2) Start the AWS lab and console and copy the aws_access_key_id, aws_secret_access_key, aws_session_token in AWS Details and paste it into your credentials file in ~/.aws/credentials. The 'config' file has the region (eg. us-east-1 for N. Virginia) and output format (eg. json) stored. 
3) Along with that download the labsuser.pem file under SSH Key to authenticate later before you can access your EC2 instances through your terminal.
4) Once you start your lab, open your AWS Management console, search for EC2 under Services, and follow the steps below to create your two instances.

## Creating EC2 Instances That Work Parallely
1) Once you're in the EC2 page, click on 'Launch Instance' and enter the name of the EC2 instances you want to create.
2) Under AMI, select 'Amazon Linux 2 AMI (HVM) - Kernel 5.10, SSD Volume Type'.
3) Select t2.micro as instance type.
4) Under Key Pair (login), create a new key-pair of type RSA and format .ppk (if you're using PuTTY for SSH, this can also be generated from .pem later using PuTTYgen) or .pem (for OpenSSH). Do not forget to save this key safely locally. (For eg. EC2_Key.ppk or .pem which will later be converted)
5) Under Network settings, edit to create security groups with the following settings.<br>
    -> SSH with Source type My IP<br>
    -> HTTP with Source type My IP<br>
    -> HTTPS with Source type My IP<br>
6) You can just leave the settings as is, under Configure Storage and Advanced details.
7) On the right side, under the summary, choose Number of Instances as 2 to launch two EC2 instances in parallel.
8) Click on Launch Instance to view your instances

   ![image](https://github.com/CK-ghub/AWS-Object-Text-Recognition-Pipeline/assets/69519007/9ed0a825-b6fb-4358-a9a8-d1ce4bd275d8)

## Working with JAVA programs
1) Once the instances are setup, we will have to create the source code for two different applications. One is to recognize objects and the other is to recognize text. For more details, read the project description.
2) Once you have the source code ready for both applications (ObjectRecognition.java and TextRecognition.java), we need to package them into executable JAR files.
3) We will now use Apache Maven to configure and build AWS SDK for the application. If you don't have Maven installed, you can visit http://maven.apache.org/ to download the binaries and extract the folders. You need to add the path to the bin folder inside to system variable PATH, to be able to access maven's mvn command. 
4) To first create a Maven package, open the terminal and run the command below:<br>
![image](https://github.com/CK-ghub/AWS-Object-Text-Recognition-Pipeline/assets/69519007/22770162-5a86-411f-b001-290e78b86fb1)<br>
Replace org.example.basicapp with the full package namespace of this project, and myapp with the name of this project, which will later become the name of this directory. Repeat the same for the other application. (ObjectRecognition and TextRecognition will be two different folders as packages).
5) Replace App.java in myapp/src/main/java/org/example/basicapp/ with your source code.<br>
For example replace App.java with ObjectRecognition.java in ObjectRecognition/src/main/java/com/aws/ec2/ where myapp is 'ObjectRecognition' and org.example.basicapp is 'com.aws.ec2'. Make sure that the name is the same as the public class in your source code.
6) To configure and use AWS SDK, we ned to declare dependencies in your project's root, the pom.xml file.<br>
   You can add dependencies using the <dependency> tag under <dependencies> as shown below. <br>
   ![image](https://github.com/CK-ghub/AWS-Object-Text-Recognition-Pipeline/assets/69519007/82f0ea38-b67d-4644-815f-058bc816128f)<br>
7) Once we have the dependencies setup, we proceed to create the JAR file.
8) Package the application using the command 'mvn clean package' followed by 'mvm clean install'.
9) You need to do the above steps for both the applications. 
10) We can now test our applications by cd-ing into the target folder where the JAR file is located and use the command 'java -jar YourApplication.jar'. Sometimes the .jar files are stored with version names. You can specify whether or not to have the name in the <build> section in pom.xml and re-package.
11) In case you get import/package errors while executing the application, in my case, we can alternatively use automation tools like Apache Maven that offers plugins. This creates an additional jar with all dependencies. Refer the screenshot below to see the structure. <br>
![image](https://github.com/CK-ghub/AWS-Object-Text-Recognition-Pipeline/assets/69519007/b7ceba01-fe6f-49da-806b-0b26b069b662)<br>


