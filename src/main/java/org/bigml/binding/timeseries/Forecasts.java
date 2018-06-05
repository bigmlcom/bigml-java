package org.bigml.binding.timeseries;

import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
// import java.util.Collections;
// import java.util.List;
// import java.util.Arrays;
// import java.util.Map;
// import java.util.HashMap;
// import java.util.Comparator;

import java.lang.reflect.*;

/**
 * Auxiliary module to store the functions to compute time-series forecasts
 following the formulae in
 https://www.otexts.org/sites/default/files/fpp/images/Table7-8.png
 as explained in https://www.otexts.org/fpp/7/6
**/

public class Forecasts {

    private Double l;
    private Double b;
    private JSONArray s;
    private Double phi;
    private JSONObject submodel;

    private static int[] range(int start, long horizon) {
        int length = (int)horizon;
        int[] range = new int[length];
        for (int i = start; i < length; i++) {
            range[i - start] = i;
        }
        return range;
    }
   
    private static final Double seasonContribution(JSONArray sList, Number step) {
        if (sList.size() > 0) {
            Integer period = sList.size();
            Integer index = Math.abs(1 - period + step.intValue() % period);
            return ((Number)sList.get(index)).doubleValue();
        } else
            return 0.0;
    }

    private static final Float calcPoint(Double op1, Double op2, String seasonality) {
        Double result = 0.0;
        if (seasonality.equals("A")) {
            result = op1 + op2;
        } else if (seasonality.equals("M")) {
            result = op1 * op2;
        } else if (seasonality.equals("N")) {
            result = op1;
        } else {
            assert(false);
        }
        return (Float)((Number)(Math.round(result * 100000) / 100000.0)).floatValue();
    }

    public Forecasts(JSONObject submodel) throws Exception {

        this.submodel = submodel;

        JSONObject finalState = Utils.getFromJSONOr(submodel, "final_state");
        this.l = Utils.getFromJSONOr(finalState, "l", 0.0);
        this.b = Utils.getFromJSONOr(finalState, "b", 0.0);
        this.phi = Utils.getFromJSONOr(finalState, "phi", 0.0);
        this.s = Utils.getFromJSONOr(finalState, "s", new JSONArray());
    }
    
    private final ArrayList<Number> trivialForecast(Long horizon, 
                                                    String seasonality) 
        throws Throwable {

        ArrayList<Number> points = new ArrayList<Number>();
        JSONArray submodelPoints = (JSONArray)Utils.getJSONObject(submodel, "value", null);
        Integer period = submodelPoints != null ? submodelPoints.size() : 0;

/*        if (submodel != null) {
            throw new Exception("TRIVIASL: " + submodel.toString());
        }

        if (submodel == null) {
            throw new Exception("TRIVIAL 1: " + submodel.toString());
        }
*/
        if (period > 1) {
            for (Integer h: range(0, horizon)) {
                points.add((Number)submodelPoints.get(h % period));
            }
        } else {
            for (Integer h: range(0, horizon)) {
                points.add((Number)submodelPoints.get(0));
            }
        }
        return points;
    }

    private final ArrayList<Number> naiveForecast(Long horizon, 
                                                  String seasonality) 
    throws Throwable {

        return trivialForecast(horizon, seasonality);
    }

    private final ArrayList<Number> meanForecast(Long horizon, 
                                                 String seasonality) 
    throws Throwable {

        return trivialForecast(horizon, seasonality);
    }

    private final ArrayList<Number> driftForecast(Long horizon, 
                                                  String seasonality) 
    throws Throwable {

        ArrayList<Number> points = new ArrayList<Number>();
        for (Integer h: range(0, horizon)) {
            points.add((Number)(Utils.getFromJSONOr(submodel, "value", 0.0).doubleValue() +
                                Utils.getFromJSONOr(submodel, "slope", 0.0).doubleValue() * (h + 1)));
        }
        return points;
    }
   
    private final ArrayList<Number> NForecast(Long horizon,
                                              String seasonality)
        throws Throwable {
        
        ArrayList<Number> points = new ArrayList<Number>();
        for (Integer h: range(0, horizon)) {
            Double sc = seasonContribution(s, h);
            points.add(calcPoint(l, sc, seasonality));
        }
        return points;
    }

    private final ArrayList<Number> AForecast(Long horizon,
                                              String seasonality)
        throws Throwable {

        ArrayList<Number> points = new ArrayList<Number>();
        for (Integer h: range(0, horizon)) {
            Double sc = seasonContribution(s, h);
            Double k = b * (h + 1);
            points.add(calcPoint(l + k, sc, seasonality));
        }
        return points;
    }

    private final ArrayList<Number> Ad_Forecast(Long horizon,
                                                String seasonality)
        throws Throwable {

        Double phi_ = phi;
        ArrayList<Number> points = new ArrayList<Number>();
        for (Integer h: range(0, horizon)) {
            Double sc = seasonContribution(s, h);
            Double k = b * phi_;
            points.add(calcPoint(l * k, sc, seasonality));
            phi_ = phi+ + Math.pow(phi_, h + 2);
        }
        return points;
    }

    private final ArrayList<Number> MForecast(Long horizon,
                                              String seasonality)
        throws Throwable {

        ArrayList<Number> points = new ArrayList<Number>();
        for (Integer h: range(0, horizon)) {
            Double sc = seasonContribution(s, h);
            Double k = Math.pow(b, h + 1);
            points.add(calcPoint(l * k, sc, seasonality));
        }
        return points;
    }

    private final ArrayList<Number> MdForecast(Long horizon,
                                               String seasonality)
        throws Throwable {

        Double phi_ = phi;
        ArrayList<Number> points = new ArrayList<Number>();
        for (Integer h: range(0, horizon)) {
            Double sc = seasonContribution(s, h);
            Double k = Math.pow(b, phi_);
            points.add(calcPoint(l * k, sc, seasonality));
            phi_ = phi+ + Math.pow(phi_, h + 2);
        }
        return points;
    }

    public final ArrayList<Number> forecast(String trend,
                                            Long horizon, 
                                            String seasonality) 
        throws Throwable {

        String methodName = trend + "Forecast";
        Method method;
//        try {
            method = this.getClass().getDeclaredMethod(methodName, Long.class, String.class);
            method.setAccessible(true);
//        } 
/*        catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        }
*/

//        try {
            return (ArrayList<Number>)method.invoke(this, horizon, seasonality);
/*        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
*/        

        /*        
        if (trend.equals("trivial") ||
            trend.equals("naive")  ||
            trend.equals("mean")) {
            return trivialForecast(horizon);
        } else if (trend.equals("drift")) {
            return driftForecast(horizon);
        } else if (trend.equals("N")) {
            return N_Forecast(horizon, seasonality);
        } else if (trend.equals("M")) {
            return M_Forecast(horizon, seasonality);
        } else if (trend.equals("M")) {
            return M_Forecast(horizon, seasonality);
        }

        throw new Exception("Unkwnown Trend: " + trend);
*/    }
 
   
}

