package gartham.c10ver.games.rpg.fighting.battles;

import java.util.function.Consumer;

import gartham.c10ver.actions.Action;
import gartham.c10ver.actions.ActionInvocation;

public class AttackAction extends Action {
	private final String description;

	public static AttackAction msg(String emoji, String name, String desc, AttackActionMessage msg,
			Consumer<ActionInvocation> action) {
		return new AttackAction(emoji, name, t -> {
			action.accept(t);
			msg.send(t.getClover(), t.getEvent().getChannel(), t.getEvent().getUser());
		}, desc);
	}

	public AttackAction(String emoji, String name, Consumer<ActionInvocation> action, String description) {
		super(emoji, name, action);
		this.description = description;
	}

	public AttackAction(String name, Consumer<ActionInvocation> action, String description) {
		this(null, name, action, description);
	}

	public String getOptionDescription() {
		return description;
	}

}
