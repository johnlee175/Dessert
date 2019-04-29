package com.johnsoft.samples;

import java.lang.String;
import java.util.Map;

/**
 *  @author John Kenrinus Lee
 *  @version 2019-04-29
 */
public class Settings3Impl {
    public static final int MODE_FLY = 1;

    public static final int MODE_RUN = 2;

    public static final int MODE_CLIMB = 3;

    public final Map<String, String> fontMap;

    public final int mode;

    public Settings3Impl() {
        this(new Builder());
    }

    private Settings3Impl(Builder builder) {
        this.fontMap = builder.fontMap;
        this.mode = builder.mode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * @see Settings3Impl
     */
    public static class Builder<T extends Builder> {
        private Map<String, String> fontMap;

        private int mode;

        public Builder() {
            this.fontMap = new java.util.HashMap<>();
            this.mode = MODE_FLY;
        }

        private Builder(Settings3Impl settings3Impl) {
            this.fontMap = settings3Impl.fontMap;
            this.mode = settings3Impl.mode;
        }

        public T setFontMap(Map<String, String> fontMap) {
            this.fontMap = fontMap;
            return (T) this;
        }

        public T addToFontMap(String key, String value) {
            fontMap.put(key, value);
            return (T) this;
        }

        public T setModeToToFly() {
            this.mode = 1;
            return (T) this;
        }

        public T setModeToToRun() {
            this.mode = 2;
            return (T) this;
        }

        public T setModeToToClimb() {
            this.mode = 3;
            return (T) this;
        }

        public Settings3Impl build() {
            return new Settings3Impl(this);
        }
    }
}
