/*
 * MIT License
 *
 * Copyright (c) 2017, Dennis Neufeld
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package space.npstr.ddfb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by napster on 05.09.17.
 */
public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    //avoid a (in this case unnecessary) gettocalypse by making all the values public final
    public static final Config C;

    static {
        Config c;
        try {
            c = new Config();
        } catch (final IOException e) {
            c = null;
            log.error("Could not load config files!" + e);
        }
        C = c;
    }

    public final boolean isDebug;
    public final String discordToken;
    public final String apiAiToken;
    public final String status;

    public final List<String> channels;

    @SuppressWarnings(value = "unchecked")
    public Config() throws IOException {

        final File configFile = new File("config.yaml");
        final Yaml yaml = new Yaml();
        try (Reader reader = new InputStreamReader(new FileInputStream(configFile), "UTF-8")) {
            final Map<String, Object> config = yaml.load(reader);
            //change nulls to empty strings
            config.keySet().forEach((String key) -> config.putIfAbsent(key, ""));

            //where are we running?
            this.isDebug = (boolean) config.getOrDefault("isDebug", false);

            final Map<String, Object> values;
            if (this.isDebug) values = (Map) config.get("debug");
            else values = (Map) config.get("prod");

            this.discordToken = (String) values.getOrDefault("discordToken", "");
            this.apiAiToken = (String) values.getOrDefault("dialogflowToken", "");

            this.channels = (List<String>) values.getOrDefault("channels", Collections.emptyList());
            this.status = (String) values.getOrDefault("status", "");
        }
    }
}
