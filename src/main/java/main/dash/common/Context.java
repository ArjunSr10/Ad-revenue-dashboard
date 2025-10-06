package main.dash.common;

import java.util.ArrayList;
import java.util.List;

/**
 * the application context
 */
public abstract class Context {
    /**
     * the current login user name
     */
    private static String CURRENT_USER;

    /**
     * All operations are logged
     */
    private static List<Operation> OPERATION_LOGS = new ArrayList<>();


    /**
     * get current user
     * @return CURRENT_USER
     */
    public static String getCurrentUser(){
        return CURRENT_USER;
    }

    /**
     * set current user
     * @param currentUser
     */
    public static void setCurrentUser(String currentUser){
        CURRENT_USER=currentUser;
    }

    /**
     * add an operation into OPERATION_LOGS
     * @param operation the operation log
     */
    public static void addOperation(Operation operation){
        OPERATION_LOGS.add(operation);
    }

    /**
     * Get all operation logs
     * @return
     */
    public static List<Operation> getOperationLogs() {
        return OPERATION_LOGS;
    }
}
