package com.geekydreams.ashioto;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by geek on 12/8/15.
 */
@DynamoDBTable(tableName = "test_gate1")
public class Ashioto{
    private String year;
    private String month;
    private String date;
    private String hour;
    private String minute;
    private String second;
    private int uuid;
    private int n;
    private int gateID;
    private int inCount;
    private int outCount;
    private int vlotted;
    private float app;
    private Boolean synced;
    private Boolean plotted;

    //Initialization Attributes
    @DynamoDBHashKey(attributeName = "uuid")
    public int getUuid(){
        return uuid;
    }
    public void setUuid(int uuid){
        this.uuid = uuid;
    }
    @DynamoDBIndexHashKey(attributeName = "Plotted", globalSecondaryIndexName = "Plotted-n-index")
    public int getVlotted(){
        return vlotted;
    }
    public void setVlotted(int vlotted){
        this.vlotted = vlotted;
    }
    @DynamoDBIndexRangeKey(attributeName = "n", globalSecondaryIndexName = "Plotted-n-index")
    public int getN(){
        return n;
    }
    public void setN(int n){
        this.n = n;
    }
    //End of initialization values
    //Resource Attributes
    @DynamoDBAttribute(attributeName = "GateID")
    public int getGateID(){
        return gateID;
    }
    public void setGateID(int gateID){
        this.gateID = gateID;
    }
    @DynamoDBAttribute(attributeName = "Plotted")
    public Boolean getPlotted(){
        return plotted;
    }
    public void setPlotted(Boolean plotted){
        this.plotted = plotted;
    }
    @DynamoDBAttribute(attributeName = "Synced")
    public Boolean getSynced(){
        return synced;
    }
    public void setSynced(Boolean synced){
        this.synced = synced;
    }
    @DynamoDBAttribute(attributeName = "In")
    public int getInCount(){
        return inCount;
    }
    public void setInCount(int inCount){
        this.inCount = inCount;
    }
    @DynamoDBAttribute(attributeName = "Out")
    public int getOutCount(){
        return outCount;
    }
    public void setOutCount(int outCount){
        this.outCount = outCount;
    }
    @DynamoDBAttribute(attributeName = "APP")
    public float getApp(){
        return app;
    }
    public void setApp(float app){
        this.app = app;
    }
    //End of Resource Attributes
    //Timestamp Attributes
    @DynamoDBAttribute(attributeName = "Year")
    public String getYear(){
        return year;
    }
    public void setYear(String year){
        this.year = year;
    }
    @DynamoDBAttribute(attributeName = "Month")
    public String getMonth(){
        return month;
    }
    public void setMonth(String month){
        this.month = month;
    }
    @DynamoDBAttribute(attributeName = "Date")
    public String getDate(){
        return date;
    }
    public void setDate(String date){
        this.date = date;
    }
    @DynamoDBAttribute(attributeName = "Hour")
    public String getHour(){
        return hour;
    }
    public void setHour(String hour){
        this.hour = hour;
    }
    @DynamoDBAttribute(attributeName = "Minute")
    public String getMinute(){
        return minute;
    }
    public void setMinute(String minute){
        this.minute = minute;
    }
    @DynamoDBAttribute(attributeName = "Second")
    public String getSecond(){
        return second;
    }
    public void setSecond(String second){
        this.second = second;
    }
    //End of Timestamp Attributes
}