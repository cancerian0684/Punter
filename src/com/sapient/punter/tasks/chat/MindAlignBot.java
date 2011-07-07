package com.sapient.punter.tasks.chat;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.parlano.foundation.Destination;
import com.parlano.foundation.Group;
import com.parlano.foundation.GroupAttribute;
import com.parlano.foundation.Message;
import com.parlano.foundation.User;
import com.parlano.foundation.UserPresenceAttribute;
import com.parlano.mdk.FilePostMessage;
import com.parlano.mdk.MDK;
import com.parlano.mdk.MDKConfig;
import com.parlano.mdk.MDKFactory;
import com.parlano.mdk.exception.AuthenticationException;
import com.parlano.mdk.exception.MDKCompatibilityException;
import com.parlano.mdk.exception.MDKConfigException;
import com.parlano.mdk.exception.MDKConnectionException;
import com.parlano.mdk.exception.MDKException;
import com.parlano.mdk.exception.MessagingServerAccessException;
import com.parlano.mdk.exception.OperationNotPermittedException;
import com.parlano.mdk.exception.RemoteServerAccessException;

public class MindAlignBot implements MDK.MDKListener{
	private static MindAlignBot instance;
    private static final Log log = LogFactory.getLog(MindAlignBot.class);
    private static final int MAX_LENGTH = 1024;
    private final Set<String> joinedChannels = new HashSet<String>();
    private final MDK mdk;
    private final String botName;
    private final String botPassword;
    private final String botChannel;
    private final Properties mindAlignConfig;
    private static boolean reconnect;

    public static MindAlignBot getInstance(String botChannel,String botName,String botPassword) throws Exception{
    	if(instance==null||reconnect){
    		instance=new MindAlignBot(botChannel,botName,botPassword);
    		reconnect=false;
    		/*Runtime.getRuntime().addShutdownHook(new Thread(){
    			@Override
    			public void run() {
    				super.run();
    				try{
    				instance.disconnect();
    				}catch (Exception e) {
    					e.printStackTrace();
					}
    			}
    		});*/
    	}
		return instance;
	}
    
    private MindAlignBot(String botChannel,String botName,String botPassword) throws MDKConfigException, IOException, AuthenticationException, MessagingServerAccessException, RemoteServerAccessException {
        this.botChannel = botChannel;
        this.botName = botName;
        this.botPassword= botPassword;
        this.mindAlignConfig= new Properties();
        mindAlignConfig.load(MindAlignBot.class.getResourceAsStream("/resources/chat-adaptor.properties"));
        this.mdk = createConfiguredMdk();    
        mdk.addMDKListener(this);
        mdk.connect();
        this.joinDefaultChannel();
    }
    private MDK createConfiguredMdk() throws MDKConfigException {
        MDKConfig mdkConfig = new MDKConfig();
        mdkConfig.includeProperties(mindAlignConfig);

        //set username and password from environment properties
        mdkConfig.setConfigProperty(MDKConfig.USERNAME_KEY, botName);
        mdkConfig.setConfigProperty(MDKConfig.USERPASSWORD_KEY, botPassword);
        mdkConfig.validateConfig();

        MDK mdk = MDKFactory.createMDK();
        mdk.configure(mdkConfig);
        return mdk;
    }
    public void sendMessage(String message,boolean urgent) {
        sendMessageToChannel(message, botChannel,urgent);
    }

