import controller.UmiClient;
import java.util.function.BiConsumer;
import java.net.*;
import java.io.*;
import java.util.*;

public class Prophet implements BiConsumer<Map<String, Object>, java.util.List<Integer>> {
	public int a = 0;
	public void addA(){
		this.a+=1;
	}

	public void accept(Map<String, Object> ships, java.util.List<Integer> energys){
		this.addA();
		System.out.println("hoge");
	}

	public static void main(String[] args) {
		UmiClient client = new UmiClient("localhost", 10000, "irita");
		Prophet prophet  = new Prophet();
		client.registerRxHandler(prophet);

		for (;;) {
			client.stat();
			client.left();
			System.out.println(prophet.a);
			try {
				Thread.sleep(6*100);
			} catch (Exception e) {
				return;
			}
		}
}
}
