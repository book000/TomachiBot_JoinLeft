package com.jaoafa.TomachiBot_JoinLeft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import org.json.JSONObject;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.audio.AudioPlayer.Track;
import sx.blah.discord.util.audio.events.TrackFinishEvent;

public class BotReadyEvent {
	Map<Long, Queue<VCSpeakClass>> vcspeaks = new HashMap<>();
	//Queue<VCSpeakClass> queue = new ArrayDeque<>();

	@EventSubscriber
	public void onReadyEvent(ReadyEvent event) {
		System.out.println("Ready: " + event.getClient().getOurUser().getName());
	}

	@EventSubscriber
	public void onUserVoiceChannelJoinEvent(UserVoiceChannelJoinEvent event){
		if(event.getUser().getLongID() == event.getClient().getOurUser().getLongID()){
			return;
		}
		if(event.getVoiceChannel().getLongID() == event.getGuild().getAFKChannel().getLongID()){
			return;
		}

		System.out.println("VoiceJoin: " + event.getUser().getName() + " " + event.getVoiceChannel().getName());
		IVoiceChannel voice = event.getVoiceChannel();

		IGuild guild = event.getGuild();

		Queue<VCSpeakClass> queue;
		if(vcspeaks.containsKey(guild.getLongID())){
			queue = vcspeaks.get(guild.getLongID());
		}else{
			queue = new ArrayDeque<>();
		}

		queue.clear();

		VCSpeakClass clazz = new VCSpeakClass(
				voice,
				event.getUser().getName() + " joined!"
		);
		Track current = clazz.getAudioPlayer().getCurrentTrack();
		if(current == null){
			clazz.run();
		}else{
			queue.add(clazz);
		}

		vcspeaks.put(guild.getLongID(), queue);

		/* generalでのvc開始通知 */
		long channelID = getAlertChannel(guild.getStringID());
		if(channelID == -1){
			return;
		}
		if(event.getUser().isBot()){
			return;
		}

		List<IUser> noBots = new ArrayList<>();
		for(IUser user : voice.getConnectedUsers()){
			if(user.isBot()){
				continue;
			}
			noBots.add(user);
		}

		long lastvcid = getLastVCID(voice.getGuild().getStringID());
		long lastvcuserid = getLastVCUserID(voice.getGuild().getStringID());
		if(lastvcid != voice.getLongID() && lastvcuserid != event.getUser().getLongID()){
			System.out.println("VCAlert: lastvcid: " + lastvcid + " | lastvcuserid: " + lastvcuserid + " | " + noBots.size());
			if(noBots.size() == 1){
				IChannel general = event.getGuild().getChannelByID(channelID);
				general.sendMessage(":telephone_receiver:" + voice.getName() + "で" + event.getUser().getName() + "#" + event.getUser().getDiscriminator() + "が通話をはじめました。");

				setLastVCID(voice.getGuild().getStringID(), voice.getLongID());
				setLastVCUserID(voice.getGuild().getStringID(), event.getUser().getLongID());
			}
		}
	}

	@EventSubscriber
	public void onUserVoiceChannelLeaveEvent(UserVoiceChannelLeaveEvent event){
		if(event.getUser().getLongID() == event.getClient().getOurUser().getLongID()){
			return;
		}
		if(event.getVoiceChannel().getLongID() == event.getGuild().getAFKChannel().getLongID()){
			return;
		}
		System.out.println("VoiceJoin: " + event.getUser().getName() + " " + event.getVoiceChannel().getName());
		IVoiceChannel voice = event.getVoiceChannel();

		IGuild guild = event.getGuild();

		Queue<VCSpeakClass> queue;
		if(vcspeaks.containsKey(guild.getLongID())){
			queue = vcspeaks.get(guild.getLongID());
		}else{
			queue = new ArrayDeque<>();
		}

		queue.clear();

		VCSpeakClass clazz = new VCSpeakClass(
				voice,
				event.getUser().getName() + " leaved"
		);
		Track current = clazz.getAudioPlayer().getCurrentTrack();
		if(current == null){
			clazz.run();
		}else{
			queue.add(clazz);
		}

		vcspeaks.put(guild.getLongID(), queue);

		List<IUser> noBots = new ArrayList<>();
		for(IUser user : voice.getConnectedUsers()){
			if(user.isBot()){
				continue;
			}
			noBots.add(user);
		}
		System.out.println("VCLeft: " + noBots.size());
		if(noBots.size() == 0){ // 自分含め
			voice.leave();
		}
	}

