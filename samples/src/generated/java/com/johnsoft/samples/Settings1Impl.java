package com.johnsoft.samples;

import java.lang.String;

/**
 *  @author John Kenrinus Lee
 *  @version 2019-04-29
 */
public final class Settings1Impl {
    public final String background;

    public final int windowHeight;

    public Settings1Impl() {
        this(new Builder());
    }

    private Settings1Impl(Builder builder) {
        this.background = builder.background;
        this.windowHeight = builder.windowHeight;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * @see Settings1Impl
     */
    public static final class Builder {
        private String background;

        private int windowHeight;

        public Builder() {
            this.background = "Red";
            this.windowHeight = 440;
        }

        private Builder(Settings1Impl settings1Impl) {
            this.background = settings1Impl.background;
            this.windowHeight = settings1Impl.windowHeight;
        }

        public Builder setBackground(String background) {
            this.background = background;
            return this;
        }

        public Builder setWindowHeight(int windowHeight) {
            this.windowHeight = windowHeight;
            return this;
        }

        public Settings1Impl build() {
            return new Settings1Impl(this);
        }
    }
}
