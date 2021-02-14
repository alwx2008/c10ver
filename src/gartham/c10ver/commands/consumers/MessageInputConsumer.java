package gartham.c10ver.commands.consumers;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface MessageInputConsumer extends InputConsumer<MessageReceivedEvent> {
	/**
	 * Returns an {@link InputConsumer} which calls this {@link InputConsumer} if
	 * the author of the message has the specified ID. Otherwise, it returns
	 * <code>false</code>.
	 * 
	 * @param userID
	 * @return
	 */
	default InputConsumer<MessageReceivedEvent> filterUser(String userID) {
		return (event, eventHandler, consumer) -> {
			if (event.getAuthor().getId().equals(userID))
				return consume(event, eventHandler, consumer);
			else
				return false;
		};
	}

	default InputConsumer<MessageReceivedEvent> filterChannel(String channelID) {
		return (event, eventHandler, consumer) -> {
			if (event.getChannel().getId().equals(channelID))
				return consume(event, eventHandler, consumer);
			else
				return false;
		};
	}

	default InputConsumer<MessageReceivedEvent> filter(String userID, String channelID) {
		return (event, eventHandler, consumer) -> {
			if (event.getChannel().getId().equals(channelID) && event.getAuthor().getId().equals(userID))
				return consume(event, eventHandler, consumer);
			else
				return false;
		};
	}

	default InputConsumer<MessageReceivedEvent> filter(User user, MessageChannel channel) {
		return (event, eventHandler, consumer) -> {
			if (event.getChannel().getId().equals(channel.getId()) && event.getAuthor().getId().equals(user.getId()))
				return consume(event, eventHandler, consumer);
			else
				return false;
		};
	}
}
