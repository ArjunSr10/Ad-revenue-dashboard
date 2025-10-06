package main.dash.common;

import main.dash.enums.OperationEnum;

import java.util.Date;

/**
 * Operation log structure
 */
public class Operation {
    /**
     * operation user name
     */
    private String name;

    /**
     * operation time
     */
    private Date operatingTime;

    /**
     * operation type,like  CREATE_USER,LOGIN,SELECT,ACTION
     */
    private OperationEnum operationEnum;

    /**
     * the operation content
     */
    private String content;

    /**
     * the remark
     */
    private String remark;

    public Operation(String name, Date operatingTime, OperationEnum operationEnum, String content) {
        this.name = name;
        this.operatingTime = operatingTime;
        this.operationEnum = operationEnum;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public Date getOperatingTime() {
        return operatingTime;
    }

    public OperationEnum getOperationEnum() {
        return operationEnum;
    }

    public String getContent() {
        return content;
    }


    public String getRemark() {
        return remark;
    }
}
