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

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by napster on 04.09.17.
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) throws Exception {

        AIConfiguration aiConfig = new AIConfiguration(Config.C.apiAiToken);
        AIDataService aiDataService = new AIDataService(aiConfig);

        final JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(Config.C.discordToken)
                .setGame(Game.of(Config.C.status))
                .setHttpClientBuilder(new OkHttpClient.Builder()
                        .retryOnConnectionFailure(true)
                        .readTimeout(30, TimeUnit.SECONDS))
                .addEventListener(new MainListener(aiDataService))
                .buildAsync();
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

            String input = event.getMessage().getStrippedContent();
            try {
                AIResponse response = aiDataService.request(new AIRequest(input));

                if (response.getStatus().getCode() == 200) {

                    if (response.getResult().getAction().equals("shutup")) {
                        return;
                    }

                    String out = response.getResult().getFulfillment().getSpeech();
                    if (out != null && !out.isEmpty()) {
                        out = rePlaceHolders(out);
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
}
