import controller.UmiClient;
import java.util.function.BiConsumer;
import java.net.*;
import java.io.*;
import java.util.*;

public class Assault implements BiConsumer<Map<String, ArrayList<Integer>>, ArrayList<Integer>> {
	private String name;
	public Map<String, ArrayList<Integer>> ships   = new HashMap<String, ArrayList<Integer>>();
	public ArrayList<Integer>              energys = new ArrayList<Integer>();

	public Assault(String name) {
		this.name = name;
	}

	public void accept(Map<String, ArrayList<Integer>> ships, ArrayList<Integer> energys){
		this.ships   = ships;
		this.energys = energys;
	}

	public static void main(String[] args) {
		String name;
		if (args.length < 1) {
			name = "assault";
		} else {
			name = args[0];
		}
		UmiClient client = new UmiClient("localhost", 10000, name);
		Assault assault  = new Assault(name);
		client.registerRxHandler(assault);
		ArrayList<Integer> me, energys;

		for (;;) {
			client.stat();
			try {
				Thread.sleep(25*10);
			} catch (Exception e) {
				return;
			}
			me      = (ArrayList<Integer>)assault.ships.get(name);
			energys = (ArrayList<Integer>)assault.energys;
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
	}
}
