package ethereum.components;

import java.math.BigInteger;

import akka.systemActors.GlobalTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.web3j.protocol.core.methods.response.TransactionReceipt;

import ethereum.Simulation;
import ethereum.helper.ConsumptionProfiles;
import ethereum.helper.Market;
import ethereum.helper.SolarRadiation;
import ethereum.helper.UnitHelper;

public class Building2 extends Building {
	
//	private BigInteger gasboilerPower = BigInteger.valueOf(40000); //W
//	private BigInteger gasboilerPrice;
	
	private BigInteger currentElectricityConsumption = BigInteger.ZERO;
	private BigInteger currentHeatConsumption = BigInteger.ZERO;
	private BigInteger currentPVProduction = BigInteger.ZERO;
	private double pvEfficiency = .2;
	private double pvArea = 8.;
	private BigInteger stateOfCharge = BigInteger.ZERO;
	private BigInteger maxInOut; //Ws
	private BigInteger capacity; //Ws
	private double storageEfficiency = 1.;
	private BigInteger heatPumpPower = BigInteger.valueOf(20000);
	
	public Building2(
			String name,
			int rpcport,
			String privateKey,
			ConsumptionProfiles consumptionProfiles,
			int consumerIndex
	) {
		super(name, rpcport, privateKey, consumptionProfiles, consumerIndex);
		maxInOut = BigInteger.valueOf((long) (5000 * storageEfficiency))
				.multiply(Simulation.TIMESTEP_DURATION_IN_SECONDS);
//		gasboilerPrice = UnitConverter.getCentsPerWsFromCents(5.2);
		capacity = UnitHelper.getWSfromKWH(100);
	}

