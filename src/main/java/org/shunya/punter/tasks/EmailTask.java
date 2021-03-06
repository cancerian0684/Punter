package org.shunya.punter.tasks;

import org.asciidoctor.Asciidoctor;
import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.utils.DevEmailService;
import org.shunya.punter.utils.EmailService;
import org.shunya.punter.utils.StringUtils;

import java.io.File;
import java.util.Collections;

import static org.asciidoctor.Asciidoctor.Factory.create;

@PunterTask(author = "munishc", name = "EmailTask", description = "Email Task", documentation = "src/main/resources/docs/EmailTask.html")
public class EmailTask extends Tasks {
    @InputParam(required = true, description = "comma separated to addresses")
    private String toAddress;
    @InputParam(required = true, description = "comma separated to addresses")
    private String ccAddress;
    @InputParam(required = true, description = "Subject of Email")
    private String subject;
    @InputParam(required = true, description = "from Address")
    private String fromAddress;
    @InputParam(required = true, description = "html body")
    private String body;
    @InputParam(required = true, description = "Comma Separated File Names")
    private String attachments;
    @InputParam(required = false, description = "Username if Auth is required")
    private String username;
    @InputParam(required = false, description = "Password if Auth is required")
    private String password;
    @InputParam(required = false, description = "Input String for matching")
    private String inputString;
    @InputParam(required = false, description = "Line separated expected messages")
    private String expectedMessages;

    private final Asciidoctor asciidoctor = create();

    @Override
    public boolean run() {
        boolean status = false;
        try {
            String outName = "";
            if (inputString != null && expectedMessages != null && !expectedMessages.isEmpty()) {
                String[] messages = expectedMessages.split("\n");
                for (String message : messages) {
                    if (!inputString.contains(message)) {
                        outName += message + " not true," + System.getProperty("line.separator");
                    }
                }
                if (outName.length() >= 1) {
                    LOGGER.get().error(outName);
                } else {
                    LOGGER.get().info("No Email was sent since Condition did not meet!");
                    return true;
                }
            }
            attachments = attachments == null ? "" : attachments;
            String[] fileNames = attachments.split("[,;]");
            body = asciidoctor.convert(body, Collections.emptyMap());
            if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
                //Authentication Based Email
//				EmailServiceWithAuth.getInstance(username,password).sendEMail(subject, toAddress, body+outName, fileNames, fromAddress, ccAddress);
                getObserver().update(getTaskHistory());
                DevEmailService.getInstance().sendEmail(subject, toAddress, body, Collections.<File>emptyList());
            } else {
                //Non-Auth Based Email
                EmailService.getInstance().sendEMail(subject, toAddress, body + outName, fileNames, fromAddress, ccAddress);
            }
            status = true;
            LOGGER.get().info("Email sent successfully To Addresses: " + toAddress);
        } catch (Exception e) {
            status = false;
            LOGGER.get().error(StringUtils.getExceptionStackTrace(e));
        }
        return status;
    }

    public static void main(String[] args) {
        EmailTask sqt = new EmailTask();
        sqt.run();
    }
}