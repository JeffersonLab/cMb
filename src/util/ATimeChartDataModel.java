package util;

import com.cosylab.gui.components.spikechart.*;

/**
 * <font size = 1 >JSA: Thomas Jefferson National Accelerator Facility<br>
 * This software was developed under a United States Government license,<br>
 * described in the NOTICE file included as part of this distribution.<br>
 * Copyright (c), Aug 19, 2010 <br></font>
 * </p>
 *
 * @author Vardan Gyurjyan
 * @version 1.3
 */

public class ATimeChartDataModel extends AbstractDataModel {
        private PointCollection points= new PointCollection();
        private PointCollector collector = new PointCollector();

        /**
         * RandomTrendDataModel constructor comment.
         */
        public ATimeChartDataModel() {
            super();
        }
        /**
         * RandomTrendDataModel constructor comment.
         * @param name of the data channel
         */
        public ATimeChartDataModel(String name){
            super();
            super.name=name;
        }
        public PointIterator getPointIterator() {
            return this;
        }

        public boolean hasNext() {
            return points.size()>0;
        }
        public synchronized void generatePoint(double data) {

            points.add(collector.newPoint(System.currentTimeMillis()/1000.0, data));
        }
        public synchronized Point next() {
            collector.recyclePoint(point);
            point= points.removeFirst();
            return point;
        }
        /**
         * @see com.cosylab.gui.components.spikechart.ChartDataSource#getPointCount()
         */
        public int getPointCount() {
            return points.size();
        }
}

