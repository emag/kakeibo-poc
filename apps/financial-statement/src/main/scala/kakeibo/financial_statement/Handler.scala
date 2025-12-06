package kakeibo.financial_statement

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import scala.jdk.CollectionConverters._

class Handler extends RequestHandler[DynamodbEvent, String] {
  
  override def handleRequest(event: DynamodbEvent, context: Context): String = {
    println(s"Received DynamoDB Stream event with ${event.getRecords.size()} records")
    
    event.getRecords.asScala.zipWithIndex.foreach { case (record, index) =>
      println(s"Record ${index + 1}:")
      println(s"  Event Name: ${record.getEventName}")
      println(s"  Event Source: ${record.getEventSource}")
      println(s"  Event Version: ${record.getEventVersion}")
      println(s"  AWS Region: ${record.getAwsRegion}")
      
      val dynamodb = record.getDynamodb
      if (dynamodb != null) {
        println(s"  Approximate Creation DateTime: ${dynamodb.getApproximateCreationDateTime}")
        println(s"  Stream View Type: ${dynamodb.getStreamViewType}")
        println(s"  Sequence Number: ${dynamodb.getSequenceNumber}")
        println(s"  Size Bytes: ${dynamodb.getSizeBytes}")
        
        if (dynamodb.getKeys != null) {
          println(s"  Keys: ${dynamodb.getKeys}")
        }
        
        if (dynamodb.getNewImage != null) {
          println(s"  New Image: ${dynamodb.getNewImage}")
        }
        
        if (dynamodb.getOldImage != null) {
          println(s"  Old Image: ${dynamodb.getOldImage}")
        }
      }
      
      println("---")
    }
    
    s"Processed ${event.getRecords.size()} records successfully"
  }
}