/*
 * Copyright (c) 2011-2015, fortiss GmbH.
 * Licensed under the Apache License, Version 2.0.
 *
 * Use, modification and distribution are subject to the terms specified
 * in the accompanying license file LICENSE.txt located at the root directory
 * of this software distribution.
 */

package vppClusterHeads.wind;

import java.util.LinkedList;

import helper.Swmcsv;
import vppClusterHeads.genericStuff.GenericAnswerContent;
import vppClusterHeads.genericStuff.VPPPlantType;
import akka.advancedMessages.ErrorAnswerContent;
import akka.basicMessages.AnswerContent;
import akka.basicMessages.RequestContent;
import akka.systemActors.GlobalTime;
import behavior.BehaviorModel;

/**
 * 
 * This is a specific behavior
 * 
 * @author bytschkow
 *
 */

public class WindBehaviorModel extends BehaviorModel {

	final VPPPlantType type = VPPPlantType.WIND;
	public double installedPower;

	// Answer Content
    public double actualPower;
    public double forecastPower;    
    public GenericAnswerContent answerContentToSend = new GenericAnswerContent();
    
    /*
     * Constructor
     */    
    public WindBehaviorModel(double installedPower) {
        this.installedPower = installedPower;
		answerContentToSend.plantType = type;
		answerContentToSend.installedPower = this.installedPower;
    }
	
	@Override
	public void handleRequest() {
	}

	public void makeDecision() {
		
		actualPower = installedPower*Swmcsv.getSWMProfileWind(GlobalTime.currentTime) * 10;
		forecastPower = installedPower*Swmcsv.getSWMProfileWind(GlobalTime.nextTime) * 10;
		
    	answerContentToSend.currentPower = actualPower;
    	answerContentToSend.scheduledProduction = actualPower;
    	answerContentToSend.forecastPower = forecastPower;
	}

	public AnswerContent returnAnswerContentToSend() {
		return answerContentToSend;
	}

	public RequestContent returnRequestContentToSend() {
		return null;
	}

	@Override
	public void handleError(LinkedList<ErrorAnswerContent> errors) {}
}