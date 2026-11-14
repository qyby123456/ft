package net.farmingtales.ftboss.zheergenphantom;

import java.util.UUID;

public record PhantomSnapshot(int delay, String skill, UUID target, double horizontal, double vertical, double depth) {
}

