package com.xayup.launchpadplus;

import android.os.*;
import android.app.*;
import android.widget.*;
import java.util.*;

public class ledTask extends AsyncTask<String, Integer, String>
{
		int corcode = 0;
		int padId = 0;
		Activity context;
		int[] colorInt;
		String pad;
		int[] chaincode;
		Map<String, List<String>> keyledsMap;
		public ledTask(Activity context, Map<String, List<String>> keyledsMap, int[] colorInt, String pad, int[] chaincode)
		{
				this.context = context;
				this.keyledsMap = keyledsMap;
				this.colorInt = colorInt;
				this.pad = pad;
				this.chaincode = chaincode;
		}

		@Override
		protected String doInBackground(String[] p1)
		{
				if (keyledsMap.get(pad) != null)
				{
						String line;
						long time = System.currentTimeMillis();
						for (int i = 0; i < keyledsMap.get(pad).size(); i++)
						{
								line = keyledsMap.get(pad).get(i);
								while (System.currentTimeMillis() < time)
								{}							
								switch (line.substring(0, 1))
								{
										case "o":
												if (line.contains("mc"))
												{
														padId = chaincode[Integer.parseInt(line.substring(3, line.indexOf("a")))];
												}
												else
												{
														padId = Integer.parseInt(line.substring(1, 3));
												}
												corcode = Integer.parseInt(line.substring(line.indexOf("a") + 1));
												break;
										case "f":
												corcode = 0;
												padId = Integer.parseInt(line.substring(1));
												break;
										case "d":
												time += Integer.parseInt(line.substring(1));
												break;
								}
								return null;
								//line = null;
								
						}

				}
				return null;
		}
		@Override
		protected void onPostExecute(String result)
		{
				ImageView pad = context.findViewById(padId).findViewById(R.id.led);
				pad.setBackgroundColor(colorInt[corcode]);
		}




}
