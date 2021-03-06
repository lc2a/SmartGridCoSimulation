/*
 * Copyright (c) 2011-2015, fortiss GmbH.
 * Licensed under the Apache License, Version 2.0.
 *
 * Use, modification and distribution are subject to the terms specified
 * in the accompanying license file LICENSE.txt located at the root directory
 * of this software distribution.
 */

package basicExample;

import java.util.HashSet;

import akka.basicActors.ActorOptions;
import akka.basicActors.LoggingMode;
import basicExample.aggregator.AggregatorModel;
import basicExample.child.ChildModel;

/**
 * 
 * The ActorFactory initializes the Actors with the corresponding BehaviorModels
 * 
 * @author bytschkow
 * 
 */
public abstract class ActorFactory {

	public static ActorOptions createChild(){
		ActorOptions result = new ActorOptions(LoggingMode.MINIMAL,							
				new HashSet<String>(),new HashSet<String>(),new HashSet<String>(),
				new ChildModel());		
		return result;
	}
	
	public static ActorOptions createAggregator(){
		ActorOptions result = new ActorOptions(LoggingMode.MINIMAL,							
				new HashSet<String>(),new HashSet<String>(),new HashSet<String>(),
				new AggregatorModel());		
		return result;		
	}
}
