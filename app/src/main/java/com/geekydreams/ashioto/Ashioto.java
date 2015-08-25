package com.geekydreams.ashioto;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
ble(tableName = "ashioto1")
public class Ashioto{
    private String timestamp;
    private int gateID;
    private int inCount;
    private int outCount;
    private String lattitude;
    private String longitude;

    //Initialization Attributes
    //Timestamp
    @DynamoDBHashKey(attributeName = "timestamp")
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    @DynamoDBIndexHashKey(attributeName = "gateID", globalSecondaryIndexName = "gateID-index")
    public int getGateID(){
        return gateID;
    }
    public void setGateID(int gateID){
        this.gateID = gateID;
    }
    //End of initialization values
    //Resource Attributes
    @DynamoDBAttribute(attributeName = "incount")
    public int getInCount(){
        return inCount;
    }
    public void setInCount(int inCount){
        this.inCount = inCount;
    }
    @DynamoDBAttribute(attributeName = "outcount")
    public int getOutCount(){
        return outCount;
    }
    public void setOutCount(int outCount){
        this.outCount = outCount;
    }
    @DynamoDBAttribute(attributeName = "lattitude")
    public String getLattitude() {
        return lattitude;
    }
    public void setLattitude(String lattitude) {
        this.lattitude = lattitude;
    }
    @DynamoDBAttribute(attributeName = "long")
    public String getLongitude() {
        return longitude;
    }
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    //End of Resource Attributes
}