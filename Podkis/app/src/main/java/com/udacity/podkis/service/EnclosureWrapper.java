package com.udacity.podkis.service;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import java.io.Serializable;

import static com.udacity.podkis.service.EnclosureWrapper.SERIALIZED_PODCAST_EPISODE_ENCLOSURE;

@Root(name = SERIALIZED_PODCAST_EPISODE_ENCLOSURE, strict = false)
public class EnclosureWrapper implements Serializable {

    static final String SERIALIZED_PODCAST_EPISODE_ENCLOSURE = "enclosure";
    private static final String SERIALIZED_PODCAST_EPISODE_URL = "url";

    @Attribute(name = SERIALIZED_PODCAST_EPISODE_URL)
    public String url;
}