	@Override
	public void makeDecision() {
		super.makeDecision();

		BigInteger heatPumpMaxProduction = Simulation.TIMESTEP_DURATION_IN_SECONDS.multiply(heatPumpPower);
		
		BigInteger heatToProduce = currentHeatConsumption.add(soldHeat);
		heatToProduce = heatToProduce.subtract(boughtHeat); //never demand more heat than consumption+soldHeat --> always positive or zero
		BigInteger fromStorage = heatToProduce.min(stateOfCharge).min(maxInOut);
		heatToProduce = heatToProduce.subtract(fromStorage);
		stateOfCharge = stateOfCharge.subtract(fromStorage);
		
		BigInteger electricityToProduce = currentElectricityConsumption.add(soldElectricity).subtract(boughtElectricity);

		BigInteger heatProduction = heatPumpMaxProduction.min(heatToProduce);
		
		if(isGreaterZero(heatToProduce)) {
			electricityToProduce = electricityToProduce.add(heatToElectricity(heatProduction)); // add electricity needed to produce necessary heating power
			heatToProduce = heatToProduce.subtract(heatProduction);
		}
		
		BigInteger excessElectricity = BigInteger.ZERO.max(
				boughtElectricity.
				add(currentPVProduction).
				subtract(electricityToProduce)
			);
		electricityToProduce = electricityToProduce.subtract(currentPVProduction).max(BigInteger.ZERO);
		BigInteger maxHeatPumpProduction = electricityToHeat(excessElectricity).min(heatPumpMaxProduction);
		BigInteger toStorage = capacity.subtract(stateOfCharge).min(maxInOut);
		BigInteger charge = toStorage.min(maxHeatPumpProduction);
		if(isGreaterZero(charge)) {
			stateOfCharge = stateOfCharge.add(charge);
			excessElectricity = excessElectricity.subtract(
					heatToElectricity(charge));
		}	
		if(isGreaterZero(heatToProduce)) { //TODO heatToProduce
			System.out.println("[" + name + "] Lacking heat : " + UnitHelper.printAmount(heatToProduce));
			timestepInfo.heatWithdrawal = heatToProduce;
		}

		if(isGreaterZero(excessElectricity)) {
			System.out.println("[" + name + "] Excess electricity: " + UnitHelper.printAmount(excessElectricity));
			timestepInfo.electricityFeedIn = excessElectricity;
		}
		if(isGreaterZero(electricityToProduce)) {
			System.out.println("[" + name + "] Lacking electricity : " + UnitHelper.printAmount(electricityToProduce));
			timestepInfo.electricityWithdrawal = electricityToProduce;
		}
		
		BigInteger nextHeatConsumption = consumptionProfiles.getHeatConsumption(
				consumerIndex,
				GlobalTime.currentTimeStep + 1
		);
		
		BigInteger nextPVProduction = 
				BigInteger.valueOf(
						(long) (SolarRadiation.getRadiation(GlobalTime.currentTimeStep + 1)
								* pvArea
								* pvEfficiency)
					);
		
		BigInteger nextElectricityConsumption = consumptionProfiles.getElectricityConsumption(
				consumerIndex,
				GlobalTime.currentTimeStep + 1
		);
		
		System.out.println("[" + name + "] Expected heat consumption for next step: " + UnitHelper.printAmount(nextHeatConsumption));
		System.out.println("[" + name + "] Expected electricity consumption for next step: " + UnitHelper.printAmount(nextElectricityConsumption));
		System.out.println("[" + name + "] Expected PV production for next step: " + UnitHelper.printAmount(nextPVProduction));

		ArrayList<BigInteger> heatDemandPrices = new ArrayList<BigInteger>();
		ArrayList<BigInteger> heatDemandAmounts = new ArrayList<BigInteger>();
		ArrayList<BigInteger> heatOfferPrices = new ArrayList<BigInteger>();
		ArrayList<BigInteger> heatOfferAmounts = new ArrayList<BigInteger>();
		
		heatDemandAmounts.add(toStorage);
		heatDemandAmounts.add(nextHeatConsumption);
		BigInteger heatPrice1 = findUniqueDemandPrice(electricityToHeat(UnitHelper.getCentsPerWsFromCents(Simulation.ELECTRICITY_MIN_PRICE)), Market.HEAT);
		BigInteger heatPrice2 = findUniqueDemandPrice(electricityToHeat(UnitHelper.getCentsPerWsFromCents(Simulation.ELECTRICITY_MAX_PRICE)), Market.HEAT);
		heatDemandPrices.add(heatPrice1);
		heatDemandPrices.add(heatPrice2);
		logDemand(toStorage, UnitHelper.getCentsFromCentUnits(heatPrice1), Market.HEAT);
		logDemand(nextHeatConsumption, UnitHelper.getCentsFromCentUnits(heatPrice2), Market.HEAT);
		postDemand(heatDemandPrices, heatDemandAmounts, Market.HEAT);

		BigInteger heatToOffer = capacity.add(nextHeatConsumption).add(stateOfCharge).min(maxInOut);
		if(isGreaterZero(heatToOffer)) {
			for(int i = 0; i < heatToOffer.intValue() / 1000000; i++) {
				heatOfferPrices.add(heatPrice2);		
				heatOfferAmounts.add(BigInteger.valueOf(1000000));
			}
			heatOfferPrices.add(heatPrice2);
			heatOfferAmounts.add(heatToOffer.mod(BigInteger.valueOf(1000000)));
			logOffer(heatToOffer, UnitHelper.getCentsFromCentUnits(heatPrice2), Market.HEAT);
			postOffer(heatOfferPrices, heatOfferAmounts, Market.HEAT);
		}

		ArrayList<BigInteger> electricityDemandPrices = new ArrayList<BigInteger>();
		ArrayList<BigInteger> electricityDemandAmounts = new ArrayList<BigInteger>();
		ArrayList<BigInteger> electricityOfferPrices = new ArrayList<BigInteger>();
		ArrayList<BigInteger> electricityOfferAmounts = new ArrayList<BigInteger>();
		electricityToProduce = nextElectricityConsumption.subtract(nextPVProduction).max(BigInteger.ZERO);
		excessElectricity = nextPVProduction.subtract(nextElectricityConsumption).max(BigInteger.ZERO);
		if(isGreaterZero(electricityToProduce)) {
			electricityDemandPrices.add(UnitHelper.getCentsPerWsFromCents(Simulation.ELECTRICITY_MAX_PRICE));
			electricityDemandAmounts.add(electricityToProduce);
			logDemand(electricityToProduce, Simulation.ELECTRICITY_MAX_PRICE, Market.ELECTRICITY);
			postDemand(electricityDemandPrices, electricityDemandAmounts, Market.ELECTRICITY);
		}
		if(isGreaterZero(excessElectricity)) {
			for(int i = 0; i < excessElectricity.intValue() / 1000000; i++) {
				electricityOfferPrices.add(UnitHelper.getCentsPerWsFromCents(Simulation.ELECTRICITY_MIN_PRICE));
				electricityOfferAmounts.add(BigInteger.valueOf(1000000));
			}
			electricityOfferPrices.add(UnitHelper.getCentsPerWsFromCents(Simulation.ELECTRICITY_MIN_PRICE));
			electricityOfferAmounts.add(excessElectricity.mod(BigInteger.valueOf(1000000)));

			logOffer(excessElectricity, Simulation.ELECTRICITY_MIN_PRICE, Market.ELECTRICITY);
			postOffer(electricityOfferPrices, electricityOfferAmounts, Market.ELECTRICITY);
		}

		currentElectricityConsumption = nextElectricityConsumption;
		currentHeatConsumption = nextHeatConsumption;
		currentPVProduction = nextPVProduction;
	}

	private BigInteger electricityToHeat(BigInteger centsPerWs) {
		return centsPerWs.multiply(BigInteger.TEN).divide(BigInteger.valueOf(38));
	}

	private BigInteger heatToElectricity(BigInteger centsPerWs) {
		return centsPerWs.multiply(BigInteger.valueOf(38)).divide(BigInteger.TEN);
	}

}