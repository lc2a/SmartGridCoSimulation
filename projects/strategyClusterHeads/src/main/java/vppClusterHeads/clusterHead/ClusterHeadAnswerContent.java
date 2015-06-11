/*
 * Copyright (c) 2011-2015, fortiss GmbH.
 * Licensed under the Apache License, Version 2.0.
 *
 * Use, modification and distribution are subject to the terms specified
 * in the accompanying license file LICENSE.txt located at the root directory
 * of this software distribution.
 */

package vppClusterHeads.clusterHead;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import akka.basicMessages.AnswerContent;

/**
 * DEMSAggregatorAnswerContent
 * 
 * @author bytschkow
 * 
 * created on 07.05.2014
 */
public class ClusterHeadAnswerContent implements AnswerContent  {
	
	final static DecimalFormatSymbols format = new DecimalFormatSymbols(Locale.ENGLISH);
	final static DecimalFormat df = new DecimalFormat("#0.00", format);
	
	public String name;
	
	public double total;
	public double scheduled;
	public double solar;
	public double water;
	public double wind;
	public double bioGas;
	public double bioMass;
	public double requestedPower;
	public double pF;
	public double nF;	
		
	public String toString(){		 
		return ("AggregatorAnswerContent={" +
                "totalConsumption: " + df.format(total) + 
                "  scheduledConsumption: " + df.format(scheduled) +
                "  solarPower: " + df.format(solar) +
                "  waterPower: " + df.format(water) +
                "  windPower: " + df.format(wind) +
                "  bioGasPower: " + df.format(bioGas) +
                "  bioMassPower: " + df.format(bioMass) +
                "  requestedPower: " + df.format(requestedPower) +      
                "  positiveFlexibility: " + df.format(pF) +      
                "  negativeFlexibility: " + df.format(nF) +      
				"}");
	}
}
