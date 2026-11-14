/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.bengeranimation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BengerEffectPlan {
    public static final int BUDGET = 64;
    public static final String[] ROLES = new String[]{"pack_howl", "pack_pounce", "wide_sweep", "fear_roar", "stun_bark", "slam_windup", "slam_air", "slam_land", "clamp_lock", "rush_charge", "milk_recover"};
    private static final float[] AMBER = new float[]{1.0f, 0.5f, 0.06f};
    private static final float[] WHITE = new float[]{0.95f, 0.93f, 0.83f};
    private static final float[] DANGER = new float[]{1.0f, 0.18f, 0.08f};
    private static final float[] EARTH = new float[]{0.7f, 0.45f, 0.22f};
    private static final float[] MINT = new float[]{0.14f, 0.85f, 0.55f};
    private static final Map<String, List<List<Point>>> FRAMES = BengerEffectPlan.buildFrames();

    private static Map<String, List<List<Point>>> buildFrames() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (String string : ROLES) {
            ArrayList<List<Point>> arrayList = new ArrayList<List<Point>>();
            for (int i = 0; i <= BengerEffectPlan.duration(string); ++i) {
                arrayList.add(List.copyOf(BengerEffectPlan.buildFrame(string, i)));
            }
            linkedHashMap.put(string, List.copyOf(arrayList));
        }
        return Map.copyOf(linkedHashMap);
    }

    public static List<Point> frame(String string, int n) {
        List<List<Point>> list = FRAMES.get(string);
        return list == null || n < 0 || n >= list.size() ? List.of() : list.get(n);
    }

    public static List<Point> emission(String string, int n, int n2, int n3, int n4, int n5, int n6) {
        Object object;
        List<Point> list = n2 > 0 || n6 == 1 && n2 == 0 ? BengerEffectPlan.frame("clamp_lock", 60 - n2) : List.of();
        int n7 = switch (string) {
            case "pack_pounce" -> 14 - n;
            case "wide_sweep" -> 10 - n;
            case "pack_howl", "fear_roar", "stun_bark", "milk_recover" -> 20 - n;
            case "rush_charge" -> 16 - n3;
            case "slam_windup", "slam_air" -> 8 - n4;
            default -> n5;
        };
        Object object2 = object = string.equals("clamp_lock") ? List.of() : BengerEffectPlan.frame(string, n7);
        if (list.isEmpty()) {
            return object;
        }
        if (object.isEmpty()) {
            return list;
        }
        ArrayList<Point> arrayList = new ArrayList<Point>(64);
        arrayList.addAll(list);
        arrayList.addAll(object.subList(0, Math.min(object.size(), 64 - arrayList.size())));
        return List.copyOf(arrayList);
    }

    public static int duration(String string) {
        return switch (string) {
            case "pack_pounce" -> 14;
            case "wide_sweep" -> 10;
            case "slam_windup", "slam_air" -> 8;
            case "slam_land" -> 22;
            case "clamp_lock" -> 60;
            case "rush_charge" -> 16;
            default -> 20;
        };
    }

    private static void point(List<Point> list, String string, double d, double d2, double d3, float[] fArray, float f) {
        if (list.size() < 64) {
            list.add(new Point(string, d, d2, d3, fArray[0], fArray[1], fArray[2], f));
        }
    }

    private static void ring(List<Point> list, String string, double d, double d2, int n, float[] fArray, float f, double d3) {
        for (int i = 0; i < n; ++i) {
            double d4 = (double)i * Math.PI * 2.0 / (double)n + d3;
            BengerEffectPlan.point(list, string, Math.cos(d4) * d, d2, Math.sin(d4) * d, fArray, f);
        }
    }

    private static void voice(List<Point> list, double d, double d2, float[] fArray, float f) {
        for (int i = 0; i < 24; ++i) {
            double d3 = (double)i * Math.PI * 2.0 / 24.0;
            BengerEffectPlan.point(list, "body", Math.cos(d3) * d, 2.45 + Math.sin(d3) * d, d2, fArray, f);
        }
    }

    private static void crescent(List<Point> list, int n, double d, double d2, double d3, float[] fArray, float f) {
        for (int i = 0; i < n; ++i) {
            double d4 = -1.18 + (double)i * 2.36 / (double)Math.max(1, n - 1) + d3;
            BengerEffectPlan.point(list, "body", Math.sin(d4) * d, d2 + Math.sin((double)i * 0.55) * 0.09, -Math.cos(d4) * d, fArray, f);
        }
    }

    private static void debris(List<Point> list, int n, double d, float[] fArray, float f, int n2) {
        for (int i = 0; i < n; ++i) {
            double d2 = (double)i * 2.399963229728653;
            double d3 = d * (0.28 + 0.72 * (double)((i * 17 + n2 * 7) % 31) / 30.0);
            BengerEffectPlan.point(list, "body", Math.cos(d2) * d3, 0.18 + (double)(i * 11 % 17) * 0.06, Math.sin(d2) * d3, fArray, f);
        }
    }

    private static List<Point> buildFrame(String string, int n) {
        ArrayList<Point> arrayList = new ArrayList<Point>();
        if (n < 0 || n > BengerEffectPlan.duration(string)) {
            return arrayList;
        }
        switch (string) {
            case "pack_howl": {
                if (n == 4 || n == 8 || n == 12) {
                    BengerEffectPlan.voice(arrayList, 0.5 + (double)n * 0.035, -2.0 - (double)n * 0.08, AMBER, 1.2f);
                }
                if (n != 8 && n != 12 && n != 16) break;
                BengerEffectPlan.ring(arrayList, "body", 1.6 + (double)(n - 8) * 0.14, 0.16, 24, AMBER, 1.1f, (double)n * 0.05);
                break;
            }
            case "pack_pounce": {
                if (n <= 4 && n % 2 == 0) {
                    for (int n2 : new int[]{-1, 1}) {
                        for (int i = 0; i < 9; ++i) {
                            BengerEffectPlan.point(arrayList, "body", (double)n2 * (0.8 + (double)i * 0.015), 0.7 + (double)i * 0.11, 0.5 + (double)i * 0.25, WHITE, 0.95f);
                        }
                    }
                }
                if (n != 6 && n != 8) break;
                BengerEffectPlan.ring(arrayList, "body", 2.7 + (double)(n - 6) * 0.22, 0.14, 32, AMBER, 1.25f, 0.0);
                BengerEffectPlan.debris(arrayList, 20, 2.5, EARTH, 1.1f, n);
                break;
            }
            case "wide_sweep": {
                if (n >= 3 && n <= 6) {
                    BengerEffectPlan.crescent(arrayList, 32, 2.5 + (double)(n - 3) * 0.22, 2.0, ((double)n - 4.5) * 0.19, n == 6 ? WHITE : AMBER, 1.15f);
                }
                if (n == 6) {
                    BengerEffectPlan.ring(arrayList, "body", 4.2, 0.16, 32, AMBER, 1.05f, 0.0);
                }
                if (n != 8) break;
                BengerEffectPlan.debris(arrayList, 16, 3.0, EARTH, 1.0f, n);
                break;
            }
            case "fear_roar": {
                if (n == 8) {
                    BengerEffectPlan.voice(arrayList, 0.9, -2.3, WHITE, 1.25f);
                }
                if (n < 8 || n > 17 || (n - 8) % 3 != 0) break;
                BengerEffectPlan.ring(arrayList, "body", 1.6 + (double)(n - 8) * 0.29, 0.35, 40, DANGER, 1.25f, (double)n * 0.07);
                break;
            }
            case "stun_bark": {
                if (n == 4 || n == 8 || n == 12) {
                    BengerEffectPlan.voice(arrayList, n == 8 ? 1.15 : 0.65, -2.3 - (double)(n - 4) * 0.1, n == 8 ? WHITE : AMBER, 1.3f);
                }
                if (n != 8 && n != 12 && n != 16) break;
                BengerEffectPlan.ring(arrayList, "body", 1.8 + (double)(n - 8) * 0.27, 0.65, 32, AMBER, 1.2f, 0.0);
                break;
            }
            case "slam_windup": 
            case "slam_air": {
                if (n == 0 || n == 3 || n == 6) {
                    BengerEffectPlan.ring(arrayList, "target", 4.0, 0.13, 40, string.equals("slam_air") ? DANGER : AMBER, 1.15f, 0.0);
                    for (int n3 : new int[]{-1, 1}) {
                        for (int i = 0; i < 4; ++i) {
                            BengerEffectPlan.point(arrayList, "target", (double)n3 * (0.2 + (double)i * 0.13), 0.16, (double)n3 * (0.2 + (double)i * 0.13), WHITE, 1.0f);
                            BengerEffectPlan.point(arrayList, "target", (double)n3 * (0.2 + (double)i * 0.13), 0.16, (double)(-n3) * (0.2 + (double)i * 0.13), WHITE, 1.0f);
                        }
                    }
                }
                if (!string.equals("slam_air") || n != 4) break;
                for (int i = 0; i < 12; ++i) {
                    BengerEffectPlan.point(arrayList, "body", i % 2 == 0 ? -0.55 : 0.55, 3.1 + (double)i * 0.11, 0.0, WHITE, 1.15f);
                }
                break;
            }
            case "slam_land": {
                if (n != 0 && n != 3 && n != 6) break;
                BengerEffectPlan.ring(arrayList, "body", 3.5 + (double)n * 0.15, 0.17, 40, n == 0 ? WHITE : AMBER, 1.45f, (double)n * 0.04);
                BengerEffectPlan.debris(arrayList, 24, 3.4, EARTH, 1.3f, n);
                break;
            }
            case "clamp_lock": {
                if (n == 0 || n == 20 || n == 40 || n == 60) {
                    int n4;
                    for (n4 = 0; n4 < 20; ++n4) {
                        double d = (double)n4 * Math.PI * 2.0 / 20.0;
                        BengerEffectPlan.point(arrayList, "body", Math.cos(d) * 0.5, 2.4 + Math.sin(d) * 0.22, -2.0, n == 0 ? AMBER : DANGER, 1.15f);
                    }
                    for (n4 = 0; n4 < 14; ++n4) {
                        BengerEffectPlan.point(arrayList, "body", (double)(n4 * 7 % 11 - 5) * 0.07, 2.18 + (double)n4 * 0.028, -1.8 - (double)(n4 % 3) * 0.14, WHITE, 0.95f);
                    }
                } else {
                    if (n <= 0 || n >= 60 || n % 12 != 0) break;
                    for (int i = 0; i < 8; ++i) {
                        BengerEffectPlan.point(arrayList, "body", (double)(i % 2 == 0 ? -1 : 1) * 0.32, 2.25 + (double)i * 0.035, -1.9, AMBER, 0.8f);
                    }
                }
                break;
            }
            case "rush_charge": {
                if (n >= 16 || n % 2 != 0) break;
                for (int n5 : new int[]{-1, 1}) {
                    for (int i = 0; i < 8; ++i) {
                        BengerEffectPlan.point(arrayList, "body", (double)n5 * (0.75 + (double)i * 0.02), 0.35 + (double)i * 0.07, 0.9 + (double)i * 0.3, WHITE, 1.05f);
                    }
                }
                BengerEffectPlan.debris(arrayList, 16, 1.6, EARTH, 1.1f, n);
                break;
            }
            case "milk_recover": {
                if (n != 8 && n != 11 && n != 14) break;
                for (int i = 0; i < 32; ++i) {
                    double d = (double)i * Math.PI * 4.0 / 32.0 + (double)n * 0.09;
                    BengerEffectPlan.point(arrayList, "body", Math.cos(d) * 0.9, 0.8 + (double)i * 0.075, Math.sin(d) * 0.9, i % 3 == 0 ? WHITE : MINT, 1.1f);
                }
                break;
            }
        }
        return arrayList;
    }

    public static double[] world(Point point, double d, double d2, double d3, double d4, double d5, double d6, double d7) {
        if (point.anchor().equals("target")) {
            return new double[]{d5 + point.offsetX(), d6 + point.offsetY(), d7 + point.offsetZ()};
        }
        double d8 = Math.toRadians(d4);
        return new double[]{d + point.offsetX() * Math.cos(d8) + point.offsetZ() * Math.sin(d8), d2 + point.offsetY(), d3 + point.offsetX() * Math.sin(d8) - point.offsetZ() * Math.cos(d8)};
    }

    public record Point(String anchor, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float size) {
    }
}

