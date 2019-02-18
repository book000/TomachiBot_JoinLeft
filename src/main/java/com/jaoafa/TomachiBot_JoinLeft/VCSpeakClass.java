package com.jaoafa.TomachiBot_JoinLeft;

import javax.sound.sampled.AudioInputStream;

import am.ik.voicetext4j.EmotionalSpeaker;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.audio.AudioPlayer;

public class VCSpeakClass {
	private IVoiceChannel channel;
	private String message;

	public VCSpeakClass(IVoiceChannel channel, String message){
		this.channel = channel;
		this.message = message;
	}

	void run(){
		join();
		speak();
	}

	void join(){
		channel.join();
	}

	AudioPlayer getAudioPlayer(){
		return AudioPlayer.getAudioPlayerForGuild(
			channel.getGuild()
		);
	}

	void speak(){
		String[] List;
		if(message.contains("\n")){
			List = message.split("\n");
		}else{
			List = new String[]{message};
		}
		for(String msg : List){
			try {
				AudioInputStream stream = EmotionalSpeaker.HIKARI.ready().happy().getResponse(msg).audioInputStream();

				AudioPlayer audioP = getAudioPlayer();

				audioP.clear();

				audioP.queue(stream);
			}catch(IllegalArgumentException e){
			}
		}
	}


}
