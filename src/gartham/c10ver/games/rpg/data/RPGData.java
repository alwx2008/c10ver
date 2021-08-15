package gartham.c10ver.games.rpg.data;

import java.io.File;

import gartham.c10ver.data.autosave.SavablePropertyObject;
import gartham.c10ver.economy.users.User;
import gartham.c10ver.games.rpg.creatures.CreatureBox;

public class RPGData extends SavablePropertyObject {
	private final CreatureBox creatures;
	private final User user;

	public RPGData(File saveLocation, User user) {
		this(saveLocation, user, true);
	}

	public RPGData(File saveLocation, User user, boolean load) {
		super(new File(saveLocation, "data.txt"));
		this.user = user;
		creatures = new CreatureBox(new File(saveLocation, "creatures.txt"));
		if (load)
			load();
	}

}
