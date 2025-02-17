package classes.mvc;

public class ErrorCode {
    public static final String DUPLICATE_MAPPING = "E001";
    public static final String METHOD_NOT_ALLOWED = "E002";
    public static final String URL_NOT_FOUND = "E003";
    public static final String MISSING_PARAMETER = "E004";
    public static final String UNSUPPORTED_TYPE = "E005";
    public static final String ERROR_CREATING_PARAMETER_OBJECT = "E006";
    public static final String FILE_UPLOAD_ERROR = "E007";
    public static final String UNKNOWN_ERROR = "E999";

    public static String getErrorMessage(String code) {
        switch (code) {
            case DUPLICATE_MAPPING:
                return "Duplicate mapping for URL.";
            case METHOD_NOT_ALLOWED:
                return "Method not allowed.";
            case URL_NOT_FOUND:
                return "URL not found.";
            case MISSING_PARAMETER:
                return "Missing parameter.";
            case UNSUPPORTED_TYPE:
                return "Unsupported type.";
            case ERROR_CREATING_PARAMETER_OBJECT:
                return "Error creating parameter object.";
            case FILE_UPLOAD_ERROR:
                return "There was an error uploading the file.";
            case UNKNOWN_ERROR:
            default:
                return "An unknown error occurred.";
        }
    }
}
