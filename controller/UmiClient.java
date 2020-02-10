package controller;

import java.awt.*;// グラフィックス
import java.awt.event.*;// イベント関連
import java.net.*;// ネットワーク関連
import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;

public class UmiClient {
	private Socket server;
	private String name;
	private BufferedReader   in; 
	private PrintWriter      out; 
	private ReadLoop         readloop;

	public UmiClient(String host, int port, String name) {
		try {
			this.server = new Socket(host, port);
			this.name   = name;
			this.in     = new BufferedReader(new InputStreamReader(server.getInputStream()));
			this.out    = new PrintWriter(server.getOutputStream());
			this.out.println("login " + name);
			this.out.flush();
		}catch(Exception e){
			System.out.println("error in constractor umiclient");
			throw new IllegalStateException("can't find server");
		}
	}

	public void up(){
		out.println("up");
		out.flush();
	}

	public void down(){
		out.println("down");
		out.flush();
	}

	public void left(){
		out.println("left");
		out.flush();
	}

	public void right(){
		out.println("right");
		out.flush();
	}

	public void stat(){
		out.println("stat");
		out.flush();
	}

	public void logout(){
		out.println("logout");
		out.flush();
	}

	public void die() {
		if (readloop == null) return;
		readloop.die();
	}

	private class ReadLoop extends Thread{
		private BiConsumer<Map<String, ArrayList<Integer>>, ArrayList<Integer>> callbackHandler;
		private boolean isLive = true;

		public void die() {
			this.isLive = false;
		}

		public Runnable setCallbackHandler(BiConsumer<Map<String, ArrayList<Integer>>, ArrayList<Integer>> callback) {
			callbackHandler = callback;
			return this;
		}

		@Override
		public void run() {
			for (;isLive;) {
				//船の座標をパース
				Map<String, ArrayList<Integer>> ships = new HashMap<String, ArrayList<Integer>>();
				String  line, player;
				Scanner scanner;
				for (line = readLine(); !"ship_info".equals(line); line = readLine());
				for (line = readLine(); !".".equals(line)        ; line = readLine()) {
					scanner = new Scanner(line);
					//int arr[] = new int[3];
					player = scanner.next();
					ArrayList<Integer> arr = new ArrayList<Integer>(Arrays.asList(
								scanner.nextInt(),
								scanner.nextInt(),
								scanner.nextInt()));
					ships.put(player, arr);
					scanner.close();
				}
				//エネルギーのリストをパース
				ArrayList<Integer> energys = new ArrayList<Integer>();
				for (line = readLine(); !"energy_info".equals(line); line = readLine());
				for (line = readLine(); !".".equals(line)          ; line = readLine()) {
					scanner = new Scanner(line);
					energys.add(scanner.nextInt()); //x
					energys.add(scanner.nextInt()); //y
					energys.add(scanner.nextInt()); //point
					scanner.close();
				}
				//ハンドラの呼び出し
				callbackHandler.accept(ships, energys);
			}
		}
	}

	public void registerRxHandler(BiConsumer<Map<String, ArrayList<Integer>>, ArrayList<Integer>> func) {
		ReadLoop readloop = new ReadLoop();
		readloop.setCallbackHandler(func);
		readloop.start();
		this.readloop = readloop;
	}

/*
	public void registerRxHandler(BiConsumer<Map<String, ArrayList<Integer>>, ArrayList<Integer>> func) {
	if (readloop != null) {
//		readloop.stop();
	}
	readloop = new Thread(new Runnable() {
	private BiConsumer<Map<String, ArrayList<Integer>>, ArrayList<Integer>> callbackHandler;
	private boolean isLive = true;

	public void die() {
	this.isLive = false;
	}

	public  Runnable setCallbackHandler(BiConsumer<Map<String, ArrayList<Integer>>, ArrayList<Integer>> callback) {
	callbackHandler = callback;
	return this;
	}

	public void run() {
	for (;isLive;) {
//船の座標をパース
Map<String, ArrayList<Integer>> ships = new HashMap<String, ArrayList<Integer>>();
String  line, player;
Scanner scanner;
for (line = readLine(); !"ship_info".equals(line); line = readLine());
for (line = readLine(); !".".equals(line)        ; line = readLine()) {
scanner = new Scanner(line);
//int arr[] = new int[3];
player = scanner.next();
ArrayList<Integer> arr = new ArrayList<Integer>(Arrays.asList(
scanner.nextInt(),
scanner.nextInt(),
scanner.nextInt()));
ships.put(player, arr);
scanner.close();
}
//エネルギーのリストをパース
ArrayList<Integer> energys = new ArrayList<Integer>();
for (line = readLine(); !"energy_info".equals(line); line = readLine());
for (line = readLine(); !".".equals(line)          ; line = readLine()) {
scanner = new Scanner(line);
energys.add(scanner.nextInt()); //x
energys.add(scanner.nextInt()); //y
energys.add(scanner.nextInt()); //point
scanner.close();
}
//ハンドラの呼び出し
callbackHandler.accept(ships, energys);
	}
	}
	}.setCallbackHandler(func));
	readloop.start();
	}
	*/

private String readLine() {
	try {
		return in.readLine();
	} catch (Exception e) {
		return "error in readLine()";
	}
}
}


