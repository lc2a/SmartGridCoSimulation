package linprog.components;

public class ThermalStorage extends Storage {
	
	public final double efficiency;
	
	public ThermalStorage(String name, double qdot_max_in, double qdot_max_out, double capacity, double efficiency, int port) {
		super(name, qdot_max_in, qdot_max_out, capacity, port);
		if(efficiency < 0.0 || efficiency > 1.0) {
			//TODO throw exception
		}
		this.efficiency = efficiency;
	}
	
	@Override
	public void makeDecision() {
		super.makeDecision();
		for (int i = 0; i < n; i++) {
			specificationToSend.cost[i] = 0.0;
			for (int j = 0; j < 2*n; j++) {
				specificationToSend.couplingMatrix[i][j] = 0.0;
			}
			specificationToSend.couplingMatrix[i][i] = 1.0/efficiency;
			specificationToSend.couplingMatrix[i][n+i] = -efficiency;
			specificationToSend.vector[i] = 0.0;
		}
		for (int i = n; i < 2*n; i++) {
			specificationToSend.cost[i] = 0.0;
			specificationToSend.vector[i] = capacity/stepSize;
		}
		
		display.update(gson.toJson(specificationToSend));
	}

}
