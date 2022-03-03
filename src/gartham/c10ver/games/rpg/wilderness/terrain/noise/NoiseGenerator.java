package gartham.c10ver.games.rpg.wilderness.terrain.noise;

import javax.tools.JavaFileManager.Location;

public interface NoiseGenerator {
	double[][] noisemap(Location tileLocation, int xStart, int yStart, int xEnd, int yEnd, int xSize, int ySize);
}
