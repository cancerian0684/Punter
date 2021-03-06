package org.shunya.punter.tasks;

import com.jcraft.jsch.*;
import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Scanner;

@PunterTask(author = "munishc", name = "SCPFromTask", description = "SCP remote file to Local machine.", documentation = "src/main/resources/docs/SCPFromTask.html")
public class SCPFromTask extends Tasks {
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
    private String remoteFile;
    @InputParam(required = true, description = "Target Dir/File name")
    private String localFile;
    @InputParam(required = true, description = "Overwrite Local File .. defaults true")
    private boolean overwrite = true;

    @Override
    public boolean run() {
        boolean status = false;
        try {
//		  LOGGER.get().log(Level.INFO, outName);
            String prefix = null;
            if (new File(localFile).isDirectory()) {
                prefix = localFile + File.separator;
            }
            getTaskHistory().setActivity("Connecting to Shell");
            getObserver().update(getTaskHistory());

            JSch jsch = new JSch();
//	      jsch.addIdentity(System.getProperty("user.home") + "/.ssh/id_rsa");
            //jsch.setKnownHosts("/home/foo/.ssh/known_hosts");
            if (privateKey != null && !privateKey.isEmpty())
                jsch.addIdentity(privateKey);

            if (port == 0)
                port = 22;

            Session session = jsch.getSession(username, hostname, port);

            if (password != null && !password.isEmpty())
                session.setPassword(password);
            // username and password will be given via UserInfo interface.
            UserInfo ui = new MyUserInfo();
            session.setUserInfo(ui);
            // session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
//	      session.connect(30000);   // making a connection with timeout.
            LOGGER.get().info("Connected to Shell.");
            int fileCounter = 0;
            String[] localFiles = localFile.split("[\r\n|\n\r|\r|\n|;|,]");
            int totalFiles = localFiles.length;
            Scanner stk = new Scanner(remoteFile).useDelimiter("\r\n|\n\r|\r|\n|;|,");
            while (stk.hasNext()) {
                String currfile = stk.next().trim();

                if (currfile.startsWith("#") || currfile.isEmpty())
                    continue;
                fileCounter++;

                getTaskHistory().setProgress(100 * fileCounter / totalFiles);
                getTaskHistory().setActivity("Downloading .. " + currfile);
                getObserver().update(getTaskHistory());

                // exec 'scp -f rfile' remotely
                String command = "scp -f " + currfile;
                Channel channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);

                // get I/O streams for remote scp
                OutputStream out = channel.getOutputStream();
                InputStream in = channel.getInputStream();

                channel.connect();

                byte[] buf = new byte[1024];

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                while (true) {
                    int c = checkAck(in);
                    if (c != 'C') {
                        break;
                    }
                    // read '0644 '
                    in.read(buf, 0, 5);

                    long filesize = 0L;
                    while (true) {
                        if (in.read(buf, 0, 1) < 0) {
                            // error
                            break;
                        }
                        if (buf[0] == ' ') break;
                        filesize = filesize * 10L + (long) (buf[0] - '0');
                    }

                    String file = null;
                    for (int i = 0; ; i++) {
                        in.read(buf, i, 1);
                        if (buf[i] == (byte) 0x0a) {
                            file = new String(buf, 0, i);
                            break;
                        }
                    }
                    LOGGER.get().info(fileCounter + ".) filesize=" + filesize + ", file=" + file + " [" + currfile + "]");
                    // send '\0'
                    buf[0] = 0;
                    out.write(buf, 0, 1);
                    out.flush();

                    // read a content of lfile
                    File fout = new File(prefix == null ? localFiles[fileCounter-1] : prefix + file);
//		        if(!fout.exists()||(fout.exists()&&overwrite))
//		        {
                    try (FileOutputStream fos = new FileOutputStream(prefix == null ? localFiles[fileCounter - 1] : prefix + file)){
                        int foo;
                        while (true) {
                            if (buf.length < filesize) foo = buf.length;
                            else foo = (int) filesize;
                            foo = in.read(buf, 0, foo);
                            if (foo < 0) {
                                // error
                                break;
                            }
                            fos.write(buf, 0, foo);
                            filesize -= foo;
                            if (filesize == 0L) break;
                        }
                        if (checkAck(in) != 0) {
                            LOGGER.get().error("Unknown Technical Failure while retrieving the file.");
                            throw new Exception("Unknown Technical Failure while retrieving the file.");
                        }
                        // send '\0'
                        buf[0] = 0;
                        out.write(buf, 0, 1);
                        out.flush();
                    } finally {
                        out.close();
                    }
//		        }else{
//		        	break;
//		        }
                }
            }
            LOGGER.get().info("Disconnecting from the session.");
            session.disconnect();
            status = true;
        } catch (Exception e) {
            LOGGER.get().error("Exception occurred. \n" + StringUtils.getExceptionStackTrace(e));
            status = false;
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
                LOGGER.get().error("Error occurred." + sb.toString());
            }
            if (b == 2) { // fatal error
                LOGGER.get().error("Fatal Error occurred." + sb.toString());
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