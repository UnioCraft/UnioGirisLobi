package me.UnioDex.UnioGirisLobi3;

import java.util.Calendar;

import org.bukkit.World;

public class RealTimeTask
implements Runnable
{
	private final Main plugin;

	public RealTimeTask(Main plugin)
	{
		this.plugin = plugin;
	}

	public void run()
	{
		Calendar c = Calendar.getInstance();
		int h = c.get(11);
		h -= 6;
		if (h < 0) {
			h += 24;
		}
		int m = c.get(12);
		int s = c.get(13);
		long seconds = h * 60 * 60 + m * 60 + s;
		long ticks = (long) secToTicks(seconds);
		for (World w : plugin.getServer().getWorlds()) {
			w.setTime(ticks);
		}
	}

	private double secToTicks(long sec)
	{
		return Math.floor(0.2777778F * (float)sec);
	}
}
