package gartham.c10ver.games.rpg.wilderness;

import java.time.Instant;

import gartham.c10ver.Clover;
import gartham.c10ver.commands.InputProcessor;
import gartham.c10ver.commands.consumers.InputConsumer;
import gartham.c10ver.games.rpg.wilderness.CloverWildernessMap.CloverWildernessTile;
import gartham.c10ver.games.rpg.wilderness.LinkType.AdjacencyLink;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public class WildernessGame {

	private final Instant timestamp = Instant.now();

	private final User target;
	private final GuildMessageChannel channel;
	private final Clover clover;
	private final CloverWildernessMap cwm = new CloverWildernessMap();
	private CloverWildernessTile tile = cwm.getOrigin();
	private boolean active;

	private final WildernessGamepad gamepad = new WildernessGamepad();

	public boolean isActive() {
		return active;
	}

	public WildernessGame(User target, GuildMessageChannel channel, Clover clover) {
		this.target = target;
		this.channel = channel;
		this.clover = clover;
		var msg = channel.sendMessageEmbeds(genEmbed()).setActionRows(gamepad.actionRows()).complete();
		clover.getEventHandler().getButtonClickProcessor().registerInputConsumer(new InputConsumer<>() {

			@Override
			public boolean consume(ButtonClickEvent event, InputProcessor<? extends ButtonClickEvent> processor,
					InputConsumer<ButtonClickEvent> consumer) {
				if (event.getUser().getIdLong() != target.getIdLong() || event.getMessageIdLong() != msg.getIdLong())
					return false;
				switch (event.getComponentId()) {
				case "left":
					tile = tile.get(AdjacencyLink.LEFT);
					break;
				case "right":
					tile = tile.get(AdjacencyLink.LEFT);
					break;
				case "up":
					tile = tile.get(AdjacencyLink.TOP);
					break;
				case "down":
					tile = tile.get(AdjacencyLink.BOTTOM);
				default:
					break;
				}
				event.editMessageEmbeds(genEmbed()).complete();
				return true;
			}
		});
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	private MessageEmbed genEmbed() {
		return new EmbedBuilder().setDescription("```" + tile.tilemapString() + "```")
				.setAuthor(target.getAsTag() + "'s Exploration").build();
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

}
