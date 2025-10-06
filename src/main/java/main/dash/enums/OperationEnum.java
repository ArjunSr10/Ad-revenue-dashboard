package main.dash.enums;

public enum OperationEnum {
    CREATE_USER("create user"),

    LOGIN("login"),

    SELECT("select item"),

    ACTION("action");
    private String type;

    OperationEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
