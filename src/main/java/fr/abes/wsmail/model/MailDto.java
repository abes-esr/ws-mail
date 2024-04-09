package fr.abes.wsmail.model;

public class MailDto {
    private String app;
    private String[] to;
    private String[] cc;
    private String[] cci;
    private String subject;
    private String text;

    public String getApp() {
        return app;
    }

    public String[] getTo() {
        return to;
    }

    public String[] getCc() {
        return cc;
    }

    public String[] getCci() {
        return cci;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setTo(String[] to) {
        this.to = to;
    }

    public void setCc(String[] cc) {
        this.cc = cc;
    }

    public void setCci(String[] cci) {
        this.cci = cci;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setText(String text) {
        this.text = text;
    }
}
