package com.devinxutal.tetris.model;

import java.io.Serializable;
import java.util.List;

import com.devinxutal.tetris.model.Block.BlockType;

public class SavablePlayground implements Serializable {
	public int[][] playground;
	public SavableBlock activeBlock;
	public int offsetX = 0;
	public int offsetY = 0;
	public List<SavableBlock> blockQueue;
	public SavableBlock holdBlock;
	public boolean holdUsed = false;
	public ScoreAndLevel scoreLevel;
	public boolean finished = false;

	public class SavableBlock implements Serializable {
		public BlockType blockType;
		public int current;

	}

	public SavableBlock create(Block b) {
		if (b == null) {
			return null;
		}
		SavableBlock sb = new SavableBlock();
		sb.blockType = b.getBlockType();
		sb.current = b.getCurrent();
		return sb;
	}

}