	@EventSubscriber
	public void onUserVoiceChannelMoveEvent(UserVoiceChannelMoveEvent event){
		if(event.getUser().getLongID() == event.getClient().getOurUser().getLongID()){
			return;
		}
		System.out.println("VoiceMove: " + event.getUser().getName() + " " + event.getOldChannel().getName() + " -> " + event.getNewChannel().getName());
		IVoiceChannel voice = event.getVoiceChannel();

		IGuild guild = event.getGuild();

		Queue<VCSpeakClass> queue;
		if(vcspeaks.containsKey(guild.getLongID())){
			queue = vcspeaks.get(guild.getLongID());
		}else{
			queue = new ArrayDeque<>();
		}

		queue.clear();
		if(event.getGuild().getAFKChannel() != null){
			// afkあり
			if(event.getOldChannel().getLongID() != event.getGuild().getAFKChannel().getLongID()){
				VCSpeakClass clazz_leave = new VCSpeakClass(
						event.getOldChannel(),
						event.getUser().getName() + " moved to " + event.getNewChannel().getName()
				);
				Track current = clazz_leave.getAudioPlayer().getCurrentTrack();
				if(current == null){
					clazz_leave.run();
				}else{
					queue.add(clazz_leave);
				}
			}
		}else{
			// afkなし
			VCSpeakClass clazz_leave = new VCSpeakClass(
					event.getOldChannel(),
					event.getUser().getName() + " moved to " + event.getNewChannel().getName()
			);
			Track current = clazz_leave.getAudioPlayer().getCurrentTrack();
			if(current == null){
				clazz_leave.run();
			}else{
				queue.add(clazz_leave);
			}
		}

		vcspeaks.put(guild.getLongID(), queue);

		/* generalでのvc開始通知 */
		long channelID = getAlertChannel(guild.getStringID());
		if(channelID == -1){
			return;
		}
		if(event.getUser().isBot()){
			return;
		}

		List<IUser> noBots = new ArrayList<>();
		for(IUser user : voice.getConnectedUsers()){
			if(user.isBot()){
				continue;
			}
			noBots.add(user);
		}

		long lastvcid = getLastVCID(voice.getGuild().getStringID());
		long lastvcuserid = getLastVCUserID(voice.getGuild().getStringID());
		if(lastvcid != voice.getLongID() && lastvcuserid != event.getUser().getLongID()){
			System.out.println("VCAlert: lastvcid: " + lastvcid + " | lastvcuserid: " + lastvcuserid + " | " + noBots.size());
			if(noBots.size() == 1){
				IChannel general = event.getGuild().getChannelByID(channelID);
				general.sendMessage(":telephone_receiver:" + voice.getName() + "で" + event.getUser().getName() + "#" + event.getUser().getDiscriminator() + "が通話をはじめました。");

				setLastVCID(voice.getGuild().getStringID(), voice.getLongID());
				setLastVCUserID(voice.getGuild().getStringID(), event.getUser().getLongID());
			}
		}
	}

	@EventSubscriber
	public void onFinish(TrackFinishEvent event){
		IGuild guild = event.getPlayer().getGuild();

		Queue<VCSpeakClass> queue;
		if(vcspeaks.containsKey(guild.getLongID())){
			queue = vcspeaks.get(guild.getLongID());
		}else{
			queue = new ArrayDeque<>();
		}
		VCSpeakClass clazz = queue.poll();
        if (clazz == null){
        	// 次がない
        	/*
        	IVoiceChannel botVoiceChannel = event.getClient().getOurUser().getVoiceStateForGuild(event.getPlayer().getGuild()).getChannel();
    		if(botVoiceChannel == null){
    			return;
    		}
    		botVoiceChannel.leave();*/
        }else{
        	// 次がある
			clazz.run();
        }

		vcspeaks.put(guild.getLongID(), queue);
	}
	long getLastVCID(String guildID){
		File f = new File("vcdata.properties");
		Properties props;
		try{
			InputStream is = new FileInputStream(f);

			// プロパティファイルを読み込む
			props = new Properties();
			props.load(is);
		}catch(FileNotFoundException e){
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		if(!props.containsKey(guildID + "_vcid")){
			return -1;
		}

		String vcidstr = props.getProperty(guildID + "_vcid");

		try{
			return Long.parseLong(vcidstr);
		}catch(NumberFormatException e){
			return -1;
		}
	}
	void setLastVCID(String guildID, long vcid){
		File f = new File("vcdata.properties");
		Properties props;

		try {
			InputStream is = new FileInputStream(f);

			// プロパティファイルを読み込む
			props = new Properties();
			props.load(is);
		} catch(FileNotFoundException e){
			props = new Properties();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		props.setProperty(guildID + "_vcid", Long.toString(vcid));

		try {
			props.store(new FileOutputStream("vcdata.properties"), "Comments");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	long getLastVCUserID(String guildID){
		File f = new File("vcdata.properties");
		Properties props;
		try{
			InputStream is = new FileInputStream(f);

			// プロパティファイルを読み込む
			props = new Properties();
			props.load(is);
		}catch(FileNotFoundException e){
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		if(!props.containsKey(guildID + "_vcuserid")){
			return -1;
		}

		String vcuseridstr = props.getProperty(guildID + "_vcuserid");

		try{
			return Long.parseLong(vcuseridstr);
		}catch(NumberFormatException e){
			return -1;
		}
	}
	void setLastVCUserID(String guildID, long vcuserid){
		File f = new File("vcdata.properties");
		Properties props;

		try {
			InputStream is = new FileInputStream(f);

			// プロパティファイルを読み込む
			props = new Properties();
			props.load(is);
		} catch(FileNotFoundException e){
			props = new Properties();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		props.setProperty(guildID + "_vcuserid", Long.toString(vcuserid));

		try {
			props.store(new FileOutputStream("vcdata.properties"), "Comments");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	long getAlertChannel(String guildID){
		Path path = Paths.get("alertchannel.json");
		try {
		    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		    String data = String.join("\n", lines);
		    JSONObject obj = new JSONObject(data);
		    if(obj.has(guildID)){
		    	return obj.getLong(guildID);
		    }else{
		    	return -1;
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		    return -1;
		}
	}
}