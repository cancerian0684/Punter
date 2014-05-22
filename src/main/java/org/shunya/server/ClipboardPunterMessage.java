package org.shunya.server;

public class ClipboardPunterMessage extends PunterMessage {
    private String contents;
    private String type;

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
