package linprog;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorSystem;
import linprog.Simulation;
import simulation.SimulationStarter;
import topology.ActorTopology;

public class Simulation {

	private ActorTopology topology;

	public static final int N_STEPS = 70;
	public static final int N_DAYS = 7;
	
	public static final int TIMESTEPS_PER_ITERATION = N_STEPS; //equals N_STEPS for case of overall optimization, smaller for reactive case
	private static final int NR_OF_ITERATIONS = 1;
	
	//For the moment, the following 3 parameters need to stay as defined to match data input
//	public static LocalDateTime startTime = LocalDateTime.of(2014,7,1,11,0);
//	public static LocalDateTime endTime = LocalDateTime.of(2014,7,1,12,0);
//	public static Duration timeInterval = Duration.ofMinutes(15);
	
	
	private void run() {
		topology = Topology.createTopology();
		SimulationStarter.saveGridTopologyPlot(topology);   
		ActorSystem actorSystem = SimulationStarter.initialiseActorSystem(topology);
        SimulationStarter.startSimulation(actorSystem, 0, NR_OF_ITERATIONS);
		
	}
	
	public static void main(String[] args){
		new Simulation().run();
	}
	
	public static double stepLength(TimeUnit timeUnit) {
		double stepLengthInSeconds = 24*3600*N_DAYS/N_STEPS;
		switch (timeUnit) {
		case SECONDS:
			return stepLengthInSeconds;
		case MINUTES:
			return stepLengthInSeconds/60;
		case HOURS:
			return stepLengthInSeconds/3600;
		default:
			throw new RuntimeException("Timeunit " + timeUnit + " not implemented.");
		}
	}

}