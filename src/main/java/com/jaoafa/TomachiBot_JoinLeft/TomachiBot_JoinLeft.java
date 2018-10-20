package com.jaoafa.TomachiBot_JoinLeft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

public class TomachiBot_JoinLeft {
	public static void main(String[] args) {
		File f = new File("conf.properties");
		Properties props;
		try{
			InputStream is = new FileInputStream(f);

			// プロパティファイルを読み込む
			props = new Properties();
			props.load(is);
		}catch(FileNotFoundException e){
			// ファイル生成
			props = new Properties();
			props.setProperty("token", "PLEASETOKEN");
			props.setProperty("voicetext_apikey", "PLEASETOKEN");
			try {
				props.store(new FileOutputStream("conf.properties"), "Comments");
				System.out.println("Please Config Token!");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				return;
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		// キーを指定して値を取得する
		String token = props.getProperty("token");
		if(token.equalsIgnoreCase("PLEASETOKEN")){
			System.out.println("Please Token!");
			return;
		}

		// キーを指定して値を取得する
		String voicetext_apikey = props.getProperty("voicetext_apikey");
		if(voicetext_apikey.equalsIgnoreCase("PLEASETOKEN")){
			System.out.println("Please Token!");
			return;
		}
		System.setProperty("voicetext.apikey", voicetext_apikey);

		IDiscordClient client = createClient(token, true);
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(new BotReadyEvent());
	}
	public static IDiscordClient createClient(String token, boolean login) { // Returns a new instance of the Discord client
		ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
		clientBuilder.withToken(token); // Adds the login info to the builder
		try {
			if (login) {
				return clientBuilder.login(); // Creates the client instance and logs the client in
			} else {
				return clientBuilder.build(); // Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
			}
		} catch (DiscordException e) { // This is thrown if there was a problem building the client
			e.printStackTrace();
			return null;
		}
	}
}
