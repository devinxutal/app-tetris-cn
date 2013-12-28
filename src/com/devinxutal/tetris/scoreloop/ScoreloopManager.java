/*
 * In derogation of the Scoreloop SDK - License Agreement concluded between
 * Licensor and Licensee, as defined therein, the following conditions shall
 * apply for the source code contained below, whereas apart from that the
 * Scoreloop SDK - License Agreement shall remain unaffected.
 * 
 * Copyright: Scoreloop AG, Germany (Licensor)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.devinxutal.tetris.scoreloop;

import android.content.Context;

import com.devinxutal.tetris.cfg.Constants;
import com.scoreloop.client.android.core.controller.ScoreController;
import com.scoreloop.client.android.core.controller.ScoresController;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.controller.UsersController;
import com.scoreloop.client.android.core.model.Client;

public class ScoreloopManager {

	private static Client client;

	
	public static void init(final Context context) {
		if (client == null) {
			client = new Client(context, Constants.SCORELOOP_SECRET, null);
		}
	}
	
	private static ScoreloopManager manager;
	public static ScoreloopManager get(){
		if(manager == null){
			manager = new ScoreloopManager();
		}
		return manager;
	}
	
	
	private ScoreController scoreController;
	private ScoresController scoresController;
	private UserController userController;
	private UsersController usersController;
	
	private ScoreloopManager(){
		
	}
	
	
}
