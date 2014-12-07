package org.nusco.narjillos.ecosystem;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.nusco.narjillos.creature.Narjillo;
import org.nusco.narjillos.embryogenesis.Embryo;
import org.nusco.narjillos.genomics.DNA;
import org.nusco.narjillos.shared.physics.Segment;
import org.nusco.narjillos.shared.physics.Vector;
import org.nusco.narjillos.shared.things.FoodPiece;
import org.nusco.narjillos.shared.things.Thing;
import org.nusco.narjillos.shared.utilities.RanGen;
import org.nusco.narjillos.shared.utilities.VisualDebugger;

//TODO: check thread-safety (too complicated and fragile right now).
/**
 * The place that Narjillos live in.
 * 
 * Can find things and detect collisions.
 */
public class Ecosystem {

	private static final double COLLISION_DISTANCE = 30;
	private static final int MAX_NUMBER_OF_FOOD_PIECES = 600;
	private static final int FOOD_RESPAWN_AVERAGE_INTERVAL = 100;
	private static final int AREAS_PER_EDGE = 80;

	private final long size;
	private final Set<Narjillo> narjillos = Collections.synchronizedSet(new LinkedHashSet<Narjillo>());

	private final Space foodSpace;
	private final Vector center;

	private final List<EcosystemEventListener> ecosystemEventListeners = new LinkedList<>();
	private final ExecutorService executorService = Executors.newFixedThreadPool(2);

