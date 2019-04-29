package com.johnsoft.samples;

import java.awt.Color;
import java.awt.Font;

/**
 *  @author John Kenrinus Lee
 *  @version 2019-04-29
 */
public final class Settings2Impl {
    public static final class Fonts {
        private final Font normal;

        public Fonts() {
            this(new Builder());
        }

        private Fonts(Builder builder) {
            this.normal = builder.normal;
        }

        public Font getNormal() {
            return normal;
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder newBuilder() {
            return new Builder(this);
        }

        /**
         * @see Fonts
         */
        public static final class Builder {
            private Font normal;

            public Builder() {
                this.normal = new java.awt.Font("Arial", java.awt.Font.BOLD, 32);
            }

            private Builder(Fonts fonts) {
                this.normal = fonts.normal;
            }

            public Builder setNormal(Font normal) {
                this.normal = normal;
                return this;
            }

            public Fonts build() {
                return new Fonts(this);
            }
        }
    }

    public static final class Colors {
        private final Color foreground;

        public Colors() {
            this(new Builder());
        }

        private Colors(Builder builder) {
            this.foreground = builder.foreground;
        }

        public Color getForeground() {
            return foreground;
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder newBuilder() {
            return new Builder(this);
        }

        /**
         * @see Colors
         */
        public static final class Builder {
            private Color foreground;

            public Builder() {
                this.foreground = com.johnsoft.samples.utils.Util.getDefaultColor();
            }

            private Builder(Colors colors) {
                this.foreground = colors.foreground;
            }

            public Builder setForeground(Color foreground) {
                this.foreground = foreground;
                return this;
            }

            public Colors build() {
                return new Colors(this);
            }
        }
    }
}
