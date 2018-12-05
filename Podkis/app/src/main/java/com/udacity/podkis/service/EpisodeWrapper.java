package com.udacity.podkis.service;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

import static com.udacity.podkis.service.EnclosureWrapper.SERIALIZED_PODCAST_EPISODE_ENCLOSURE;
import static com.udacity.podkis.service.EpisodeWrapper.SERIALIZED_PODCAST_EPISODE_ROOT;
import static com.udacity.podkis.service.ImageWrapper.SERIALIZED_PODCAST_EPISODE_IMAGE;

@Root(name = SERIALIZED_PODCAST_EPISODE_ROOT, strict = false)
public class EpisodeWrapper {

    static final String SERIALIZED_PODCAST_EPISODE_ROOT = "item";
    private static final String SERIALIZED_PODCAST_EPISODE_TITLE = "title";
    private static final String SERIALIZED_PODCAST_EPISODE_DESCRIPTION = "description";
    private static final String SERIALIZED_PODCAST_EPISODE_NUMBER = "episode";
    private static final String SERIALIZED_PODCAST_EPISODE_SEASON_NUMBER = "season";
    private static final String SERIALIZED_PODCAST_EPISODE_PUBLISHED_DATE = "pubDate";
    private static final String SERIALIZED_PODCAST_EPISODE_DURATION = "duration";

    @ElementList(entry = SERIALIZED_PODCAST_EPISODE_TITLE, inline = true)
    public List<String> title;

    @Element(name = SERIALIZED_PODCAST_EPISODE_DESCRIPTION, data = true)
    public String description;

    @Element(name = SERIALIZED_PODCAST_EPISODE_NUMBER)
    public Integer episodeNumber;

    @Element(name = SERIALIZED_PODCAST_EPISODE_SEASON_NUMBER, required = false)
    public Integer seasonNumber;

    @Element(name = SERIALIZED_PODCAST_EPISODE_PUBLISHED_DATE)
    public String publishedDate;

    @Element(name = SERIALIZED_PODCAST_EPISODE_IMAGE)
    public ImageWrapper image;

    @Element(name = SERIALIZED_PODCAST_EPISODE_DURATION)
    public String duration;

    @Element(name = SERIALIZED_PODCAST_EPISODE_ENCLOSURE)
    public EnclosureWrapper enclosure;
}
