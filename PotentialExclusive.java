import controller.UmiClient;
import java.util.function.BiConsumer;
import java.net.*;
import java.io.*;
import java.util.*;

public class PotentialExclusive implements BiConsumer<Map<String, ArrayList<Integer>>, ArrayList<Integer>> {
	private String name;
	public Map<String, ArrayList<Integer>> ships   = new HashMap<String, ArrayList<Integer>>();
	public ArrayList<Integer>              energys = new ArrayList<Integer>();

	public PotentialExclusive(String name) {
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
			name = "P-exclusiv";
		} else {
			name = args[0];
		}
		UmiClient client = new UmiClient("localhost", 10000, name);
		PotentialExclusive potential  = new PotentialExclusive(name);
		client.registerRxHandler(potential);
		ArrayList<Integer> me, energys;
		Map<String, ArrayList<Integer>> ships;

		for (;;) {
			client.stat();
			try {
				Thread.sleep(25*10);
			} catch (Exception e) {
				return;
			}
			ships   = (Map<String, ArrayList<Integer>>)potential.ships;
			me      = (ArrayList<Integer>)potential.ships.get(name);
			energys = (ArrayList<Integer>)potential.energys;
			if (me == null || energys == null || ships == null) continue;

			int meX = ((Integer)me.get(0)).intValue();
			int meY = ((Integer)me.get(1)).intValue();
			double[] vec = new double[2];

			for (Iterator<String> it = ships.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				if (key == potential.name) continue;
				ArrayList<Integer> arr = ships.get(key);
				int x     = ((Integer)arr.get(0)).intValue();
				int y     = ((Integer)arr.get(1)).intValue();
				int point = ((Integer)arr.get(2)).intValue();
				int dX = (x-meX);
				int dY = (y-meY);
				if (Math.abs(dX) > 128) dX = (dX < 0) ? (256+dX) : (256-dX);
				if (Math.abs(dY) > 128) dY = (dY < 0) ? (256+dY) : (256-dY);
				double dist = dX*dX + dY*dY;
				if (dist < 100) continue;
				vec[0] -= ((double)5/dX)/(dist);
				vec[1] -= ((double)5/dY)/(dist);
			}

			double[] vector = vec.clone();
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
					vector[0] += (double)(point*point*dX)/(dist*dist);
					vector[1] += (double)(point*point*dY)/(dist*dist);
				}
				if (Math.abs(vector[0]) > Math.abs(vector[1])) {
					if (vector[0] < 0) {
						client.left();
						meX -= 10;
					} else {
						client.right();
						meX += 10;
					}
				} else {
					if (vector[1] < 0) {
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
