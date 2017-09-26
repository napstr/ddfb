/*
 * Copyright (C) 2017 Dennis Neufeld
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package space.npstr.discordapiaibot;

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
            final Map<String, Object> config = (Map<String, Object>) yaml.load(reader);
            //change nulls to empty strings
            config.keySet().forEach((String key) -> config.putIfAbsent(key, ""));

            //where are we running?
            this.isDebug = (boolean) config.getOrDefault("isDebug", false);

            final Map<String, Object> values;
            if (this.isDebug) values = (Map) config.get("debug");
            else values = (Map) config.get("prod");

            this.discordToken = (String) values.getOrDefault("discordToken", "");
            this.apiAiToken = (String) values.getOrDefault("apiAiToken", "");

            this.channels = (List<String>) values.getOrDefault("channels", Collections.emptyList());
            this.status = (String) values.getOrDefault("status", "");
        }
    }
}
