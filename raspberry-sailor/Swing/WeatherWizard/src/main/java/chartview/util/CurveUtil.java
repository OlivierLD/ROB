package chartview.util;


import calc.GeoPoint;
import chartview.util.grib.GRIBDataUtil;
import chartview.util.grib.GribHelper;

import java.util.ArrayList;
import java.util.List;

public class CurveUtil {
    public final static int PRMSL = 0;
    public final static int HGT500 = 1;
    public final static int AIRTEMP = 2;
    public final static int WAVES = 3;
    public final static int RAIN = 4;
    public final static int TWS = 5;
    public final static int SEATEMP = 6;

    public static List<GeoBump> getBumps(GribHelper.GribConditionData gribData, int type) {
        List<CurveUtil.GeoBump> result = new ArrayList<>();

        for (int h = 0; gribData.getGribPointData() != null && h < gribData.getGribPointData().length; h++) {
            for (int w = 0; w < gribData.getGribPointData()[h].length; w++) {
                try {
                    float value = 0,
                            value_1 = 0,
                            value_2 = 0,
                            value_3 = 0,
                            value_4 = 0,
                            value_5 = 0,
                            value_6 = 0,
                            value_7 = 0,
                            value_8 = 0;
                    if (type == PRMSL) {
                        value = gribData.getGribPointData()[h][w].getPrmsl();
                        value_1 = gribData.getGribPointData()[h - 1][w - 1].getPrmsl();
                        value_2 = gribData.getGribPointData()[h - 1][w].getPrmsl();
                        value_3 = gribData.getGribPointData()[h - 1][w + 1].getPrmsl();
                        value_4 = gribData.getGribPointData()[h][w - 1].getPrmsl();
                        value_5 = gribData.getGribPointData()[h][w + 1].getPrmsl();
                        value_6 = gribData.getGribPointData()[h + 1][w - 1].getPrmsl();
                        value_7 = gribData.getGribPointData()[h + 1][w].getPrmsl();
                        value_8 = gribData.getGribPointData()[h + 1][w + 1].getPrmsl();
                    } else if (type == HGT500) {
                        value = gribData.getGribPointData()[h][w].getHgt();
                        value_1 = gribData.getGribPointData()[h - 1][w - 1].getHgt();
                        value_2 = gribData.getGribPointData()[h - 1][w].getHgt();
                        value_3 = gribData.getGribPointData()[h - 1][w + 1].getHgt();
                        value_4 = gribData.getGribPointData()[h][w - 1].getHgt();
                        value_5 = gribData.getGribPointData()[h][w + 1].getHgt();
                        value_6 = gribData.getGribPointData()[h + 1][w - 1].getHgt();
                        value_7 = gribData.getGribPointData()[h + 1][w].getHgt();
                        value_8 = gribData.getGribPointData()[h + 1][w + 1].getHgt();
                    } else if (type == WAVES) {
                        value = gribData.getGribPointData()[h][w].getWHgt();
                        value_1 = gribData.getGribPointData()[h - 1][w - 1].getWHgt();
                        value_2 = gribData.getGribPointData()[h - 1][w].getWHgt();
                        value_3 = gribData.getGribPointData()[h - 1][w + 1].getWHgt();
                        value_4 = gribData.getGribPointData()[h][w - 1].getWHgt();
                        value_5 = gribData.getGribPointData()[h][w + 1].getWHgt();
                        value_6 = gribData.getGribPointData()[h + 1][w - 1].getWHgt();
                        value_7 = gribData.getGribPointData()[h + 1][w].getWHgt();
                        value_8 = gribData.getGribPointData()[h + 1][w + 1].getWHgt();
                    } else if (type == AIRTEMP) {
                        value = gribData.getGribPointData()[h][w].getAirtmp();
                        value_1 = gribData.getGribPointData()[h - 1][w - 1].getAirtmp();
                        value_2 = gribData.getGribPointData()[h - 1][w].getAirtmp();
                        value_3 = gribData.getGribPointData()[h - 1][w + 1].getAirtmp();
                        value_4 = gribData.getGribPointData()[h][w - 1].getAirtmp();
                        value_5 = gribData.getGribPointData()[h][w + 1].getAirtmp();
                        value_6 = gribData.getGribPointData()[h + 1][w - 1].getAirtmp();
                        value_7 = gribData.getGribPointData()[h + 1][w].getAirtmp();
                        value_8 = gribData.getGribPointData()[h + 1][w + 1].getAirtmp();
                    } else if (type == TWS) {
                        value = (int) Math.round(GRIBDataUtil.getGRIBWindSpeed(gribData.getGribPointData()[h][w].getU(),
                                gribData.getGribPointData()[h][w].getV()));
                        value_1 = (int) Math.round(GRIBDataUtil.getGRIBWindSpeed(gribData.getGribPointData()[h - 1][w - 1].getU(),
                                gribData.getGribPointData()[h - 1][w - 1].getV()));
                        value_2 = (int) Math.round(GRIBDataUtil.getGRIBWindSpeed(gribData.getGribPointData()[h - 1][w].getU(),
                                gribData.getGribPointData()[h - 1][w].getV()));
                        value_3 = (int) Math.round(GRIBDataUtil.getGRIBWindSpeed(gribData.getGribPointData()[h - 1][w + 1].getU(),
                                gribData.getGribPointData()[h - 1][w + 1].getV()));
                        value_4 = (int) Math.round(GRIBDataUtil.getGRIBWindSpeed(gribData.getGribPointData()[h][w - 1].getU(),
                                gribData.getGribPointData()[h][w - 1].getV()));
                        value_5 = (int) Math.round(GRIBDataUtil.getGRIBWindSpeed(gribData.getGribPointData()[h][w + 1].getU(),
                                gribData.getGribPointData()[h][w + 1].getV()));
                        value_6 = (int) Math.round(GRIBDataUtil.getGRIBWindSpeed(gribData.getGribPointData()[h + 1][w - 1].getU(),
                                gribData.getGribPointData()[h + 1][w - 1].getV()));
                        value_7 = (int) Math.round(GRIBDataUtil.getGRIBWindSpeed(gribData.getGribPointData()[h + 1][w].getU(),
                                gribData.getGribPointData()[h + 1][w].getV()));
                        value_8 = (int) Math.round(GRIBDataUtil.getGRIBWindSpeed(gribData.getGribPointData()[h + 1][w + 1].getU(),
                                gribData.getGribPointData()[h + 1][w + 1].getV()));
                    }

                    int extr = 0;
                    if (value < value_1 &&
                            value < value_2 &&
                            value < value_3 &&
                            value < value_4 &&
                            value < value_5 &&
                            value < value_6 &&
                            value < value_7 &&
                            value < value_8) {
                      extr = -1;
                    } else if (value > value_1 &&
                            value > value_2 &&
                            value > value_3 &&
                            value > value_4 &&
                            value > value_5 &&
                            value > value_6 &&
                            value > value_7 &&
                            value > value_8) {
                      extr = 1;
                    }
                    if (extr != 0) {
                        double lat = gribData.getGribPointData()[h][w].getLat();
                        double lng = gribData.getGribPointData()[h][w].getLng();
                        result.add(new GeoBump(new GeoPoint(lat, lng), (extr == 1 ? GeoBump.H : GeoBump.L)));
                    }
                } catch (Exception ex) {
                    continue;
                }
            }
        }
        return result;
    }

    public static class GeoBump {
        private GeoPoint pos = null;
        private int type = -1;
        public final static int L = 0;
        public final static int H = 1;

        public GeoBump(GeoPoint pt, int type) {
            this.pos = pt;
            this.type = type;
        }

        public GeoPoint getGeoPoint() {
            return pos;
        }

        public int getType() {
            return type;
        }
    }
}
