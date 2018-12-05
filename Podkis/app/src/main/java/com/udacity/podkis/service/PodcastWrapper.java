package com.udacity.podkis.service;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

import static com.udacity.podkis.service.ImageWrapper.SERIALIZED_PODCAST_EPISODE_IMAGE;
import static com.udacity.podkis.service.EpisodeWrapper.SERIALIZED_PODCAST_EPISODE_ROOT;
import static com.udacity.podkis.service.PodcastWrapper.SERIALIZED_PODCAST_ROOT;

@Root(name = SERIALIZED_PODCAST_ROOT, strict = false)
public class PodcastWrapper {

    static final String SERIALIZED_PODCAST_ROOT = "channel";
    private static final String SERIALIZED_PODCAST_TITLE = "title";
    private static final String SERIALIZED_PODCAST_DESCRIPTION = "description";
    private static final String SERIALIZED_PODCAST_AUTHOR = "author";

    @Element(name = SERIALIZED_PODCAST_TITLE)
    public String title;

    @Element(name = SERIALIZED_PODCAST_DESCRIPTION, data = true)
    public String description;

    @Element(name = SERIALIZED_PODCAST_AUTHOR)
    public String author;

    @ElementList(entry= SERIALIZED_PODCAST_EPISODE_IMAGE, inline = true)
    public List<ImageWrapper> image;


    @ElementList(entry = SERIALIZED_PODCAST_EPISODE_ROOT, inline = true)
    public List<EpisodeWrapper> episodeWrapperList;
}
