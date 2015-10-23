package org.shunya.punter.tasks;

import com.jcraft.jsch.*;
import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.PunterTask;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.logging.Level;

@PunterTask(author = "munishc", name = "SCPTask", description = "SCP File to remote machine.", documentation = "src/main/resources/docs/SCPTask.html")
public class SCPTask extends Tasks {
    @InputParam(required = true, description = "Hostname of Unix machine")
    private String hostname;
    @InputParam(required = true, description = "SSH port number, default 22")
    private int port;
    @InputParam(required = true, description = "Absolute path for private key location")
    private String privateKey;
    @InputParam(required = true)
    private String username;
    @InputParam(required = true)
    private String password;
    @InputParam(required = true, description = "Source File to be copied")
    private String sourceFile;
    @InputParam(required = true, description = "Target Dir/File name")
    private String targetFile;

    @Override
    public boolean run() {
        boolean status = false;
        try {
//		  LOGGER.get().log(Level.INFO, outName);
            JSch jsch = new JSch();

            //jsch.setKnownHosts("/home/foo/.ssh/known_hosts");
            if (privateKey != null && !privateKey.isEmpty())
                jsch.addIdentity(privateKey);

            if(port == 0)
                port = 22;

            Session session = jsch.getSession(username, hostname, port);

            if (password != null && !password.isEmpty())
                session.setPassword(password);

            // username and password will be given via UserInfo interface.
            UserInfo ui = new MyUserInfo();
            session.setUserInfo(ui);

            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
//	      session.connect(30000);   // making a connection with timeout.
            LOGGER.get().log(Level.INFO, "Connected to Shell.");

            String[] srcFiles = sourceFile.split("[\r\n|\n\r|\r|\n|;|,]");
            String[] tgtFiles = targetFile.split("[\r\n|\n\r|\r|\n|;|,]");

            for (int index = 0; index < srcFiles.length; index++) {
                String srcFile = srcFiles[index];
                String tgtFile = tgtFiles[index];
                // exec 'scp -t rfile' remotely
                String command = "scp -p -t " + tgtFile;
                Channel channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);

                // get I/O streams for remote scp
                OutputStream out = channel.getOutputStream();
                InputStream in = channel.getInputStream();

                channel.connect();

                if (checkAck(in) != 0) {
                    LOGGER.get().log(Level.SEVERE, "UnKnown Error transmitting the file.");
                    return false;
                }

                // send "C0644 filesize filename", where filename should not include '/'
                long filesize = (new File(srcFile)).length();
                command = "C0644 " + filesize + " ";
                if (srcFile.lastIndexOf('/') > 0) {
                    command += srcFile.substring(srcFile.lastIndexOf('/') + 1);
                } else {
                    command += srcFile;
                }
                command += "\n";
                out.write(command.getBytes());
                out.flush();
                if (checkAck(in) != 0) {
                    LOGGER.get().log(Level.SEVERE, "UnKnown Error transmitting the file.");
                    return false;
                }
                // send a content of lfile
                byte[] buf = new byte[0];
                int progress = 0;
                long transferred = 0;
                try (FileInputStream fis = new FileInputStream(srcFile)){
                    buf = new byte[1024];
                    while (true) {
                        int len = fis.read(buf, 0, buf.length);
                        if (len <= 0) break;
                        out.write(buf, 0, len);
                        out.flush();
                        transferred += len;
                        progress = (int) (transferred * 100 / filesize);
                        getTaskHistory().setProgress(progress);
                        getObserver().update(getTaskHistory());
                    }
                    // send '\0'
                    buf[0] = 0;
                    out.write(buf, 0, 1);
                    out.flush();
                    if (checkAck(in) != 0) {
                        LOGGER.get().log(Level.SEVERE, "UnKnown Error transmitting the file.");
                        return false;
                    }
                } catch (IOException ee) {
                    LOGGER.get().log(Level.SEVERE, "Error in SCP operation", ee);
                    ee.printStackTrace();
                } finally {
                    out.close();
                }
                channel.disconnect();
                LOGGER.get().log(Level.INFO, "File sent successfully : " + tgtFile);
            }

            session.disconnect();
            status = true;
        } catch (Exception e) {
            LOGGER.get().log(Level.SEVERE, "Error in SCP operation", e);
            e.printStackTrace();
        }
        return status;
    }

    static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }

    public static class MyUserInfo implements UserInfo, UIKeyboardInteractive {
        public String getPassword() {
            return passwd;
        }

        public boolean promptYesNo(String str) {
            return true;
        }

        String passwd;
        JTextField passwordField = (JTextField) new JPasswordField("Dare10dream$", 20);

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
            Object[] ob = {passwordField};
            passwd = passwordField.getText();
            return true;
        }

        public void showMessage(String message) {
        }

        final GridBagConstraints gbc =
                new GridBagConstraints(0, 0, 1, 1, 1, 1,
                        GridBagConstraints.NORTHWEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0);
        private Container panel;

        public String[] promptKeyboardInteractive(String destination,
                                                  String name,
                                                  String instruction,
                                                  String[] prompt,
                                                  boolean[] echo) {
            panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridx = 0;
            panel.add(new JLabel(instruction), gbc);
            gbc.gridy++;

            gbc.gridwidth = GridBagConstraints.RELATIVE;

            JTextField[] texts = new JTextField[prompt.length];
            for (int i = 0; i < prompt.length; i++) {
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridx = 0;
                gbc.weightx = 1;
                panel.add(new JLabel(prompt[i]), gbc);

                gbc.gridx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weighty = 1;
                if (echo[i]) {
                    texts[i] = new JTextField(20);
                } else {
                    texts[i] = new JPasswordField(20);
                }
                panel.add(texts[i], gbc);
                gbc.gridy++;
            }

            String[] response = new String[prompt.length];
            for (int i = 0; i < prompt.length; i++) {
                response[i] = texts[i].getText();
            }
            return response;
        }
    }
}