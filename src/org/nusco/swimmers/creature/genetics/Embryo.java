package org.nusco.swimmers.creature.genetics;

import java.util.LinkedList;

import org.nusco.swimmers.creature.Swimmer;
import org.nusco.swimmers.creature.body.Head;
import org.nusco.swimmers.creature.body.Organ;

public class Embryo {

	private DNA genes;

	public Embryo(DNA genes) {
		this.genes = genes;
	}
	
	public Swimmer develop() {
		OrganParser parser = new OrganParser(genes);
		
		Head head = createHeadSystem(parser);
		Organ neck = head.getChildren().get(0);
		Organ[] firstLevelChildren = createTwinOrgans(parser, neck);
		
		LinkedList<Organ> secondLevelChildren = new LinkedList<>();
		for (Organ organ : firstLevelChildren) {
			Organ[] twinSecondLevelChildren = createTwinOrgans(parser, organ);
			secondLevelChildren.add(twinSecondLevelChildren[0]);
			secondLevelChildren.add(twinSecondLevelChildren[1]);
		}
		
		createTwinOrgans(parser, secondLevelChildren.getFirst());
		createTwinOrgans(parser, secondLevelChildren.getLast());

		return new Swimmer(head);
	}

	private Head createHeadSystem(OrganParser parser) {
		return new OrganBuilder(parser.nextPart()).buildHeadSystem();
	}

	private Organ[] createTwinOrgans(OrganParser parser, Organ parent) {
		return new TwinOrgansBuilder(parser.nextPart(), parser.nextPart()).buildSegments(parent);
	}
}