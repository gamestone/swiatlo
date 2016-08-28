package com.html.core;

public class Interfaces {

    public static interface ITagListenner {

        // jesli zwraca false to tag zostanie pominiety
        public boolean onBeforeBuildTag(Node node);
    }

    public static interface INonContentTag {

    }
}
