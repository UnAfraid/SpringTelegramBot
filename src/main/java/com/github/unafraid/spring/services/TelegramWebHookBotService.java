package com.github.unafraid.spring.services;

import javax.inject.Inject;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiConstants;
import org.telegram.telegrambots.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.api.objects.WebhookInfo;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import com.github.unafraid.spring.bot.TelegramWebHookBot;
import com.github.unafraid.spring.bot.util.BotUtil;
import com.github.unafraid.spring.config.TelegramBotConfig;

/**
 * @author UnAfraid
 */
@Service
public class TelegramWebHookBotService extends TelegramWebHookBot {
	private static final Logger LOGGER = LoggerFactory.getLogger(TelegramWebHookBotService.class);

	@Inject
	private TelegramBotConfig config;

	@Inject
	public TelegramWebHookBotService(TelegramBotConfig config) throws Exception {
		super(config.getToken(), config.getUsername());
		final WebhookInfo info = getWebhookInfo();
		final String url = info.getUrl();
		final StringBuilder sb = new StringBuilder(config.getPath());
		if (sb.charAt(sb.length() - 1) != '/') {
			sb.append('/');
		}
		sb.append("callback/");
		sb.append(config.getToken());
		final String webHookUrl = sb.toString();
		LOGGER.info("Verifying web hook..");
		if (url == null || url.isEmpty() || !url.equals(webHookUrl)) {
			LOGGER.info("Web Hook URL require changes updating to: {} ..", webHookUrl);
			setWebhook(webHookUrl, "");
		} else {
			LOGGER.info("Web Hook is okay {}", webHookUrl);
		}
	}

	@Override
	public void setWebhook(String url, String publicCertificatePath) throws TelegramApiRequestException {
		try {
			final SetWebhook setWebhook = new SetWebhook();
			setWebhook.setUrl(url);
			if (publicCertificatePath != null && !publicCertificatePath.isEmpty()) {
				setWebhook.setCertificateFile(publicCertificatePath);
			}
			setWebhook.setMaxConnections(40);
			final String responseContent = BotUtil.doPostJSONQuery(this, SetWebhook.PATH, setWebhook);
			final JSONObject jsonObject = new JSONObject(responseContent);
			if (!jsonObject.getBoolean(ApiConstants.RESPONSE_FIELD_OK)) {
				throw new TelegramApiRequestException("Error setting web hook", jsonObject);
			}
			LOGGER.info("Web hook set: {}", jsonObject);
		} catch (JSONException e) {
			throw new TelegramApiRequestException("Error de-serializing setWebHook method response", e);
		} catch (Exception e) {
			throw new TelegramApiRequestException("Error executing setWebHook method", e);
		}
	}

	@Override
	public String getBotPath() {
		return config.getPath();
	}
}