    public void connect() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support connect()");
    }

    public void disconnect() {
        joinedChannels.clear();
        try {
            mdk.disconnect();
        } catch (MDKException e) {
            throw new RuntimeException("Error while attempting disconnect", e);
        }finally{
        	instance=null;
        }
    }

    public void onReconnect() {
        try {
            joinedChannels.clear();
            joinChannel(botChannel);
        } catch (OperationNotPermittedException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (MDKConnectionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void joinDefaultChannel() {
        try {
            joinChannel(botChannel);
        } catch (OperationNotPermittedException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (MDKConnectionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void onGroupMessage(User sender, Group group, Message message) {
        String msgContent = message.getContent();
        if (msgContent == null || !isAddressedToThisBot(msgContent)) {
            return;
        }

        try {
            String response = generateResponse(sender, msgContent);
            sendMessageToChannel(response, group.getName(),false);
        }
        catch (RuntimeException e) {
            // Do nothing
            log.error("Could not send reply message to group", e);
        }
    }

    public void onDisconnect() {
        log.warn("onDisconnect event received. what to do? what to do?");
    }

    public void onPrivateMessage(User sender, Message message) {
        if (isBot(sender)) {
            return;
        }

        String response = generateResponse(sender, message.getContent());
        response = sanitise(response);

        try {
            mdk.sendMessage(sender, response, false);
        } catch (OperationNotPermittedException e) {
            log.error("Unable to reply to message", e);
        } catch (MDKConnectionException e) {
            log.error("Unable to reply to message", e);
        }
    }

    private void sendMessageToChannel(final String originalMessage, final String channelName,final boolean urgent) {
        String message = sanitise(originalMessage);
        try {
            joinChannel(channelName);
            Destination destination = mdk.getGroup(channelName);
            mdk.sendMessage(destination, message, urgent);
        } catch (OperationNotPermittedException e) {
        	reconnect=true;
            throw new RuntimeException(e.getMessage(), e);

        } catch (MDKConnectionException e) {
        	 reconnect=true;
             throw new RuntimeException(e.getMessage(), e);

        } catch (MDKCompatibilityException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void joinChannel(String channel) throws OperationNotPermittedException, MDKConnectionException {
        if (channel.startsWith("#")) {
            channel = channel.substring(1);
        }
        if (joinedChannels.contains(channel)) {
            return;
        }
        log.debug("joinChannel: #'" + channel + "'");
        mdk.joinGroup(channel);
        joinedChannels.add(channel);
    }

    private boolean isAddressedToThisBot(String request) {
        String botFriendlyName = botName.replace("_", "");
        return isAddressedToBot(request, botName) || isAddressedToBot(request, botFriendlyName);

    }

    private boolean isAddressedToBot(String request, String botName) {
        if (request == null) {
            return false;
        }
        botName = botName.toLowerCase();
        request = request.toLowerCase().trim();
        String[] words = request.split(" ");
        for (String word : words) {
            if (word.equals(botName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBot(User sender) {
        return sender.getUserName().startsWith("_");
    }

    private String generateResponse(User sender, String msgContent) {
        return getResponse(sender.getFirstName() + " " + sender.getLastName(), msgContent);
    }

    private String getResponse(String requesterFullName, String request) {
        if (request == null) {
            return "";
        }
        request = request.toLowerCase().trim();
        List<String> words = asList(request.split(" "));
        if (words.contains("about")) {
            return getAboutResponse();
        }

        return "Sorry " + requesterFullName + ". I'm not allowed to talk to strangers.";
    }

    private String getAboutResponse() {

        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            hostName = "??????";
        }

        String systemUserName = System.getProperty("user.name");

        StringBuilder builder = new StringBuilder();
        builder.append("I'm running as ");
        builder.append(systemUserName);
        builder.append(" on host ");
        builder.append(hostName);
        builder.append(".\nI'm responsible for notifications from Wheelbarrow.");

        return builder.toString();

    }

    private String sanitise(String message) {
        if (message == null || (message = message.trim()).length() == 0) {
            return message;
        }

        return Sanitizer.validChars(Sanitizer.maxLength(message, MAX_LENGTH));
    }

    public void onFilePostMessage(FilePostMessage filePostMessage) {
    }

    public void onPresence(User user, UserPresenceAttribute userPresenceAttribute, String s) {
    }

    public void onUserJoin(User user, Group group) {
    }

    public void onUserKicked(User user, Group group) {
    }

    public void onUserLeft(User user, Group group) {
    }

    public void onError(int i, String s) {
    }

    public void onGroupChanged(Group group, GroupAttribute groupAttribute, boolean b) {
    }

    public void onTopicChanged(Group group, String s) {
    }

    public void onInvite(Group group) {
    }

    public void onUserNameChanged(User user, String s, String s1) {
    }

    public void onGroupAdminAdded(Group group, User user) {
    }

    public void onGroupAdminRemoved(Group group, User user) {
    }

    public void onUserCreated(User user) {
    }

    public void onUserDeleted(User user) {
    }
}