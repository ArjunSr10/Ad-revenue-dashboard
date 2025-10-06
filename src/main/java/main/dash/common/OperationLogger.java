package main.dash.common;

import main.dash.enums.OperationEnum;

import java.util.Date;

public abstract class OperationLogger {
    /**
     * record an operation into Context
     * @param operationEnum the type of operation
     * @param content the content of operation
     */
    private static void record(OperationEnum operationEnum, String content) {
        String name = Context.getCurrentUser();
        Operation operation = new Operation(name, new Date(), operationEnum, content);
        Context.addOperation(operation);
    }

    /**
     * record a type of operation is create user
     * @param content the content of operation
     */
    public static void recordCreateUser(String content) {
        record(OperationEnum.CREATE_USER, content);
    }

    /**
     * record a type of operation is login
     * @param content the content of operation
     */
    public static void recordLogin(String content) {
        record(OperationEnum.LOGIN, content);
    }

    /**
     * record a type of operation is action
     * @param content the content of operation
     */
    public static void recordAction(String content) {
        record(OperationEnum.ACTION, content);
    }

    /**
     * record a type of operation is select
     * @param content the content of operation
     */
    public static void recordSelect(String content) {
        record(OperationEnum.SELECT, content);
    }
}
