package com.aws.ec2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.plaf.synth.Region;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectTextRequest;
import software.amazon.awssdk.services.rekognition.model.DetectTextResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import software.amazon.awssdk.services.rekognition.model.TextDetection;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;
 

public class TextRecognition 
{
    public static void main( String[] args )
    {
    	File myObj = new File("filename.txt");
    	
    	String bucketName = "njit-cs-643";
        Region region = Region.US_EAST_1;
        try {
        myObj.createNewFile();
        FileWriter myWriter = new FileWriter("filename.txt");
        while(true) {
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();
        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_EAST_1)
                .build();
        RekognitionClient rekClient = RekognitionClient.builder()
                .region(region)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
        
        	CreateQueueRequest request = CreateQueueRequest.builder()
                    .queueName("sqs1")
                    .build();
            CreateQueueResponse createResult = sqsClient.createQueue(request);

            GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                .queueName("sqs1")
                .build();

            String queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();
            
        	ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .waitTimeSeconds(0)
                    .build();
                List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();
 
                DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                         .queueUrl(queueUrl);
                         
            	for (Message message: messages ) {
            
            	String myValue = message.body();
            	    deleteMessageRequest.receiptHandle(message.receiptHandle()).build();
                     sqsClient.deleteMessage(deleteMessageRequest);
                     
                
                
                if(myValue.equalsIgnoreCase("-1")) {
                	System.out.println("Terminate");
                	myWriter.close();
                	return;
                }
                    software.amazon.awssdk.services.rekognition.model.S3Object s3Object = software.amazon.awssdk.services.rekognition.model.S3Object.builder()
                            .bucket(bucketName)
                            .name(myValue)
                            .build() ;
 
                    Image myImage = Image.builder()
                            .s3Object(s3Object)
                            .build();
 
                    DetectTextRequest textRequest = DetectTextRequest.builder()
                            .image(myImage)
                            .build();
 
                    DetectTextResponse textResponse = rekClient.detectText(textRequest);
                    List<TextDetection> textCollection = textResponse.textDetections();
 
                    //System.out.println("Detected lines and words");
                    
                    if(textCollection.size()>0)
                    	myWriter.write(myValue+ " : ");
                    	textCollection.forEach(System.out::println);
                    for (TextDetection text: textCollection) {
                        //System.out.println(text.detectedText());
                    	myWriter.write(text.detectedText() + " ");
                    }
                    myWriter.write("\n");
 
             }
            //sqsClient.deleteQueue(deleteQueueRequest);
        }}
        catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
        
}}

