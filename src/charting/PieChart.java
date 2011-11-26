/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package charting;

import java.util.ArrayList;
import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.renderer.DefaultRenderer;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

/**
 * Return/Collect pie chart.
 */
public class PieChart extends AbstractChart {
  /**
   * Returns the chart name.
   * 
   * @return the chart name
   */
  public String getName() {
    return "Pie chart";
  }

  /**
   * Returns the chart description.
   * 
   * @return the chart description
   */
  public String getDesc() {
    return "Pie Chart to visualize money";
  }

  /**
   * Creates the chart based on the data being passed into param2
   * 
   * @param context the context
   * @param p array containing information about each section of the pie
   * @return the built intent
   */
  public Intent execute(Context context, PersonTotal[] p, String title) {
    double[] values = new double[p.length];
    String[] names = new String[p.length];
    //Initialize data
    for(int i = 0; i < p.length; i++)
    {
    	values[i] = p[i].total;
    	names[i] = p[i].name;
    }
    ArrayList<Integer> colors = new ArrayList<Integer>();
    int[] colorsArray;
    int count = 0;
    int[] tempColors = {Color.RED, Color.GREEN, Color.BLUE, Color.GRAY};
    Random random = new Random();
    //Generate random colors for each section of the pie chart
    while(count < values.length)
    {
    	int color = generateRandomColor(tempColors[random.nextInt(tempColors.length)]);
    	if(!colors.contains(color))
    	{
    		colors.add(color);
    		count++;
    	}
    }
    colorsArray = new int[colors.size()];
    for(int i = 0; i < colors.size(); i++)
    {
    	colorsArray[i] = colors.get(i);
    }
    DefaultRenderer renderer = buildCategoryRenderer(colorsArray);
    renderer.setZoomButtonsVisible(true);
    renderer.setZoomEnabled(true);
    renderer.setChartTitleTextSize(30);
    renderer.setScale((float) (0.75));
    return ChartFactory.getPieChartIntent(context, buildCategoryDataset(title, values, names),
        renderer, title);
  }
  
  public int generateRandomColor(int mix) {
	    Random random = new Random();
	    int red = random.nextInt(256);
	    int green = random.nextInt(256);
	    int blue = random.nextInt(256);
	    
	    // mix the color
        red = (red + Color.red(mix)) / 2;
        green = (green + Color.green(mix)) / 2;
        blue = (blue + Color.blue(mix)) / 2;

	    int c = Color.rgb(red, green, blue);
	    return c;
	}
}
