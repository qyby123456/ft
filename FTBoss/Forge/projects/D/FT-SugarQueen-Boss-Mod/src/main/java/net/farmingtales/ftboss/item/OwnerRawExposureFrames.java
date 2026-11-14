package net.farmingtales.ftboss.item;

import java.lang.StackWalker.StackFrame;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/** 原始字段鉴权的单次栈帧筛选；保持跳过首帧、过滤钩子和四帧边界，不缓存授权结果。 */
public final class OwnerRawExposureFrames {
    private static final int FRAME_LIMIT = 4;

    private OwnerRawExposureFrames() {}

    public static List<StackFrame> select(Stream<StackFrame> stream) {
        Iterator<StackFrame> frames = stream.iterator();
        if (frames.hasNext()) frames.next();
        StackFrame observer = null;
        StackFrame raw = null;
        int examined = 0;
        while (examined < FRAME_LIMIT && frames.hasNext()) {
            StackFrame frame = frames.next();
            String name = frame.getMethodName();
            if (name.contains("ftboss$ownerRawExposed")) continue;
            examined++;
            if (name.startsWith("ftboss$raw")) {
                if (raw == null) raw = frame;
            } else if (observer == null) {
                observer = frame;
            }
            if (observer != null && raw != null) break;
        }
        return Arrays.asList(observer, raw);
    }
}
