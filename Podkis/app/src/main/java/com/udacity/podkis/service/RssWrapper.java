package com.udacity.podkis.service;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import static com.udacity.podkis.service.PodcastWrapper.SERIALIZED_PODCAST_ROOT;
import static com.udacity.podkis.service.RssWrapper.SERIALIZED_RSS_ROOT;

@Root(name = SERIALIZED_RSS_ROOT, strict = false)
public class RssWrapper {

    static final String SERIALIZED_RSS_ROOT = "rss";
    private static final String SERIALIZED_RSS_VERSION = "version";

    @Element(name = SERIALIZED_PODCAST_ROOT)
    public PodcastWrapper podcastWrapper;
}
