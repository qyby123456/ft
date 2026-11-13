package net.farmingtales.tooltipscroll;

public final class AutoScrollController {
    private static final long TOP_PAUSE_MILLIS = 1500;
    private static final long BOTTOM_PAUSE_MILLIS = 2000;
    private static final long MANUAL_PAUSE_MILLIS = 5000;
    private static final long HOVER_GAP_MILLIS = 200;
    private static final float PIXELS_PER_SECOND = 18.0f;

    private Object hoveredTooltip;
    private float minimum;
    private float maximum;
    private float startOffset;
    private long scrollStartsAt;
    private long manualPauseUntil;
    private long lastFrameAt;

    public float update(Object tooltip, boolean overflowing, float lowerBound, float upperBound,
                        float currentOffset, long now) {
        if (!overflowing || tooltip == null || upperBound <= lowerBound) {
            hoveredTooltip = null;
            manualPauseUntil = 0;
            return Float.NaN;
        }

        if (hoveredTooltip != tooltip || minimum != lowerBound || maximum != upperBound
                || now < lastFrameAt || now - lastFrameAt > HOVER_GAP_MILLIS) {
            hoveredTooltip = tooltip;
            minimum = lowerBound;
            maximum = upperBound;
            startOffset = maximum;
            scrollStartsAt = now + TOP_PAUSE_MILLIS;
            manualPauseUntil = 0;
        }
        lastFrameAt = now;

        if (now < manualPauseUntil) {
            startOffset = Math.max(minimum, Math.min(maximum, currentOffset));
            scrollStartsAt = manualPauseUntil;
            return Float.NaN;
        }

        if (now <= scrollStartsAt) {
            return startOffset;
        }

        double elapsedMillis = now - scrollStartsAt;
        double travelMillis = (startOffset - minimum) * 1000.0 / PIXELS_PER_SECOND;
        if (elapsedMillis <= travelMillis) {
            return Math.max(minimum, startOffset - (float) (elapsedMillis * PIXELS_PER_SECOND / 1000.0));
        }
        if (elapsedMillis < travelMillis + BOTTOM_PAUSE_MILLIS) {
            return minimum;
        }

        startOffset = maximum;
        scrollStartsAt = now + TOP_PAUSE_MILLIS;
        return maximum;
    }

    public void onManualInput(long now) {
        if (hoveredTooltip != null) {
            manualPauseUntil = now + MANUAL_PAUSE_MILLIS;
        }
    }
}
