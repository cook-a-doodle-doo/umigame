import controller.UmiClient;
import java.util.function.BiConsumer;
import java.net.*;
import java.io.*;
import java.util.*;

public class Prophet implements BiConsumer<Map<String, ArrayList<Integer>>, ArrayList<Integer>> {
	private int playersNum;
	private long seed;
	private String name;
	private UmiClient client;
	public Map<String, ArrayList<Integer>> ships   = new HashMap<String, ArrayList<Integer>>();
	public ArrayList<Integer>              energys = new ArrayList<Integer>();
	public List<Long> predictions = new LinkedList<Long>();
	public boolean isDetermine = false;
	public long history = System.currentTimeMillis() - 60*1000;

	public class Energy {
		public int x, y;
		public int point;
	}

	public Energy nextEnergy () {
		Energy e = new Energy();
		this.seed = (this.seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
		int v = (int)(seed >>> (48 - 32));
		System.out.println("energy x: " + v);
		e.x = Math.abs(v)%256;

		this.seed = (this.seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
		v = (int)(seed >>> (48 - 32));
		System.out.println("energy y: " + v);
		e.y = Math.abs(v)%256;

		this.seed = (this.seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
		v = (int)(seed >>> (48 - 32));
		System.out.println("energy point: " + v);
		int tmpval = Math.abs(v)%8;

		if      (tmpval < 2) e.point = 1;
		else if (tmpval < 4) e.point = 2;
		else if (tmpval < 6) e.point = 3;
		else                 e.point = 4; // エネルギータンクの点数
		return e;
	}

	public Prophet(String name, int num) {
		try {
			this.playersNum = num;
			this.name       = name;
			this.client     = new UmiClient("localhost", 10000, name);
			this.client.registerRxHandler(this);
		}catch(Exception e){
			System.out.println("error in constractor umiclient");
			throw new IllegalStateException("can't find server");
		}
		client.stat();
		ArrayList<Integer> me;
		for (;;) {
			me = this.ships.get(this.name);
			if (me != null) break;
		}
		int meX = ((Integer)me.get(0)).intValue();
		int meY = ((Integer)me.get(1)).intValue();

		for (long i=0; i<65*1000; i++) {
			long prediction = this.history + i;
			long s = (prediction ^ 0x5DEECE66DL) & ((1L << 48) - 1);
			boolean isConsistent = false;
			for (int j=0; j<this.playersNum; j++){
				s = (s * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
				int v = (int)(s >>> (48 - 32));
				if (Math.abs(v)%256 != meX) continue;
				s = (s * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
				v = (int)(s >>> (48 - 32));
				if (Math.abs(v)%256 != meY) continue;
				isConsistent = true;
				break;
			}
			if (isConsistent) {
				predictions.add(s);
			}
		}
		if (predictions.size() == 0) System.out.println("divination is failed");
		if (predictions.size() == 1) {
			isDetermine = true;
			this.seed = predictions.get(0);
		}
	}

	public void accept(Map<String, ArrayList<Integer>> ships, ArrayList<Integer> energys){
		this.ships   = ships;
		this.energys = energys;
	}

	public void divide (int num) {
		int ex = ((Integer)(energys.get(num*3+0))).intValue();
		int ey = ((Integer)(energys.get(num*3+1))).intValue();
		int ep = ((Integer)(energys.get(num*3+2))).intValue();
		for (Iterator it = this.predictions.iterator(); it.hasNext();) {
			this.seed = (Long)it.next();
			Energy e = nextEnergy();
			if (e.x != ex)     {it.remove(); continue;}
			if (e.y != ey)     {it.remove(); continue;}
			if (e.point != ep) {it.remove(); continue;}
			this.seed = (this.seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
			this.seed = (this.seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
			this.seed = (this.seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
			isDetermine = true;
			return;
		}
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("I need players number");
			return;
		}
		int playerNum = Integer.parseInt(args[0]);
		String name = (args.length < 2) ? "prophet" : args[1];
		Prophet prophet  = new Prophet(name, playerNum);

		System.out.println(prophet.history);
		Map<String, ArrayList<Integer>> ships;
		ArrayList<Integer> energys;
		ArrayList<Integer> me;

		for (;;) {
			prophet.client.stat();
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				return;
			}
			ships   = (Map<String, ArrayList<Integer>>)prophet.ships;
			me      = (ArrayList<Integer>)ships.get(name);
			energys = (ArrayList<Integer>)prophet.energys;
			if (me == null || energys == null) continue;
			if (prophet.isDetermine) {
				System.out.println("hoge");
				Energy e = prophet.nextEnergy();
				System.out.println("x: " + e.x);
				System.out.println("y: " + e.y);
				System.out.println("point: " + e.point);
			} else {
				System.out.println("分からない: ");
//				prophet.divide(0);
			}
			try {
				Thread.sleep(4000);
			} catch (Exception e) {
				return;
			}
		}


		//		client.die();
		/*
			for (;;) {
			client.stat();
			try {
			Thread.sleep(25*10);
			} catch (Exception e) {
			return;
			}
			me      = (ArrayList<Integer>)prophet.ships.get(name);
			energys = (ArrayList<Integer>)prophet.energys;
			if (me == null || energys == null) continue;

			int meX = ((Integer)me.get(0)).intValue();
			int meY = ((Integer)me.get(1)).intValue();
			int[] pos = {0,0,255,255}; //x, y, distX, distY
			for (Iterator it = energys.iterator(); it.hasNext();) {
			int x     = ((Integer)it.next()).intValue();
			int y     = ((Integer)it.next()).intValue();
			int point = ((Integer)it.next()).intValue();
		//if (point == 1) continue;
		int distX = Math.abs(x-meX);
		int distY = Math.abs(y-meY);
		if (distX > 127){distX = (256 - distX);}
		if (distY > 127){distY = (256 - distY);}
		if ((distX+distY) > (pos[2]+pos[3])) continue;
		pos[0] = x; pos[1] = y; pos[2] = distX; pos[3] = distY;
			}
			for (int i = 0;i<2;i++) {
			if (pos[2] > pos[3]) {
			if (pos[2] > Math.abs((meX+256-10)%256 - pos[0])) {
			client.left();
			meX = (meX+256-10)%256;
			} else {
			client.right();
			meX = (meX+256+10)%256;
			}
			pos[2] -= 10;
			}else{
			if (pos[3] > Math.abs((meY+256-10)%256 - pos[1])) {
			client.down();
			meY = (meY+256-10)%256;
			} else {
			client.up();
			meY = (meY+256+10)%256;
			}
			pos[3] -= 10;
			}
			}

			client.right();
			try {
			Thread.sleep(25*10);
			} catch (Exception e) {
			return;
			}
			}
			*/
	}
}
