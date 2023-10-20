# AWS-Object-Text-Recognition-Pipeline
<p><b>Goal:</b> The purpose of this individual assignment is to learn how to use the Amazon AWS cloud platform and how to develop an AWS application that uses existing cloud services. Specifically, you will learn: (1) how to create VMs (EC2 instances) in the cloud; (2) how to use cloud storage (S3) in your applications; (3) how to communicate between VMs using a queue service (SQS); (4) how to program distributed applications in Java on Linux VMs in the cloud; and (5) how to use a machine learning service (AWS Rekognition) in the cloud.</p>

<b>Description:</b> You have to build an image recognition pipeline in AWS, using two EC2 instances, S3, SQS, and Rekognition. The assignment must be done in Java on Amazon Linux VMs. For the rest of the description, you should refer to the figure below:

![image](https://github.com/CK-ghub/AWS-Object-Text-Recognition-Pipeline/assets/69519007/e4358681-7eca-446d-bd90-6fb5a27c7632)

You have to create 2 EC2 instances (EC2 A and B in the figure), with Amazon Linux AMI, that will work in parallel. Each instance will run a Java application. Instance A will read 10 images from an S3 bucket that we created (https://njit-cs-643.s3.us-east-1.amazonaws.com) and perform object detection in the images. When a car is detected using Rekognition, with confidence higher than 90%, the index of that image (e.g., 2.jpg) is stored in SQS. Instance B reads indexes of images from SQS as soon as these indexes become available in the queue, and performs text recognition on these images (i.e., downloads them from S3 one by one and uses Rekognition for text recognition). Note that the two instances work in parallel: for example, instance A is processing image 3, while instance B is processing image 1 that was recognized as a car by instance A. When instance A terminates its image processing, it adds index -1 to the queue to signal to instance B that no more indexes will come. When instance B finishes, it prints to a file, in its associated EBS, the indexes of the images that have both cars and text, and also prints the actual text in each image next to its index.

# Approached Method

## Initial Setup
1) Login to the AWS Academy account with the credentials sent by your professor and go to your course. Access the vocareum learner lab in Modules > Launch AWS Learner Lab.
2) Start the AWS lab and console and copy the aws_access_key_id, aws_secret_access_key, aws_session_token in AWS Details and paste it into your credentials file in ~/.aws/credentials. The 'config' file has the region (eg. us-east-1 for N. Virginia) and output format (eg. json) stored. 
3) Along with that download the labsuser.pem file under SSH Key to authenticate later before you can access your EC2 instances through your terminal.
4) Once you start your lab, open your AWS Management console, search for EC2 under Services, and follow the steps below to create your two instances.

## Creating EC2 Instances (To work parallely)
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

## Setting up JAVA Applications and Maven Packages
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
12) REMEMBER TO CONFIGURE AWS CREDENTIALS IN YOUR LOCAL TERMINAL AS WELL BEFORE RUNNING, to use the services of AWS in your application, using the access_key, secret_access_key and session_token that you copy pasted onto your ~/.aws/credentials file.
13) If everything runs well, you're good to proceed to the next section where you'll upload these JAR files onto your EC2 instances. 

## SSH Access to EC2 Instances (using PuTTY on Windows OS)
1) Make sure that you have PuTTY installed, else you can visit https://www.putty.org/ to do so. We need PuTTY to use the 'pscp' command.
2) Upload the ObjectRecognition.jar onto your EC2_A instance and TextRecognition.jar onto your EC2_B instance.
3) To upload the JAR file onto your EC2 instance, run the following command by opening a terminal (cmd). 
![image](https://github.com/CK-ghub/AWS-Object-Text-Recognition-Pipeline/assets/69519007/fc6068a4-a121-4d13-9038-dcf48abf660e)<br>
This will upload and store the JAR file in the root directory of that EC2 instance. Do the same for the other application as well.
4) We will now SSH into the EC2 instances one by one in individual windows. To SSH into one instance, for example EC2_A, follow the steps below.
5) Open Putty and in the 'Session' catrgory, enter the public IP address of your EC2 instance in the field.
6) In the "Connection">"SSH">"Auth">"Credentials" browse and select the PPK Private key file (EC2_Key.ppk).
7) Click on Open to start the session.
8) The user will now be prompted to log in. The default username for Amazon Linux and Amazon AMI instances is "ec2-user".<br>
![image](https://github.com/CK-ghub/AWS-Object-Text-Recognition-Pipeline/assets/69519007/e1e69ea1-e3ea-4137-b889-a92e94566aa9)<br>
If you get this screen, you've successfully connected to your EC2 instance.
9) Repeat steps 5 to 8 for the other instance as well.
10) You'll now install necessary software, including Java, AWS CLI, and the AWS SDK for Java after connecting to EC2 instances via PuTTY on windows, using the following commands in order.<br>
    -> 'sudo yum update'<br>
    -> 'sudo yum install java-1.8.0-openjdk'<br>
    -> 'sudo yum install python-pip'<br>
    -> 'pip install awscli'<br>
    -> 'aws configure' to configure the AWS credentials and access the resources.<br>

## Running the Applications on Instances
1) Finally, once you have everything setup and installed on both instances, you can find your JAR file in your root directory of your instances.
2) Use 'java -jar yourjarfile.jar' to run the application.
3) Parallely run both the applications on two different windows.

## ObjectRecognition on Instance EC2_A
1) This application reads images from the S3 bucket that has already been created:- https://njit-cs-643.s3.us-east-1.amazonaws.com
2) The images that satisfy the object "Car" and with confidence > 90 are pushed to the SQS queue created in the application.
3) We can see that 6 images have satisfied the condition and been pushed to the queue. <br>
![image](https://github.com/CK-ghub/AWS-Object-Text-Recognition-Pipeline/assets/69519007/87214c94-1784-4c5a-b810-28bfb531c7f3)
<br>
4) The queue is also generated in AWS lab as shown below. (In SQS under Services)<br>

![image](https://github.com/CK-ghub/AWS-Object-Text-Recognition-Pipeline/assets/69519007/9ac75e78-8587-4fc2-916c-00ac2467fcf0)
 <br>
The messages can also be seen through polling. 6 images are pushed and another one is "-1" to show that the application has finished processing images. (for termination)<br>
![image](https://github.com/CK-ghub/AWS-Object-Text-Recognition-Pipeline/assets/69519007/068f7e31-294f-4c0d-85d3-2451372f2990)
<br>

## TextRecognition on Instance EC2_B
1) As the images are sent to the SQS queue from Instance EC2_A, the images are retrieved from the same SQS and processed for text.
2) The output of the texts in images are stored in "filename.txt" in the same directory, which can be read to see the final output.
   The processing and final outputs are shown below. <br>
   ![image](https://github.com/CK-ghub/AWS-Object-Text-Recognition-Pipeline/assets/69519007/babca7a3-d275-403e-b46d-6a7d8e4d2d31) <br>
   ![image](https://github.com/CK-ghub/AWS-Object-Text-Recognition-Pipeline/assets/69519007/81dc0d20-be3b-4682-803e-cd27ae7b11b5) <br>

## Conclusion
You've now learnt to: <br>(1) Create VMs (EC2 instances) in the cloud<br>(2) Use cloud storage (S3) in your applications<br>(3) Communicate between VMs using a queue service (SQS)<br>(4) Program distributed applications in Java on Linux VMs in the cloud.<br>(5) Use a machine learning service (AWS Rekognition) in the cloud.
