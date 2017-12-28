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

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.npstr.ddfb.info.AppInfo;
import space.npstr.ddfb.info.GitRepoState;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by napster on 04.09.17.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        //just post the info to the console
        if (args.length > 0 &&
                (args[0].equalsIgnoreCase("-v")
                        || args[0].equalsIgnoreCase("--version")
                        || args[0].equalsIgnoreCase("-version"))) {
            System.out.println("Version flag detected. Printing version info, then exiting.");
            System.out.println(getVersionInfo());
            System.out.println("Version info printed, exiting.");
            return;
        }

        log.info(getVersionInfo());

        AIConfiguration aiConfig = new AIConfiguration(Config.C.apiAiToken);
        AIDataService aiDataService = new AIDataService(aiConfig);

        new DefaultShardManagerBuilder()
                .setToken(Config.C.discordToken)
                .setGame(Game.playing(Config.C.status))
                .addEventListeners(new MainListener(aiDataService))
                .setAudioEnabled(false)
                .build();
    }

    public static class MainListener extends ListenerAdapter {

        private final AIDataService aiDataService;

        public MainListener(AIDataService aiDataService) {
            this.aiDataService = aiDataService;
        }

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            //ignore bots
            if (event.getAuthor().isBot()) {
                return;
            }
            //ignore none-whitelisted channels
            if (!(Config.C.channels.contains(event.getChannel().getId()))) {
                return;
            }

            String input = event.getMessage().getContentStripped();
            try {
                AIResponse response = aiDataService.request(new AIRequest(input));

                if (response.getStatus().getCode() == 200) {

                    if (response.getResult().getAction().equals("shutup")) {
                        return;
                    }

                    String out = response.getResult().getFulfillment().getSpeech();
                    if (out != null && !out.isEmpty()) {
                        out = rePlaceHolders(out);
                        log.info(String.format("\nUser input: %s\nBot output: %s", input, out));
                        event.getChannel().sendMessage(out).queue();
                    }
                } else {
                    log.error(response.getStatus().getErrorDetails());
                }
            } catch (Exception ex) {
                log.error("Random exception", ex);
            }
        }
    }

    //careful, the keys support regex characters
    private static final Map<String, String> placeHolders = new HashMap<>();

    static {
        placeHolders.put("<br>", "\n");
    }

    private static String rePlaceHolders(String input) {
        String result = input;
        for (Map.Entry<String, String> entry : placeHolders.entrySet()) {
            result = result.replaceAll(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Nonnull
    private static String getVersionInfo() {
        return "\n\n"
                + "\n\tVersion:       " + AppInfo.getAppInfo().VERSION
                + "\n\tBuild:         " + AppInfo.getAppInfo().BUILD_NUMBER
                + "\n\tBuild time:    " + asTimeInCentralEurope(AppInfo.getAppInfo().BUILD_TIME)
                + "\n\tCommit:        " + GitRepoState.getGitRepositoryState().commitIdAbbrev + " (" + GitRepoState.getGitRepositoryState().branch + ")"
                + "\n\tCommit time:   " + asTimeInCentralEurope(GitRepoState.getGitRepositoryState().commitTime * 1000)
                + "\n\tJVM:           " + System.getProperty("java.version")
                + "\n\tJDA:           " + JDAInfo.VERSION
                + "\n";
    }

    public static final DateTimeFormatter TIME_IN_CENTRAL_EUROPE = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z")
            .withZone(ZoneId.of("Europe/Berlin"));

    @Nonnull
    public static String asTimeInCentralEurope(final long epochMillis) {
        return TIME_IN_CENTRAL_EUROPE.format(Instant.ofEpochMilli(epochMillis));
    }
}
