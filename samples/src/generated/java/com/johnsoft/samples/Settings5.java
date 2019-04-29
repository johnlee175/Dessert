package com.johnsoft.samples;

import com.johnsoft.samples.utils.Nullable;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

/**
 *  @author John Kenrinus Lee
 *  @version 2019-04-29
 */
public class Settings5 {
    /**
     *  a comment will be copied
     */
    @Nullable
    private static final String xib = "iiiii";

    @Nullable
    public final String name;

    public final int age;

    public final long xs;

    public final ArrayList<String> keys;

    public final List<String> values;

    public Settings5() {
        this(new Builder());
    }

    private Settings5(Builder builder) {
        this.name = builder.name;
        this.age = builder.age;
        this.xs = builder.xs;
        this.keys = builder.keys;
        this.values = builder.values;
    }

    @Nullable
    public <T> T test(@Nullable String... stb) throws IllegalArgumentException {
        System.out.println("MMMMMM");
            return (T)this;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * @see Settings5
     */
    public static class Builder<T extends Builder> {
        @Nullable
        private String name;

        private int age;

        private long xs;

        private ArrayList<String> keys;

        private List<String> values;

        public Builder() {
            this.name = "dark";
            this.age = 3;
            this.xs = com.johnsoft.samples.utils.Util.Inner.class.hashCode();
            this.keys = new java.util.ArrayList<>();
            this.values = new java.util.ArrayList<>();
        }

        private Builder(Settings5 settings5) {
            this.name = settings5.name;
            this.age = settings5.age;
            this.xs = settings5.xs;
            this.keys = settings5.keys;
            this.values = settings5.values;
        }

        public T setName(@Nullable String name) {
            this.name = name;
            return (T) this;
        }

        public T setAge(int age) {
            int tmp = age; if (tmp > 0) this.age = tmp;
            return (T) this;
        }

        public T setXs(long xs) {
            this.xs = xs;
            return (T) this;
        }

        public T setKeys(ArrayList<String> keys) {
            this.keys = keys;
            return (T) this;
        }

        public T addToKeys(String value) {
            keys.add(value);
            return (T) this;
        }

        public T setValues(List<String> values) {
            this.values = values;
            return (T) this;
        }

        public T addToValues(String value) {
            values.add(value);
            return (T) this;
        }

        public Settings5 build() {
            return new Settings5(this);
        }
    }
}
