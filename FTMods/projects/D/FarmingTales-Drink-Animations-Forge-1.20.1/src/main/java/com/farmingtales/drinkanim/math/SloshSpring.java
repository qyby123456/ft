package com.farmingtales.drinkanim.math;

/** Fixed 20 Hz damped spring. Inputs are changes in player velocity and view angle. */
public final class SloshSpring {
    private double x, z, vx, vz, previousX, previousZ;
    public void tick(double impulseX, double impulseZ) {
        previousX = x;
        previousZ = z;
        vx = (vx + clamp(impulseX, -.3, .3) - x * .20) * .79;
        vz = (vz + clamp(impulseZ, -.3, .3) - z * .20) * .79;
        x = clamp(x + vx, -.55, .55);
        z = clamp(z + vz, -.55, .55);
    }
    public double x(double partial) { return previousX + (x - previousX) * partial; }
    public double z(double partial) { return previousZ + (z - previousZ) * partial; }
    public double energy() { return Math.abs(x) + Math.abs(z) + Math.abs(vx) + Math.abs(vz); }
    public void reset() { x = z = vx = vz = previousX = previousZ = 0; }
    private static double clamp(double n, double min, double max) { return Math.max(min, Math.min(max, n)); }
}
