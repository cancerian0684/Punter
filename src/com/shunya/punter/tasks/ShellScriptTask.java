package com.shunya.punter.tasks;

import com.jcraft.jsch.*;
import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.OutputParam;
import com.shunya.punter.annotations.PunterTask;
import com.shunya.punter.utils.ProxyOutputStream;
import com.shunya.punter.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@PunterTask(author = "munishc", name = "ShellScriptTask", description = "Runs Script in Bash Shell", documentation = "com/shunya/punter/tasks/docs/ShellScriptTask.html")
public class ShellScriptTask extends Tasks {
    @InputParam(required = true, description = "Hostname of Unix machine")
    private String hostname;
    @InputParam(required = true)
    private String username;
    @InputParam(required = true)
    private String password;
    @InputParam(required = true, description = "Provide Punter syntax script")
    private String script;
    @InputParam(required = false)
    private long timeout;
    @InputParam(required = false)
    private String expectedMessage;
    @OutputParam
    private String outName;

    @Override
    public boolean run() {
        boolean status = false;
        try {
//		LOGGER.get().log(Level.INFO, outName);
            if (timeout < 1000)
                timeout = 600000; //10 minutes
            JSch jsch = new JSch();

            //jsch.setKnownHosts("/home/foo/.ssh/known_hosts");

            Session session = jsch.getSession(username, hostname, 22);

            session.setPassword(password);

            // username and password will be given via UserInfo interface.
            UserInfo ui = new MyUserInfo();
            session.setUserInfo(ui);

            // session.setConfig("StrictHostKeyChecking", "no");

            //session.connect();
            session.connect(30000);   // making a connection with timeout.

            Channel channel = session.openChannel("shell");

            LOGGER.get().log(Level.INFO, "Connected to Shell.");
            // Enable agent-forwarding.
            //((ChannelShell)channel).setAgentForwarding(true);

            PipedOutputStream pipeOut = new PipedOutputStream();
            PrintStream ps = new PrintStream(pipeOut);

            PipedInputStream pipeIn = new PipedInputStream(pipeOut);

//	      channel.setOutputStream(pipeOut);
//	      final PipedInputStream in = new PipedInputStream();
//	      PipedOutputStream out = new PipedOutputStream(in);
            ProxyOutputStream proxyOutputStream = new ProxyOutputStream(LOGGER.get(), "\nmunish1234");
            channel.setOutputStream(proxyOutputStream);
//	      channel.setOutputStream(System.out);
//	      channel.setInputStream(pipeIn);
            channel.setInputStream(pipeIn);
            ((ChannelShell) channel).setPtyType("vt102");
            channel.connect(5 * 1000);

//          MyThread t = new MyThread(in, LOGGER.get());
//	      t.setDaemon(true);
//	      t.start();

//	      ps.print("bash\r");
//	      ps.flush();
            TimeUnit.SECONDS.sleep(2);
            ps.print("echo munish1234\r");
            ps.flush();
            proxyOutputStream.waitForToken();
            Scanner stk = new Scanner(script).useDelimiter("\r\n|\n\r|\r|\n");
            while (stk.hasNext()) {
                String token = stk.next().trim();
                if (!token.isEmpty()) {
                    if (token.equalsIgnoreCase("f")) {
                        ps.flush();
                    } else if (token.equalsIgnoreCase("e")) {
                        ps.print("echo munish1234\r");
                        ps.flush();
                        proxyOutputStream.waitForToken();
                    } else if (token.startsWith("e ")) {
                        String[] tmp = token.split(" ");
                        if (tmp.length > 1) {
                            proxyOutputStream.setToken(tmp[1]);
                        }
                        proxyOutputStream.waitForToken();
                    } else if (token.toLowerCase().startsWith("e-")) {
                        ps.print("echo munish1234\r");
                        ps.flush();
                        proxyOutputStream.waitForToken();
                    } else {
                        try {
                            int sleep = Integer.parseInt(token);
                            TimeUnit.SECONDS.sleep(sleep);
                        } catch (Exception e) {
                            System.err.println("P: " + token + "\r");
                            ps.print(token + "\r");
                        }
                    }
                }
            }
            stk.close();
//            ps.print("exit\r");
//            ps.flush();
            TimeUnit.SECONDS.sleep(3);
            session.disconnect();
            channel.disconnect();
            LOGGER.get().log(Level.INFO, "Disconnected from Channel.");
            strLogger.delete(0, strLogger.length());
            strLogger.setLength(0);
            String proxyOutputStreamLogs = proxyOutputStream.getLogs();
            strLogger.append(proxyOutputStreamLogs);
            outName = proxyOutputStreamLogs;
            if (expectedMessage != null && !expectedMessage.isEmpty()) {
                if (!outName.contains(expectedMessage)) {
                    status = false;
                } else {
                    status = true;
                }
            } else {
                status = true;
            }
        } catch (Exception e) {
            LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
        }
        return status;
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