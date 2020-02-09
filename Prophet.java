import controller.UmiClient;
import java.util.function.BiConsumer;
import java.net.*;
import java.io.*;
import java.util.*;

public class Prophet implements BiConsumer<Map<String, ArrayList<Integer>>, ArrayList<Integer>> {
	private String name;
	public Map<String, ArrayList<Integer>> ships   = new HashMap<String, ArrayList<Integer>>();
	public ArrayList<Integer>              energys = new ArrayList<Integer>();

	public Prophet(String name) {
		this.name = name;
	}

	public void accept(Map<String, ArrayList<Integer>> ships, ArrayList<Integer> energys){
		this.ships   = ships;
		this.energys = energys;
	}

	public static void main(String[] args) {
		String name = "irita";
		UmiClient client = new UmiClient("localhost", 10000, name);
		Prophet prophet  = new Prophet(name);
		client.registerRxHandler(prophet);
		ArrayList<Integer> me, energys;

		for (;;) {
			client.stat();
			try {
				Thread.sleep(25*10);
			} catch (Exception e) {
				return;
			}
			me      = (ArrayList<Integer>)prophet.ships.get(name);
			energys = (ArrayList<Integer>)prophet.energys;
			int[] pos = {0,0,255,255}; //x, y, mX, mY
			if (me == null || energys == null) continue;
			for (Iterator it = energys.iterator(); it.hasNext();) {
				int x     = ((Integer)it.next()).intValue();
				int y     = ((Integer)it.next()).intValue();
				int point = ((Integer)it.next()).intValue();
				int distX = Math.abs(x-((Integer)me.get(0)).intValue());
				int distY = Math.abs(y-((Integer)me.get(1)).intValue());
				if (distX > 128){distX = (255 - distX);}
				if (distY > 128){distY = (255 - distY);}
				if ((distX+distY) > (pos[2]+pos[3])) continue;
				pos[0] = x; pos[1] = y; pos[2] = distX; pos[3] = distY;
			}
			System.out.println("x:" + pos[0]);
			System.out.println("y:" + pos[1]);
			for (int i = 0;i<2;i++) {
				if (pos[2] > pos[3]) {
					if (pos[0] < ((Integer)me.get(0)).intValue()) {
						client.left();
					} else {
						client.right();
					}
					pos[2] -= 1;
				}else{
					if (pos[1] < ((Integer)me.get(1)).intValue()) {
						client.down();
					} else {
						client.up();
					}
					pos[3] -= 1;
				}
			}

			client.right();
			try {
				Thread.sleep(25*10);
			} catch (Exception e) {
				return;
			}
		}
	}
}
