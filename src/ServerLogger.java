import jsward.platformracer.common.util.ILogger;

public class ServerLogger implements ILogger {
    @Override
    public void logMessage(String tag, String msg) {
        System.out.println(tag + ": " + msg);
    }

    @Override
    public void logException(String tag, Exception e) {
        System.out.println(tag + ": ");
        e.printStackTrace();
    }
}
