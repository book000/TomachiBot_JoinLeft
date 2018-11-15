package com.jaoafa.TomachiBot_JoinLeft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.sound.sampled.AudioInputStream;

import am.ik.voicetext4j.EmotionalSpeaker;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.audio.AudioPlayer;

public class BotReadyEvent {
	@EventSubscriber
	public void onReadyEvent(ReadyEvent event) {
		System.out.println("Ready: " + event.getClient().getOurUser().getName());


	}
	/*
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		System.out.println("Msg: " + event.getAuthor().getName() + " " + event.getMessage().getContent());
		IVoiceChannel voice = event.getGuild().getVoiceChannelByID(189377933356302336L);
		if(voice == null) return;
		voice.join();

		AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(event.getGuild());


		speakinVC(audioP, event.getChannel(), event.getMessage().getContent());
	}*/

	@EventSubscriber
	public void onUserVoiceChannelJoinEvent(UserVoiceChannelJoinEvent event){
		if(event.getUser().getLongID() == event.getClient().getOurUser().getLongID()){
			return;
		}
		System.out.println("VoiceJoin: " + event.getUser().getName() + " " + event.getVoiceChannel().getName());
		IVoiceChannel voice = event.getVoiceChannel();
		if(voice == null) return;
		voice.join();

		AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(event.getGuild());
		audioP.clear();

		speakinVC(audioP, null, event.getUser().getName() + " joined!");

		if(!event.getGuild().getStringID().equals("189377932429492224")){
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

		long lastvcid = getLastVCID();
		long lastvcuserid = getLastVCUserID();
		if(lastvcid != voice.getLongID() && lastvcuserid != event.getUser().getLongID()){
			System.out.println("VCAlert: lastvcid: " + lastvcid + " | lastvcuserid: " + lastvcuserid + " | " + noBots.size());
			if(noBots.size() == 1){
				IChannel general = event.getGuild().getChannelByID(512242412635029514L);
				general.sendMessage(":telephone_receiver:" + voice.getName() + "で" + event.getUser().getName() + "#" + event.getUser().getDiscriminator() + "が通話をはじめました。");

				setLastVCID(voice.getLongID());
				setLastVCUserID(event.getUser().getLongID());
			}
		}
	}

	@EventSubscriber
	public void onUserVoiceChannelLeaveEvent(UserVoiceChannelLeaveEvent event){
		if(event.getUser().getLongID() == event.getClient().getOurUser().getLongID()){
			return;
		}
		System.out.println("VoiceLeave: " + event.getUser().getName() + " " + event.getVoiceChannel().getName());
		IVoiceChannel voice = event.getVoiceChannel();
		if(voice == null) return;
		voice.join();

		AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(event.getGuild());
		audioP.clear();

		speakinVC(audioP, null, event.getUser().getName() + " Leave");

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
		if(voice == null) return;
		voice.join();

		AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(event.getGuild());
		audioP.clear();

		speakinVC(audioP, null, event.getUser().getName() + " Moved from " + event.getOldChannel().getName() + "!");

		if(!event.getGuild().getStringID().equals("189377932429492224")){
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

		long lastvcid = getLastVCID();
		long lastvcuserid = getLastVCUserID();
		if(lastvcid != voice.getLongID() && lastvcuserid != event.getUser().getLongID()){
			System.out.println("VCAlert: lastvcid: " + lastvcid + " | lastvcuserid: " + lastvcuserid + " | " + noBots.size());
			if(noBots.size() == 1){
				IChannel general = event.getGuild().getChannelByID(512242412635029514L);
				general.sendMessage(":telephone_receiver:" + voice.getName() + "で" + event.getUser().getName() + "#" + event.getUser().getDiscriminator() + "が通話をはじめました。");

				setLastVCID(voice.getLongID());
				setLastVCUserID(event.getUser().getLongID());
			}
		}
	}

	void speakinVC(AudioPlayer audioP, IChannel channel, String message){
		String[] List;
		if(message.contains("\n")){
			List = message.split("\n");
		}else{
			List = new String[]{message};
		}
		for(String msg : List){
			try {
				AudioInputStream stream = EmotionalSpeaker.HIKARI.ready().happy().getResponse(msg).audioInputStream();

				audioP.queue(stream);
			}catch(IllegalArgumentException e){
				if(channel == null) continue;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				channel.sendMessage("[" + sdf.format(new Date()) + "] " + e.getMessage());
			}
		}
	}
	long getLastVCID(){
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
		if(!props.containsKey("vcid")){
			return -1;
		}

		String vcidstr = props.getProperty("vcid");

		try{
			return Long.parseLong(vcidstr);
		}catch(NumberFormatException e){
			return -1;
		}
	}
	void setLastVCID(long vcid){
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

		props.setProperty("vcid", Long.toString(vcid));

		try {
			props.store(new FileOutputStream("vcdata.properties"), "Comments");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	long getLastVCUserID(){
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
		if(!props.containsKey("vcuserid")){
			return -1;
		}

		String vcuseridstr = props.getProperty("vcuserid");

		try{
			return Long.parseLong(vcuseridstr);
		}catch(NumberFormatException e){
			return -1;
		}
	}
	void setLastVCUserID(long vcuserid){
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

		props.setProperty("vcuserid", Long.toString(vcuserid));

		try {
			props.store(new FileOutputStream("vcdata.properties"), "Comments");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
/*
	@EventSubscriber
	public void onFinish(TrackFinishEvent event){
		if(event.getNewTrack().isPresent()){
			return;
		}
		IVoiceChannel botVoiceChannel = event.getClient().getOurUser().getVoiceStateForGuild(event.getPlayer().getGuild()).getChannel();

		if(botVoiceChannel == null)
			return;

		botVoiceChannel.leave();
	}
	*/
}