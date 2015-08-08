package org.nusco.narjillos.application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import org.nusco.narjillos.application.utilities.NarjillosApplicationState;
import org.nusco.narjillos.application.utilities.StoppableThread;
import org.nusco.narjillos.application.views.EnvirommentView;
import org.nusco.narjillos.application.views.MicroscopeView;
import org.nusco.narjillos.application.views.SpecklesView;
import org.nusco.narjillos.application.views.StringView;
import org.nusco.narjillos.core.physics.Vector;
import org.nusco.narjillos.core.utilities.RanGen;
import org.nusco.narjillos.creature.Narjillo;
import org.nusco.narjillos.creature.body.Body;
import org.nusco.narjillos.genomics.DNA;

/**
 * This application loads a list of genomes (typically a germline) and shows
 * phenotypes for each genome.
 * 
 * (Alternately, it can also show a lineup of random creatures).
 */
public class DNABrowserApplication extends NarjillosApplication {

	static {
		SpecklesView.DENSITY = 25; // more speckles for reference
	}
	
	private static boolean MOVEMENT_ENABLED = false;

	private NarjillosApplicationState state = new NarjillosApplicationState();
	private volatile boolean autoplay = false;
	
	@Override
	protected void startSupportThreads() {
		startAutoplayThread();
	}

	@Override
	protected StoppableThread createModelThread(final String[] arguments, final boolean[] isModelInitialized) {
		return new StoppableThread() {
			@Override
			public void run() {
				if (arguments.length != 1 || arguments[0].equals("-?") || arguments[0].equals("--help")) {
					System.out.println("This program needs either a *.germline file or the --random option.");
					System.exit(1);
				}

				List<DNA> genomes;
				if (arguments[0].equals("-r") || arguments[0].equals("--random")) {
					System.out.println("No *.germline file. Generating random DNAs...");
					genomes = randomGenomes();
				} else
					genomes = readGenomes(arguments[0]);

				setDish(new IsolationDish(genomes));
				getDish().moveToFirst();
				Narjillo firstNarjillo = getDish().getNarjillo();
				firstNarjillo.getBody().forcePosition(Vector.ZERO, 180);

				isModelInitialized[0] = true;

				while (!hasBeenAskedToStop()) {
					long startTime = System.currentTimeMillis();
					if (MOVEMENT_ENABLED)
						tick();
					waitFor(state.getSpeed().getTicksPeriod(), startTime);
				}
			}

			private List<DNA> randomGenomes() {
				List<DNA> result = new LinkedList<DNA>();
				RanGen ranGen = new RanGen((int) (Math.random() * 100000));
				for (int i = 0; i < 1000; i++)
					result.add(DNA.random(i, ranGen));
				return result;
			}

			/**
			 * Takes an ancestry file that contains genomes (one DNA document
			 * per line) and returns a list of matching phenotypes.
			 */
			private List<DNA> readGenomes(String genomesFileName) {
				try {
					BufferedReader reader = new BufferedReader(new FileReader(genomesFileName));

					List<DNA> result = new ArrayList<DNA>();
					String nextLine = reader.readLine();
					while (nextLine != null) {
						result.add(new DNA(1, nextLine.trim()));
						nextLine = reader.readLine();
					}
					reader.close();
					System.out.println("Loaded genomes from " + genomesFileName);
					return result;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	@Override
	protected StoppableThread createViewThread(final Group root) {
		return new StoppableThread() {
			private volatile boolean renderingFinished = false;

			private final MicroscopeView foregroundView = new MicroscopeView(getViewport());
			private final EnvirommentView ecosystemView = new EnvirommentView(getEcosystem(), getViewport(), state);
			private final StringView statusView = new StringView(40, state);

			public void run() {
				trackNarjillo();

				while (!hasBeenAskedToStop()) {
					long startTime = System.currentTimeMillis();
					renderingFinished = false;

					getTracker().tick();
					ecosystemView.tick();

					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							update(root);
							renderingFinished = true;
						}
					});

					waitFor(state.getFramesPeriod(), startTime);
					while (!renderingFinished && !hasBeenAskedToStop())
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
				}
			}

			private void update(final Group root) {
				root.getChildren().clear();
				root.getChildren().add(ecosystemView.toNode());
				root.getChildren().add(foregroundView.toNode());

				Node statusInfo = statusView.toNode(getDishStatistics());
				root.getChildren().add(statusInfo);
			}
		};
	}

	@Override
	protected String getName() {
		return "DNA Browser";
	}

	@Override
	protected void registerInteractionHandlers(final Scene scene) {
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(final KeyEvent keyEvent) {
				final int SKIP = 10;
				if (keyEvent.getCode() == KeyCode.LEFT && keyEvent.isControlDown())
					moveBack(SKIP);
				else if (keyEvent.getCode() == KeyCode.RIGHT && keyEvent.isControlDown())
					moveForward(SKIP);
				else if (keyEvent.getCode() == KeyCode.LEFT && keyEvent.isShiftDown())
					moveToFirst();
				else if (keyEvent.getCode() == KeyCode.RIGHT && keyEvent.isShiftDown())
					moveToLast();
				else if (keyEvent.getCode() == KeyCode.LEFT)
					moveBack(1);
				else if (keyEvent.getCode() == KeyCode.RIGHT)
					moveForward(1);
				else if (keyEvent.getCode() == KeyCode.DOWN)
					resetSpecimen();
				else if (keyEvent.getCode() == KeyCode.ENTER)
					getDish().rotateTarget();
				else if (keyEvent.getCode() == KeyCode.O || keyEvent.getCode() == KeyCode.P)
					state.toggleSpeed();
				else if (keyEvent.getCode() == KeyCode.A)
					MOVEMENT_ENABLED = !MOVEMENT_ENABLED;
				else if (keyEvent.getCode() == KeyCode.Z)
					Body.PHYSICS_ENABLED = !Body.PHYSICS_ENABLED;
				else if (keyEvent.getCode() == KeyCode.SPACE)
					autoplay = !autoplay;
			}
		});

		scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				Vector clickedPositionSC = Vector.cartesian(event.getSceneX(), event.getSceneY());
				Vector clickedPositionEC = getViewport().toEC(clickedPositionSC);

				if (event.getClickCount() == 3)
					copyDNAToClipboard(clickedPositionEC);
			}
		});
	}

	@Override
	protected synchronized IsolationDish getDish() {
		return (IsolationDish) super.getDish();
	}

	private void startAutoplayThread() {
		Thread autoplayThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					if (autoplay) {
						moveForward(1);
						sleep(100);
					} else
						sleep(100);
				}
			}

			private void sleep(int timeMillis) {
				try {
					Thread.sleep(timeMillis);
				} catch (InterruptedException e) {
				}
			}
		});
		autoplayThread.setDaemon(true);
		autoplayThread.start();
	}

	private void trackNarjillo() {
		getTracker().startTracking(getDish().getNarjillo());
	}

	private synchronized void resetSpecimen() {
		Narjillo from = getDish().getNarjillo();
		getDish().resetSpecimen();
		Narjillo to = getDish().getNarjillo();
		switchNarjillo(from, to);
	}

	private synchronized void moveBack(int skip) {
		Narjillo from = getDish().getNarjillo();
		getDish().moveBack(skip);
		Narjillo to = getDish().getNarjillo();
		switchNarjillo(from, to);
	}

	private synchronized void moveForward(int skip) {
		Narjillo from = getDish().getNarjillo();
		getDish().moveForward(skip);
		Narjillo to = getDish().getNarjillo();
		switchNarjillo(from, to);
	}

	private synchronized void moveToFirst() {
		Narjillo from = getDish().getNarjillo();
		getDish().moveToFirst();
		Narjillo to = getDish().getNarjillo();
		switchNarjillo(from, to);
	}

	private synchronized void moveToLast() {
		Narjillo from = getDish().getNarjillo();
		getDish().moveToLast();
		Narjillo to = getDish().getNarjillo();
		switchNarjillo(from, to);
	}

	private synchronized void switchNarjillo(Narjillo from, Narjillo to) {
		Vector centerOffset = to.getPosition().minus(to.getCenter());
		to.getBody().forcePosition(from.getCenter().plus(centerOffset), 180);
		trackNarjillo();
	}
}
