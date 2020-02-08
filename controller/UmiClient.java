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
	private Thread           readloop;

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
			return;
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

	//ships  Map<String, int[]>
	//energy Map<int   , int[]>
	public void registerRxHandler(BiConsumer<Map<String, Object>, java.util.List<Integer>> func) {
		if (readloop != null) {
	//		readloop.stop();
		}
		readloop = new Thread(new Runnable() {
			private BiConsumer<Map<String, Object>, java.util.List<Integer>> callbackHandler;
			private boolean isLive = true;

			public void die() {
				this.isLive = false;
			}

			public  Runnable setCallbackHandler(BiConsumer<Map<String, Object>, java.util.List<Integer>> callback) {
				callbackHandler = callback;
				return this;
			}

			public void run() {
				for (;isLive;) {
					//船の座標をパース
					Map<String, Object> ships = new HashMap<>();
					String  line, player;
					Scanner scanner;
					for (line = readLine(); !"ship_info".equals(line); line = readLine());
					for (line = readLine(); !".".equals(line)        ; line = readLine()) {
						scanner = new Scanner(line);
						int arr[] = new int[3];
						player = scanner.next();
						arr[0] = scanner.nextInt(); //y
						arr[1] = scanner.nextInt(); //x
						arr[2] = scanner.nextInt(); //point
						ships.put(player, arr);
						scanner.close();
					}
					//エネルギーのリストをパース
					java.util.List<Integer> energys = new ArrayList<Integer>();
					for (line = readLine(); !"energy_info".equals(line); line = readLine());
					for (line = readLine(); !".".equals(line)          ; line = readLine()) {
						scanner = new Scanner(line);
						energys.add(scanner.nextInt());
						energys.add(scanner.nextInt());
						energys.add(scanner.nextInt());
						scanner.close();
					}
					//ハンドラの呼び出し
					System.out.println("accept!!!");
					callbackHandler.accept(ships, energys);
				}
			}
		}.setCallbackHandler(func));
		readloop.start();
	}

	private String readLine() {
		try {
			String s = in.readLine();
			System.out.println(s);
			return s;
		} catch (Exception e) {
			return "error";
		}
	}
}

