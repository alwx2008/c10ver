package gartham.c10ver.events;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import gartham.c10ver.Clover;
import gartham.c10ver.economy.Multiplier;
import gartham.c10ver.economy.Rewards;
import gartham.c10ver.economy.items.ItemBunch;
import gartham.c10ver.economy.items.utility.crates.DailyCrate;
import gartham.c10ver.economy.items.utility.crates.MonthlyCrate;
import gartham.c10ver.economy.items.utility.crates.NormalCrate;
import gartham.c10ver.economy.items.utility.crates.WeeklyCrate;
import gartham.c10ver.economy.items.utility.foodstuffs.Hamburger;
import gartham.c10ver.economy.items.utility.foodstuffs.Pizza;
import gartham.c10ver.economy.items.utility.foodstuffs.Sandwich;
import gartham.c10ver.economy.items.valuables.VoteToken;
import gartham.c10ver.economy.items.valuables.VoteToken.Type;
import gartham.c10ver.economy.users.User;
import gartham.c10ver.utils.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class VoteManager {

	private final Timer timer = new Timer(true);
	private final Map<String, Map<String, TimerTask>> tasks = new HashMap<>();

	private final Clover clover;

	public VoteManager(Clover clover) {
		this.clover = clover;
	}

	public void handleVoteRoleAdded(Member member) {
		List<ItemBunch<?>> items = new ArrayList<>();
		items.add(new ItemBunch<>(new WeeklyCrate(), Math.random() > 0.5 ? 3 : 2));
		if (Math.random() > 0.5)
			items.add(new ItemBunch<>(new MonthlyCrate()));
		if (Math.random() > 0.95)
			items.add(new ItemBunch<>(new DailyCrate(), 50));
		items.add(new ItemBunch<>(new NormalCrate(), (long) (Math.random() * 5 + 3)));
		items.add(new ItemBunch<>(new Pizza(), (long) (Math.random() * 5 + 3)));
		items.add(new ItemBunch<>(new Sandwich(), (long) (Math.random() * 7 + 3)));
		items.add(new ItemBunch<>(new Hamburger(), (long) (Math.random() * 2 + 4)));
		items.add(new ItemBunch<>(new VoteToken(Type.NORMAL), 5));

		List<Multiplier> multipliers = new ArrayList<>();
		if (Math.random() > 0.2)
			multipliers.add(Multiplier.ofHr(12, BigDecimal.valueOf(2, 1)));
		if (Math.random() > 0.3)
			multipliers.add(Multiplier.ofHr(12, BigDecimal.valueOf(3, 1)));
		if (Math.random() > 0.5)
			multipliers.add(Multiplier.ofHr(12, BigDecimal.valueOf(5, 1)));

		Rewards rewards = new Rewards(items, BigInteger.valueOf((long) (Math.random() * 3000 + 5000)));

		User u = clover.getEconomy().getUser(member.getId());
		u.incrementVoteCount();
		var rec = u.rewardAndSave(rewards, member.getGuild());
		u.save();
		var s = clover.getEconomy().getServer(member.getGuild().getId());

		EmbedBuilder eb = new EmbedBuilder();

		MessageEmbed embed = eb
				.setAuthor(member.getUser().getAsTag() + " just voted!", null, member.getUser().getEffectiveAvatarUrl())
				.setDescription(
						member.getUser().getAsMention() + " just voted and received:\n" + Utilities.listRewards(rec)
								+ "\n\nYou can [vote on top.gg by clicking me](https://top.gg/servers/"
								+ member.getGuild().getId() + "/vote).")
				.build();

		if (u.getSettings().isVoteRemindersEnabled()) {
			TimerTask task = new TimerTask() {

				@Override
				public void run() {
					remove(member.getId(), s.getServerID());
					MessageChannel channel = s.getGeneralChannel() != null
							? member.getGuild().getTextChannelById(s.getGeneralChannel())
							: member.getUser().openPrivateChannel().complete();
					channel.sendMessage("Hey there " + member.getAsMention() + "! It's time to vote for **"
							+ member.getGuild().getName() + "**! Here's the vote link: https://top.gg/servers/"
							+ member.getGuild().getId()
							+ "/vote\n\n(You can disable this using the command: `~settings vr false`.)").queue();
				}
			};
			timer.schedule(task, Date.from(Instant.now().plus(12, ChronoUnit.HOURS)));

			var o = put(member.getId(), s.getServerID(), task);
			if (o != null)
				o.cancel();
		}

		MessageChannel channel = s.getGeneralChannel() != null
				? member.getGuild().getTextChannelById(s.getGeneralChannel())
				: member.getUser().openPrivateChannel().complete();
		channel.sendMessage(embed).queue(
//				t -> {
//					t.addReaction("\u274C").queue(t2 -> t.addReaction("\u2611").queue());
//				}
		);
	}

	public TimerTask put(String user, String server, TimerTask task) {
		var u = tasks.get(user);
		if (u == null)
			tasks.put(user, u = new HashMap<>());

		var s = u.get(server);
		u.put(server, task);
		return s;
	}

	public TimerTask remove(String user, String server) {
		var u = tasks.get(user);
		if (u != null) {
			var t = u.remove(server);
			if (u.isEmpty())
				tasks.remove(user);
			return t;
		}
		return null;
	}

	public TimerTask get(String user, String server) {
		var u = tasks.get(user);
		return u == null ? null : u.get(server);
	}

	public boolean contains(String user, String server) {
		return get(user, server) != null;
	}

	public void setVotingRemindersEnabled(User user, String server, boolean b) {
		if (!b)
			remove(user.getUserID(), server).cancel();
		user.getSettings().setVoteRemindersEnabled(b);
		user.getSettings().save();
	}
}
