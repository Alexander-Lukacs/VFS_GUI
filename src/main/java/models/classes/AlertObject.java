package models.classes;

/**
 * Created by Mesut on 08.02.2018.
 */
public class AlertObject {
    private String title;
    private String header;
    private String content;
    private Exception exception;

    public AlertObject(){}

    public AlertObject(String title, String header, String content, Exception exception) {
        this.title = title;
        this.header = header;
        this.content = content;
        this.exception = exception;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
