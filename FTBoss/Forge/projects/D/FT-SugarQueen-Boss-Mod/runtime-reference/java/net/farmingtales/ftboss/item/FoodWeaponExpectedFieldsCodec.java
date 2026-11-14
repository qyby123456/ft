/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.item;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace;

final class FoodWeaponExpectedFieldsCodec {
    private final ByteBuffer input;
    private int nodes;

    private FoodWeaponExpectedFieldsCodec(byte[] byArray) {
        if (byArray == null || byArray.length > 0x100000) {
            throw new IllegalArgumentException("Independent d084 cursor bytes required");
        }
        this.input = ByteBuffer.wrap((byte[])byArray.clone());
    }

    static FoodWeaponExpectedTrace.Value decodeD084(byte[] byArray) {
        FoodWeaponExpectedFieldsCodec foodWeaponExpectedFieldsCodec = new FoodWeaponExpectedFieldsCodec(byArray);
        if (foodWeaponExpectedFieldsCodec.input.getInt() != 1179932499 || foodWeaponExpectedFieldsCodec.input.getInt() != 3) {
            throw new IllegalArgumentException("Expected exact d084 canonical version");
        }
        FoodWeaponExpectedTrace.Value value = foodWeaponExpectedFieldsCodec.value(0);
        if (foodWeaponExpectedFieldsCodec.input.hasRemaining()) {
            throw new IllegalArgumentException("Trailing independent canonical bytes");
        }
        return value;
    }

    private String text() {
        int n = this.count(1);
        ByteBuffer byteBuffer = this.input.slice();
        byteBuffer.limit(n);
        this.input.position(this.input.position() + n);
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(byteBuffer).toString();
        }
        catch (CharacterCodingException characterCodingException) {
            throw new IllegalArgumentException("Malformed independent UTF-8", characterCodingException);
        }
    }

    private int count(int n) {
        int n2 = this.input.getInt();
        if (n2 < 0 || n2 > this.input.remaining() / n) {
            throw new IllegalArgumentException("Independent canonical length");
        }
        return n2;
    }

    private FoodWeaponExpectedTrace.Value value(int n) {
        if (n > 64 || ++this.nodes > 65536) {
            throw new IllegalArgumentException("Independent canonical complexity");
        }
        int n2 = Byte.toUnsignedInt(this.input.get());
        return switch (n2) {
            case 0 -> FoodWeaponExpectedTrace.end();
            case 1 -> FoodWeaponExpectedTrace.number(n2, this.input.get());
            case 2 -> FoodWeaponExpectedTrace.number(n2, this.input.getShort());
            case 3, 5 -> FoodWeaponExpectedTrace.number(n2, this.input.getInt());
            case 4, 6 -> FoodWeaponExpectedTrace.number(n2, this.input.getLong());
            case 8 -> FoodWeaponExpectedTrace.string(this.text());
            case 10 -> this.compound(n);
            case 9 -> this.list(n);
            case 7, 11, 12 -> this.array(n2);
            default -> throw new IllegalArgumentException("Unknown independent canonical kind");
        };
    }

    private FoodWeaponExpectedTrace.Value compound(int n) {
        int n2 = this.count(1);
        if (n2 > 65536 - this.nodes) {
            throw new IllegalArgumentException("Independent compound budget");
        }
        String[] stringArray = new String[n2];
        FoodWeaponExpectedTrace.Value[] valueArray = new FoodWeaponExpectedTrace.Value[n2];
        for (int i = 0; i < n2; ++i) {
            stringArray[i] = this.text();
            valueArray[i] = this.value(n + 1);
        }
        return FoodWeaponExpectedTrace.compound(stringArray, valueArray);
    }

    private FoodWeaponExpectedTrace.Value list(int n) {
        int n2 = Byte.toUnsignedInt(this.input.get());
        int n3 = this.count(1);
        if (n3 > 65536 - this.nodes) {
            throw new IllegalArgumentException("Independent list budget");
        }
        FoodWeaponExpectedTrace.Value[] valueArray = new FoodWeaponExpectedTrace.Value[n3];
        for (int i = 0; i < n3; ++i) {
            valueArray[i] = this.value(n + 1);
        }
        return FoodWeaponExpectedTrace.list(n2, valueArray);
    }

    private FoodWeaponExpectedTrace.Value array(int n) {
        int n2 = n == 7 ? 1 : (n == 11 ? 4 : 8);
        int n3 = this.count(n2);
        long[] lArray = new long[n3];
        for (int i = 0; i < n3; ++i) {
            lArray[i] = n2 == 1 ? (long)this.input.get() : (n2 == 4 ? (long)this.input.getInt() : this.input.getLong());
        }
        return FoodWeaponExpectedTrace.array(n, lArray);
    }
}

