import controller.UmiClient;
import java.util.function.BiConsumer;
import java.net.*;
import java.io.*;
import java.util.*;

public class Potential implements BiConsumer<Map<String, ArrayList<Integer>>, ArrayList<Integer>> {
	private String name;
	public Map<String, ArrayList<Integer>> ships   = new HashMap<String, ArrayList<Integer>>();
	public ArrayList<Integer>              energys = new ArrayList<Integer>();

	public Potential(String name) {
		this.name = name;
	}

	public void accept(Map<String, ArrayList<Integer>> ships, ArrayList<Integer> energys){
		this.ships   = ships;
		this.energys = energys;
	}

	/*
	private static void putMass(int sea[][], int x, int y, int val) {
		for (int i = 0; i<256; i++) {
			for (int j = 0; j<256; j++) {
				int dx = x - i;
				int dy = y - j;
				if ((dx*dx + dy*dy) < 100) {
					sea[i][j] += val*10;
				} else {
				}
			}
		}
	}
	*/

	public static void main(String[] args) {
		String name;
		if (args.length < 1) {
			name = "potential";
		} else {
			name = args[0];
		}
		UmiClient client = new UmiClient("localhost", 10000, name);
		Potential potential  = new Potential(name);
		client.registerRxHandler(potential);
		ArrayList<Integer> me, energys;

		for (;;) {
			client.stat();
			try {
				Thread.sleep(25*10);
			} catch (Exception e) {
				return;
			}
			me      = (ArrayList<Integer>)potential.ships.get(name);
			energys = (ArrayList<Integer>)potential.energys;
			if (me == null || energys == null) continue;

			int meX = ((Integer)me.get(0)).intValue();
			int meY = ((Integer)me.get(1)).intValue();
			double[] vec = new double[4];

			for (int c = 0;c<2;c++) {
				for (Iterator it = energys.iterator(); it.hasNext();) {
					int x     = ((Integer)it.next()).intValue();
					int y     = ((Integer)it.next()).intValue();
					int point = ((Integer)it.next()).intValue();
					int dX = (x-meX);
					int dY = (y-meY);
					if (Math.abs(dX) > 128) dX = (dX < 0) ? (256+dX) : (256-dX);
					if (Math.abs(dY) > 128) dY = (dY < 0) ? (256+dY) : (256-dY);
					double dist = dX*dX + dY*dY;
					if (dist < 100) continue;
					vec[0] += (double)(point*point*dX)/(dist*dist);
					vec[1] += (double)(point*point*dY)/(dist*dist);
				}
				if (Math.abs(vec[0]) > Math.abs(vec[1])) {
					if (vec[0] < 0) {
						client.left();
						meX -= 10;
					} else {
						client.right();
						meX += 10;
					}
				} else {
					if (vec[1] < 0) {
						client.down();
						meY -= 10;
					} else {
						client.up();
						meY += 10;
					}
				}
			}
			try {
				Thread.sleep(25*10);
			} catch (Exception e) {
				return;
			}
		}
	}
}
