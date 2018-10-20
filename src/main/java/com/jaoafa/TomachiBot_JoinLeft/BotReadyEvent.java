package com.jaoafa.TomachiBot_JoinLeft;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sound.sampled.AudioInputStream;

import am.ik.voicetext4j.EmotionalSpeaker;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.events.TrackFinishEvent;

public class BotReadyEvent {
	@EventSubscriber
	public void onReadyEvent(ReadyEvent event) {
		System.out.println("Ready: " + event.getClient().getOurUser().getName());


	}
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		System.out.println("Msg: " + event.getAuthor().getName() + " " + event.getMessage().getContent());
		IVoiceChannel voice = event.getGuild().getVoiceChannelByID(189377933356302336L);
		if(voice == null) return;
		voice.join();

		AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(event.getGuild());


		speakinVC(audioP, event.getChannel(), event.getMessage().getContent());
	}

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
}