	public Ecosystem(final long size) {
		this.size = size;
		this.foodSpace = new Space(size, AREAS_PER_EDGE);
		this.center = Vector.cartesian(size, size).by(0.5);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				executorService.shutdown();
			}
		});
	}

	public long getSize() {
		return size;
	}

	public Set<Thing> getThings() {
		Set<Thing> result = new LinkedHashSet<Thing>();
		synchronized (this) {
			result.addAll(foodSpace.getAll());
		}
		result.addAll(narjillos);
		return result;
	}

	public Vector findClosestTarget(Narjillo narjillo) {
		Thing target = foodSpace.findClosestTo(narjillo);

		if (target == null)
			return center;
		
		return target.getPosition();
	}

	public Narjillo findNarjillo(Vector near) {
		double minDistance = Double.MAX_VALUE;
		Narjillo result = null;
		for (Narjillo narjillo : narjillos) {
			double distance = narjillo.getPosition().minus(near).getLength();
			if (distance < minDistance) {
				minDistance = distance;
				result = narjillo;
			}
		}
		return result;
	}

	public void tick(RanGen ranGen) {
		List<Narjillo> narjillosCopy = new LinkedList<>(narjillos);

		List<Future<Segment>> movements = tickAll(narjillosCopy);

		for (int i = 0; i < narjillosCopy.size(); i++) {
			Segment movement = waitUntilAvailable(movements, i);

			checkForExcessiveSpeed(movement);
			
			Narjillo narjillo = narjillosCopy.get(i);
			consumeCollidedFood(narjillo, movement, ranGen);
			if (narjillo.isDead())
				remove(narjillo);
		}

		if (shouldSpawnFood(ranGen))
			spawnFood(randomPosition(getSize(), ranGen));

		if (VisualDebugger.DEBUG)
			VisualDebugger.clear();
	}

	private void checkForExcessiveSpeed(Segment movement) {
		if (movement.getVector().getLength() > foodSpace.getAreaSize())
			System.out.println("WARNING: Excessive narjillo speed: " + movement.getVector().getLength() + " for Space area size of " + foodSpace.getAreaSize() + ". Could result in missed collisions.");
	}

	private Segment waitUntilAvailable(List<Future<Segment>> movements, int index) {
		try {
			return movements.get(index).get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private List<Future<Segment>> tickAll(List<Narjillo> narjillos) {
		List<Future<Segment>> result = new LinkedList<>();
		for (int i = 0; i < narjillos.size(); i++) {
			final Narjillo narjillo = narjillos.get(i);
			result.add(executorService.submit(new Callable<Segment>() {
				@Override
				public Segment call() throws Exception {
					return narjillo.tick();
				}
			}));
		}
		return result;
	}

	private boolean shouldSpawnFood(RanGen ranGen) {
		return getNumberOfFoodPieces() < MAX_NUMBER_OF_FOOD_PIECES && ranGen.nextDouble() < 1.0 / FOOD_RESPAWN_AVERAGE_INTERVAL;
	}

	private Vector randomPosition(long size, RanGen ranGen) {
		return Vector.cartesian(ranGen.nextDouble() * size, ranGen.nextDouble() * size);
	}

	public final FoodPiece spawnFood(Vector position) {
		FoodPiece newFood = new FoodPiece();
		newFood.setPosition(position);
		forceAdd(newFood);
		return newFood;
	}

	public void forceAdd(FoodPiece food) {
		foodSpace.add(food);
		notifyThingAdded(food);
	}

	public final Narjillo spawnNarjillo(DNA genes, Vector position) {
		final Narjillo narjillo = new Narjillo(genes, new Embryo(genes).develop(), position);
		forceAdd(narjillo);
		return narjillo;
	}

	public void forceAdd(final Narjillo narjillo) {
		narjillos.add(narjillo);
		notifyThingAdded(narjillo);
	}

	private void updateTargets(Thing food) {
		for (Thing creature : narjillos) {
			if (((Narjillo) creature).getTarget() == food) {
				Narjillo narjillo = (Narjillo) creature;
				Vector closestTarget = findClosestTarget(narjillo);
				narjillo.setTarget(closestTarget);
			}
		}
	}

	public void updateAllTargets() {
		for (Thing creature : narjillos) {
			Narjillo narjillo = (Narjillo) creature;
			Vector closestTarget = findClosestTarget(narjillo);
			narjillo.setTarget(closestTarget);
		}
	}

	private void consumeCollidedFood(Narjillo narjillo, Segment movement, RanGen ranGen) {
		Set<Thing> collidedFoodPieces = new LinkedHashSet<>();

		for (Thing nearbyFood : foodSpace.getNearbyNeighbors(narjillo))
			if (checkCollisionWithFood(movement, nearbyFood))
				collidedFoodPieces.add(nearbyFood);

		for (Thing collidedFoodPiece : collidedFoodPieces)
			consumeFood(narjillo, collidedFoodPiece, ranGen);
	}

	private boolean checkCollisionWithFood(Segment movement, Thing foodPiece) {
		return movement.getMinimumDistanceFromPoint(foodPiece.getPosition()) <= COLLISION_DISTANCE;
	}

	private void consumeFood(Narjillo narjillo, Thing foodPiece, RanGen ranGen) {
		if (!foodSpace.contains(foodPiece))
			return; // race condition: already consumed

		notifyThingRemoved(foodPiece);
		foodSpace.remove(foodPiece);

		narjillo.feedOn(foodPiece);

		Vector offset = Vector.cartesian(getRandomInRange(3000, ranGen), getRandomInRange(3000, ranGen));
		Vector position = narjillo.getPosition().plus(offset);

		reproduce(narjillo, position, ranGen);
		updateTargets(foodPiece);
	}

	private void remove(Narjillo narjillo) {
		if (!narjillos.contains(narjillo))
			return;
		narjillos.remove(narjillo);
		narjillo.getDNA().removeFromPool();
		notifyThingRemoved(narjillo);
	}

	private void reproduce(Narjillo narjillo, Vector position, RanGen ranGen) {
		final Narjillo child = narjillo.reproduce(position, ranGen);
		if (child == null) // refused to reproduce
			return;
		forceAdd(child);
	}

	private double getRandomInRange(final int range, RanGen ranGen) {
		return (range * 2 * ranGen.nextDouble()) - range;
	}

	private final void notifyThingAdded(Thing thing) {
		for (EcosystemEventListener ecosystemEvent : ecosystemEventListeners)
			ecosystemEvent.thingAdded(thing);
	}

	private final void notifyThingRemoved(Thing thing) {
		for (EcosystemEventListener ecosystemEvent : ecosystemEventListeners)
			ecosystemEvent.thingRemoved(thing);
	}

	public void addEventListener(EcosystemEventListener ecosystemEventListener) {
		ecosystemEventListeners.add(ecosystemEventListener);
	}

	public int getNumberOfFoodPieces() {
		return foodSpace.getAll().size();
	}

	public int getNumberOfNarjillos() {
		return narjillos.size();
	}

	public Set<Thing> getFoodPieces() {
		return foodSpace.getAll();
	}

	public Set<Narjillo> getNarjillos() {
		return narjillos;
	}
}
