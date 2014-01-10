package com.trf.theegg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Game_end extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();   //从Game_ing得到分尸的值并显示
	}

}
